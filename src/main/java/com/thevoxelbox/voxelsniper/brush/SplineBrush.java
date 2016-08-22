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
package com.thevoxelbox.voxelsniper.brush;

import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeData;
import org.spongepowered.api.effect.particle.ParticleType.Block;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;

/**
 * FOR ANY BRUSH THAT USES A SPLINE, EXTEND THAT BRUSH FROM THIS BRUSH!!! That way, the spline calculations are already there. Also, the UI for
 * the splines will be included.
 *
 * @author psanker
 */
public class SplineBrush extends PerformBrush
{
    private final ArrayList<Block> endPts = new ArrayList<Block>();
    private final ArrayList<Block> ctrlPts = new ArrayList<Block>();
//    protected ArrayList<Point> spline = new ArrayList<Point>();
    protected boolean set;
    protected boolean ctrl;
    protected String[] sparams = {"ss", "sc", "clear"};

    // @Spongify
    public SplineBrush()
    {
        this.setName("Spline");
    }

    public final void addToSet(final SnipeData v, final boolean ep, Block targetBlock)
    {
//        if (ep)
//        {
//            if (this.endPts.contains(targetBlock) || this.endPts.size() == 2)
//            {
//                return;
//            }
//
//            this.endPts.add(targetBlock);
//            v.sendMessage(TextColors.GRAY + "Added block " + TextColors.RED + "(" + targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ() + ") " + TextColors.GRAY + "to endpoint selection");
//            return;
//        }
//
//        if (this.ctrlPts.contains(targetBlock) || this.ctrlPts.size() == 2)
//        {
//            return;
//        }
//
//        this.ctrlPts.add(targetBlock);
//        v.sendMessage(TextColors.GRAY + "Added block " + TextColors.RED + "(" + targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ() + ") " + TextColors.GRAY
//                + "to control point selection");
    }

    public final void removeFromSet(final SnipeData v, final boolean ep, Block targetBlock)
    {
//        if (ep)
//        {
//            if (!this.endPts.contains(targetBlock))
//            {
//                v.sendMessage(TextColors.RED + "That block is not in the endpoint selection set.");
//                return;
//            }
//
//            this.endPts.add(targetBlock);
//            v.sendMessage(TextColors.GRAY + "Removed block " + TextColors.RED + "(" + targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ() + ") " + TextColors.GRAY
//                    + "from endpoint selection");
//            return;
//        }
//
//        if (!this.ctrlPts.contains(targetBlock))
//        {
//            v.sendMessage(TextColors.RED + "That block is not in the control point selection set.");
//            return;
//        }
//
//        this.ctrlPts.remove(targetBlock);
//        v.sendMessage(TextColors.GRAY + "Removed block " + TextColors.RED + "(" + targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ() + ") " + TextColors.GRAY
//                + "from control point selection");
    }

//    public final boolean spline(final Point start, final Point end, final Point c1, final Point c2, final SnipeData v)
//    {
//        this.spline.clear();
//
//        try
//        {
//            final Point c = (c1.subtract(start)).multiply(3);
//            final Point b = ((c2.subtract(c1)).multiply(3)).subtract(c);
//            final Point a = ((end.subtract(start)).subtract(c)).subtract(b);
//
//            for (double t = 0.0; t < 1.0; t += 0.01)
//            {
//                final int px = (int) Math.round((a.getX() * (t * t * t)) + (b.getX() * (t * t)) + (c.getX() * t) + this.endPts.get(0).getX());
//                final int py = (int) Math.round((a.getY() * (t * t * t)) + (b.getY() * (t * t)) + (c.getY() * t) + this.endPts.get(0).getY());
//                final int pz = (int) Math.round((a.getZ() * (t * t * t)) + (b.getZ() * (t * t)) + (c.getZ() * t) + this.endPts.get(0).getZ());
//
//                if (!this.spline.contains(new Point(px, py, pz)))
//                {
//                    this.spline.add(new Point(px, py, pz));
//                }
//            }
//
//            return true;
//        }
//        catch (final Exception exception)
//        {
//            v.sendMessage(TextColors.RED + "Not enough points selected; " + this.endPts.size() + " endpoints, " + this.ctrlPts.size() + " control points");
//            return false;
//        }
//    }

    protected final void render(final SnipeData v)
    {
//        if (this.spline.isEmpty())
//        {
//            return;
//        }
//
//        for (final Point point : this.spline)
//        {
//            this.current.perform(this.clampY(point.getX(), point.getY(), point.getZ()));
//        }
//
//        v.owner().storeUndo(this.current.getUndo());
    }

