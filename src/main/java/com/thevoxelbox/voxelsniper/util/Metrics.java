/*
 * This file is part of VoxelSniper, licensed under the MIT License (MIT).
 *
 * Copyright (c) The VoxelBox <http://thevoxelbox.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.thevoxelbox.voxelsniper.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * Metrics collector for mcstats.
 */
public abstract class Metrics {

    /**
     * The current revision number.
     */
    private static final int REVISION = 7;

    /**
     * The base url of the metrics domain.
     */
    private static final String BASE_URL = "http://report.mcstats.org";

    /**
     * The url used to report a server's status.
     */
    private static final String REPORT_URL = "/plugin/%s";

    /**
     * Interval of time to ping (in minutes).
     */
    private static final int PING_INTERVAL = 15;

    /**
     * Debug mode.
     */
    final boolean debug;

    /**
     * All of the custom graphs to submit to metrics.
     */
    final Set<Graph> graphs = Collections.synchronizedSet(new HashSet<Graph>());

    /**
     * The plugin configuration file.
     */
    private final Properties properties = new Properties();

    /**
     * The plugin's name.
     */
    private final String pluginName;

    /**
     * The plugin's version.
     */
    private final String pluginVersion;

    /**
     * The plugin configuration file.
     */
    private final File configurationFile;

    /**
     * Unique server id.
     */
    private final String guid;

    /**
     * Lock for synchronization.
     */
    final Object optOutLock = new Object();

    /**
     * The thread submission is running on.
     */
    Thread thread;

    /**
     * Creates a new Metrics manager.
     * 
     * @param pluginName The plugin name
     * @param pluginVersion The plugin version
     * @throws IOException If there is an issue reading the configuration file
     */
    public Metrics(String pluginName, String pluginVersion, File configFile) throws IOException {
        if (pluginName == null || pluginVersion == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;

        this.configurationFile = configFile;

        if (!this.configurationFile.exists()) {
            if (this.configurationFile.getPath().contains("/") || this.configurationFile.getPath().contains("\\")) {
                File parent = new File(this.configurationFile.getParent());
                if (!parent.exists()) {
                    parent.mkdirs();
                }
            }

            this.configurationFile.createNewFile(); // config file
            this.properties.put("opt-out", "false");
            this.properties.put("guid", UUID.randomUUID().toString());
            this.properties.put("debug", "false");
            this.properties.store(new FileOutputStream(this.configurationFile), "http://mcstats.org");
        } else {
            this.properties.load(new FileInputStream(this.configurationFile));
        }

        this.guid = this.properties.getProperty("guid");
        this.debug = Boolean.parseBoolean(this.properties.getProperty("debug"));
    }

    /**
     * Get the full server version.
     * 
     * @return The server version
     */
    public abstract String getServerVersion();

    /**
     * Get the amount of players online.
     * 
     * @return Players online
     */
    public abstract int getPlayersOnline();

    /**
     * Construct and create a Graph that can be used to separate specific
     * plotters to their own graphs on the metrics website. Plotters can be
     * added to the graph object returned.
     * 
     * @param name The name of the graph
     * @return Graph object created. Will never return NULL under normal
     *         circumstances unless bad parameters are given
     */
    public Graph createGraph(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Graph name cannot be null");
        }

        // Construct the graph object
        final Graph graph = new Graph(name);

        // Now we can add our graph
        this.graphs.add(graph);

        // and return back
        return graph;
    }

    /**
     * Add a Graph object to SpoutMetrics that represents data for the plugin
     * that should be sent to the backend.
     * 
     * @param graph The name of the graph
     */
    public void addGraph(final Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }

        this.graphs.add(graph);
    }

    /**
     * Start measuring statistics. This will immediately create an async
     * repeating task as the plugin and send the initial data to the metrics
     * backend, and then after that it will post in increments of PING_INTERVAL
     * * 1200 ticks.
     * 
     * @return True if statistics measuring is running, otherwise false.
     */
    public boolean start() {
        synchronized (this.optOutLock) {
            // Did we opt out?
            if (isOptOut()) {
                return false;
            }

            // Is metrics already running?
            if (this.thread != null) {
                return true;
            }

            this.thread = new Thread(new Runnable() {

                private boolean firstPost = true;

                private long nextPost;

                @Override
                public void run() {
                    while (Metrics.this.thread != null) {
                        if (this.nextPost == 0L || System.currentTimeMillis() > this.nextPost) {
                            try {
                                // This has to be synchronized or it can collide
                                // with the disable method.
                                synchronized (Metrics.this.optOutLock) {
                                    // Disable Task, if it is running and the
                                    // server owner decided to opt-out
                                    if (isOptOut() && Metrics.this.thread != null) {
                                        Thread temp = Metrics.this.thread;
                                        Metrics.this.thread = null;
                                        // Tell all plotters to stop gathering
                                        // information.
                                        for (Graph graph : Metrics.this.graphs) {
                                            graph.onOptOut();
                                        }
                                        temp.interrupt(); // interrupting
                                                          // ourselves
                                        return;
                                    }
                                }

                                // We use the inverse of firstPost because if it
                                // is the first time we are posting,
                                // it is not a interval ping, so it evaluates to
                                // FALSE
                                // Each time thereafter it will evaluate to
                                // TRUE, i.e PING!
                                postPlugin(!this.firstPost);

                                // After the first post we set firstPost to
                                // false
                                // Each post thereafter will be a ping
                                this.firstPost = false;
                                this.nextPost = System.currentTimeMillis() + (PING_INTERVAL * 60 * 1000);
                            } catch (IOException e) {
                                if (Metrics.this.debug) {
                                    System.out.println("[Metrics] " + e.getMessage());
                                }
                            }
                        }

                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            // ignored
                        }
                    }
                }
            }, "MCStats / Plugin Metrics");
            this.thread.start();

            return true;
        }
    }

    /**
     * Has the server owner denied plugin metrics?
     * 
     * @return True if metrics should be opted out of it
     */
    public boolean isOptOut() {
        synchronized (this.optOutLock) {
            try {
                // Reload the metrics file
                this.properties.load(new FileInputStream(this.configurationFile));
            } catch (IOException ex) {
                if (this.debug) {
                    System.out.println("[Metrics] " + ex.getMessage());
                }
                return true;
            }

            return Boolean.parseBoolean(this.properties.getProperty("opt-out"));
        }
    }

    /**
     * Enables metrics for the server by setting "opt-out" to false in the
     * config file and starting the metrics task.
     * 
     * @throws java.io.IOException If there is issue loading config
     */
    public void enable() throws IOException {
        // This has to be synchronized or it can collide with the check in the
        // task.
        synchronized (this.optOutLock) {
            // Check if the server owner has already set opt-out, if not, set
            // it.
            if (isOptOut()) {
                this.properties.setProperty("opt-out", "false");
                this.properties.store(new FileOutputStream(this.configurationFile), "http://mcstats.org");
            }

            // Enable Task, if it is not running
            if (this.thread == null) {
                start();
            }
        }
    }

    /**
     * Disables metrics for the server by setting "opt-out" to true in the
     * config file and canceling the metrics task.
     * 
     * @throws java.io.IOException If there is issue loading config
     */
    public void disable() throws IOException {
        // This has to be synchronized or it can collide with the check in the
        // task.
        synchronized (this.optOutLock) {
            // Check if the server owner has already set opt-out, if not, set
            // it.
            if (!isOptOut()) {
                this.properties.setProperty("opt-out", "true");
                this.properties.store(new FileOutputStream(this.configurationFile), "http://mcstats.org");
            }

            // Disable Task, if it is running
            if (this.thread != null) {
                this.thread.interrupt();
                this.thread = null;
            }
        }
    }

    /**
     * Generic method that posts a plugin to the metrics website.
     */
    void postPlugin(final boolean isPing) throws IOException {
        String serverVersion = getServerVersion();
        int playersOnline = getPlayersOnline();

        // END server software specific section -- all code below does not use
        // any code outside of this class / Java

        // Construct the post data
        StringBuilder json = new StringBuilder(1024);
        json.append('{');

        // The plugin's description file containg all of the plugin data such as
        // name, version, author, etc
        appendJSONPair(json, "guid", this.guid);
        appendJSONPair(json, "plugin_version", this.pluginVersion);
        appendJSONPair(json, "server_version", serverVersion);
        appendJSONPair(json, "players_online", Integer.toString(playersOnline));

        // New data as of R6
        String osname = System.getProperty("os.name");
        String osarch = System.getProperty("os.arch");
        String osversion = System.getProperty("os.version");
        String javaVersion = System.getProperty("java.version");
        int coreCount = Runtime.getRuntime().availableProcessors();

        // normalize os arch .. amd64 -> x86_64
        if (osarch.equals("amd64")) {
            osarch = "x86_64";
        }

        appendJSONPair(json, "osname", osname);
        appendJSONPair(json, "osarch", osarch);
        appendJSONPair(json, "osversion", osversion);
        appendJSONPair(json, "cores", Integer.toString(coreCount));
        // appendJSONPair(json, "auth_mode", onlineMode ? "1" : "0");
        appendJSONPair(json, "java_version", javaVersion);

        // If we're pinging, append it
        if (isPing) {
            appendJSONPair(json, "ping", "1");
        }

        if (this.graphs.size() > 0) {
            synchronized (this.graphs) {
                json.append(',');
                json.append('"');
                json.append("graphs");
                json.append('"');
                json.append(':');
                json.append('{');

                boolean firstGraph = true;

                final Iterator<Graph> iter = this.graphs.iterator();

                while (iter.hasNext()) {
                    Graph graph = iter.next();

                    StringBuilder graphJson = new StringBuilder();
                    graphJson.append('{');

                    for (Plotter plotter : graph.getPlotters()) {
                        appendJSONPair(graphJson, plotter.getColumnName(), Integer.toString(plotter.getValue()));
                    }

                    graphJson.append('}');

                    if (!firstGraph) {
                        json.append(',');
                    }

                    json.append(escapeJSON(graph.getName()));
                    json.append(':');
                    json.append(graphJson);

                    firstGraph = false;
                }

                json.append('}');
            }
        }

        // close json
        json.append('}');

        // Create the url
        URL url = new URL(BASE_URL + String.format(REPORT_URL, urlEncode(this.pluginName)));

        // Connect to the website
        URLConnection connection;

        // Mineshafter creates a socks proxy, so we can safely bypass it
        // It does not reroute POST requests so we need to go around it
        if (isMineshafterPresent()) {
            connection = url.openConnection(Proxy.NO_PROXY);
        } else {
            connection = url.openConnection();
        }

        byte[] uncompressed = json.toString().getBytes();
        byte[] compressed = gzip(json.toString());

        // Headers
        connection.addRequestProperty("User-Agent", "MCStats/" + REVISION);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", Integer.toString(compressed.length));
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");

        connection.setDoOutput(true);

        if (this.debug) {
            System.out.println("[Metrics] Prepared request for " + this.pluginName + " uncompressed=" + uncompressed.length + " compressed="
                    + compressed.length);
        }
        String response = null;
        // Write the data
        try (OutputStream os = connection.getOutputStream()) {
            os.write(compressed);
            os.flush();

            // Now read the response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                response = reader.readLine();
            }
        }

        if (response == null || response.startsWith("ERR") || response.startsWith("7")) {
            if (response == null) {
                response = "null";
            } else if (response.startsWith("7")) {
                response = response.substring(response.startsWith("7,") ? 2 : 1);
            }

            throw new IOException(response);
        }
        // Is this the first update this hour?
        if ("1".equals(response) || "This is your first update this hour".contains(response)) {
            synchronized (this.graphs) {
                final Iterator<Graph> iter = this.graphs.iterator();

                while (iter.hasNext()) {
                    final Graph graph = iter.next();

                    for (Plotter plotter : graph.getPlotters()) {
                        plotter.reset();
                    }
                }
            }
        }
    }

    /**
     * GZip compress a string of bytes.
     * 
     * @param input The input
     * @return The gziped string The input compressed
     */
    public static byte[] gzip(String input) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = null;

        try {
            gzos = new GZIPOutputStream(baos);
            gzos.write(input.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzos != null) {
                try {
                    gzos.close();
                } catch (IOException ignore) {
                    // ignored
                }
            }
        }

        return baos.toByteArray();
    }

    /**
     * Check if mineshafter is present. If it is, we need to bypass it to send
     * POST requests
     * 
     * @return True if mineshafter is installed on the server
     */
    private static boolean isMineshafterPresent() {
        try {
            Class.forName("mineshafter.MineServer");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Appends a json encoded key/value pair to the given string builder.
     * 
     * @param json
     * @param key
     * @param value
     * @throws UnsupportedEncodingException
     */
    private static void appendJSONPair(StringBuilder json, String key, String value) throws UnsupportedEncodingException {
        boolean isValueNumeric = false;

        try {
            if ("0".equals(value) || !"0".endsWith(value)) {
                Double.parseDouble(value);
                isValueNumeric = true;
            }
        } catch (NumberFormatException e) {
            isValueNumeric = false;
        }

        if (json.charAt(json.length() - 1) != '{') {
            json.append(',');
        }

        json.append(escapeJSON(key));
        json.append(':');

        if (isValueNumeric) {
            json.append(value);
        } else {
            json.append(escapeJSON(value));
        }
    }

    /**
     * Escape a string to create a valid JSON string.
     * 
     * @param text The text to escape
     * @return The escaped text
     */
    private static String escapeJSON(String text) {
        StringBuilder builder = new StringBuilder();

        builder.append('"');
        for (int index = 0; index < text.length(); index++) {
            char chr = text.charAt(index);

            switch (chr) {
                case '"':
                case '\\':
                    builder.append('\\');
                    builder.append(chr);
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                default:
                    if (chr < ' ') {
                        String t = "000" + Integer.toHexString(chr);
                        builder.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        builder.append(chr);
                    }
                    break;
            }
        }
        builder.append('"');

        return builder.toString();
    }

    /**
     * Encode text as UTF-8.
     * 
     * @param text The text to encode
     * @return The encoded text, as UTF-8
     */
    private static String urlEncode(final String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }

    /**
     * Represents a custom graph on the website.
     */
    public static class Graph {

        /**
         * The graph's name, alphanumeric and spaces only :) If it does not
         * comply to the above when submitted, it is rejected.
         */
        private final String name;

        /**
         * The set of plotters that are contained within this graph.
         */
        private final Set<Plotter> plotters = new LinkedHashSet<Plotter>();

        Graph(final String name) {
            this.name = name;
        }

        /**
         * Gets the graph's name.
         * 
         * @return The Graph's name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Add a plotter to the graph, which will be used to plot entries.
         * 
         * @param plotter The plotter to add to the graph
         */
        public void addPlotter(final Plotter plotter) {
            this.plotters.add(plotter);
        }

        /**
         * Remove a plotter from the graph.
         * 
         * @param plotter The plotter to remove from the graph
         */
        public void removePlotter(final Plotter plotter) {
            this.plotters.remove(plotter);
        }

        /**
         * Gets an <b>unmodifiable</b> set of the plotter objects in the graph.
         * 
         * @return An unmodifiable {@link java.util.Set} of the plotter objects
         */
        public Set<Plotter> getPlotters() {
            return Collections.unmodifiableSet(this.plotters);
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(final Object object) {
            if (!(object instanceof Graph)) {
                return false;
            }

            final Graph graph = (Graph) object;
            return graph.name.equals(this.name);
        }

        /**
         * Called when the server owner decides to opt-out of BukkitMetrics
         * while the server is running.
         */
        protected void onOptOut() {
        }
    }

    /**
     * Interface used to collect custom data for a plugin.
     */
    public abstract static class Plotter {

        /**
         * The plot's name.
         */
        private final String name;

        /**
         * Construct a plotter with the default plot name.
         */
        public Plotter() {
            this("Default");
        }

        /**
         * Construct a plotter with a specific plot name.
         * 
         * @param name The name of the plotter to use, which will show up on the
         *        website
         */
        public Plotter(final String name) {
            this.name = name;
        }

        /**
         * Get the current value for the plotted point. Since this function
         * defers to an external function it may or may not return immediately
         * thus cannot be guaranteed to be thread friendly or safe. This
         * function can be called from any thread so care should be taken when
         * accessing resources that need to be synchronized.
         * 
         * @return The current value for the point to be plotted.
         */
        public abstract int getValue();

        /**
         * Get the column name for the plotted point.
         * 
         * @return The plotted point's column name
         */
        public String getColumnName() {
            return this.name;
        }

        /**
         * Called after the website graphs have been updated.
         */
        public void reset() {
        }

        @Override
        public int hashCode() {
            return getColumnName().hashCode();
        }

        @Override
        public boolean equals(final Object object) {
            if (!(object instanceof Plotter)) {
                return false;
            }

            final Plotter plotter = (Plotter) object;
            return plotter.name.equals(this.name) && plotter.getValue() == getValue();
        }
    }
}