    @Override
    protected final void arrow(final SnipeData v)
    {
//        if (this.set)
//        {
//            this.removeFromSet(v, true, this.getTargetBlock());
//        }
//        else if (this.ctrl)
//        {
//            this.removeFromSet(v, false, this.getTargetBlock());
//        }
    }

    protected final void clear(final SnipeData v)
    {
//        this.spline.clear();
//        this.ctrlPts.clear();
//        this.endPts.clear();
//        v.sendMessage(TextColors.GRAY + "Bezier curve cleared.");
    }

    @Override
    protected final void powder(final SnipeData v)
    {
//        if (this.set)
//        {
//            this.addToSet(v, true, this.getTargetBlock());
//        }
//        if (this.ctrl)
//        {
//            this.addToSet(v, false, this.getTargetBlock());
//        }
    }

    @Override
    public final void info(final Message vm)
    {
        vm.brushName(this.getName());

        if (this.set)
        {
            vm.custom(TextColors.GRAY , "Endpoint selection mode ENABLED.");
        }
        else if (this.ctrl)
        {
            vm.custom(TextColors.GRAY , "Control point selection mode ENABLED.");
        }
        else
        {
            vm.custom(TextColors.AQUA , "No selection mode enabled.");
        }
    }

    @Override
    public final void parameters(final String[] par, final com.thevoxelbox.voxelsniper.SnipeData v)
    {
        for (int i = 1; i < par.length; i++)
        {
            if (par[i].equalsIgnoreCase("info"))
            {
                v.sendMessage(TextColors.GOLD , "Spline brush parameters");
                v.sendMessage(TextColors.AQUA , "ss: Enable endpoint selection mode for desired curve");
                v.sendMessage(TextColors.AQUA , "sc: Enable control point selection mode for desired curve");
                v.sendMessage(TextColors.AQUA , "clear: Clear out the curve selection");
                v.sendMessage(TextColors.AQUA , "ren: Render curve from control points");
                return;
            }
            if (par[i].equalsIgnoreCase("sc"))
            {
                if (!this.ctrl)
                {
                    this.set = false;
                    this.ctrl = true;
                    v.sendMessage(TextColors.GRAY , "Control point selection mode ENABLED.");
                }
                else
                {
                    this.ctrl = false;
                    v.sendMessage(TextColors.AQUA , "Control point selection mode disabled.");
                }
            }
            else if (par[i].equalsIgnoreCase("ss"))
            {
                if (!this.set)
                {
                    this.set = true;
                    this.ctrl = false;
                    v.sendMessage(TextColors.GRAY , "Endpoint selection mode ENABLED.");
                }
                else
                {
                    this.set = false;
                    v.sendMessage(TextColors.AQUA , "Endpoint selection mode disabled.");
                }
            }
            else if (par[i].equalsIgnoreCase("clear"))
            {
                this.clear(v);
            }
            else if (par[i].equalsIgnoreCase("ren"))
            {
//                if (this.spline(new Point(this.endPts.get(0)), new Point(this.endPts.get(1)), new Point(this.ctrlPts.get(0)), new Point(this.ctrlPts.get(1)), v))
//                {
//                    this.render(v);
//                }
            }
            else
            {
                v.sendMessage(TextColors.RED , "Invalid brush parameters! use the info parameter to display parameter info.");
            }
        }
    }

    // Vector class for splines
//    protected class Point
//    {
//        private int x;
//        private int y;
//        private int z;
//
//        public Point(final Block b)
//        {
//            this.setX(b.getX());
//            this.setY(b.getY());
//            this.setZ(b.getZ());
//        }
//
//        public Point(final int x, final int y, final int z)
//        {
//            this.setX(x);
//            this.setY(y);
//            this.setZ(z);
//        }
//
//        public final Point add(final Point p)
//        {
//            return new Point(this.getX() + p.getX(), this.getY() + p.getY(), this.getZ() + p.getZ());
//        }
//
//        public final Point multiply(final int scalar)
//        {
//            return new Point(this.getX() * scalar, this.getY() * scalar, this.getZ() * scalar);
//        }
//
//        public final Point subtract(final Point p)
//        {
//            return new Point(this.getX() - p.getX(), this.getY() - p.getY(), this.getZ() - p.getZ());
//        }
//
//        public int getX()
//        {
//            return x;
//        }
//
//        public void setX(int x)
//        {
//            this.x = x;
//        }
//
//        public int getY()
//        {
//            return y;
//        }
//
//        public void setY(int y)
//        {
//            this.y = y;
//        }
//
//        public int getZ()
//        {
//            return z;
//        }
//
//        public void setZ(int z)
//        {
//            this.z = z;
//        }
//    }

    @Override
    public String getPermissionNode()
    {
        return "voxelsniper.brush.spline";
    }
}