package com.automapart.autobuilder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class PathRunner {
    private final int PATH_FOLLOWING_TIMEOUT = 200;// 200 ticks before stop trying to go to next node

    private MinecraftClient mc;
    private BlockPos[] path;
    private BlockPos goal;
    private Astar astar;
    private int timeoutCounter = 0;
    public boolean active = true;

    private int current = 0;

    public PathRunner(MinecraftClient mc, BlockPos goal) {
        this.mc = mc;
        this.goal = goal;
        astar = new Astar(mc);
        path = generatePath();
    }

    public BlockPos getGoal() {
        return goal;
    }

    private BlockPos[] generatePath() {
        BlockPos[] path = astar.findPath(mc.player.getBlockPos(), goal);
        if (path == null) {
            active = false;
        }
        current = 0;
        return path;
    }

    public boolean isActive() {
        return active;
    }

    public boolean run() {
        if (!active)
            return false;

        timeoutCounter++;
        if (timeoutCounter == PATH_FOLLOWING_TIMEOUT) {
            generatePath();
            timeoutCounter = 0;
            return false;
        }

        Utils.setPressed(mc.options.forwardKey, true);
        if (mc.player.getBlockPos().equals(path[current])) {
            timeoutCounter = 0;
            current++;
            if (current >= path.length) {// just in case
                return checkDone();
            }
        }
        if (path[current].getY() - mc.player.getBlockPos().getY() > 0) {
            Utils.setPressed(mc.options.jumpKey, true);
        } else {
            Utils.setPressed(mc.options.jumpKey, false);
        }
        Utils.lookTowardBlock(mc, path[current]);
        return false;
    }

    private boolean checkDone() {
        if (mc.player.getBlockPos().equals(goal)) {
            Utils.setPressed(mc.options.forwardKey, false);
            Utils.setPressed(mc.options.jumpKey, false);
            return true;
        }
        path = generatePath();
        return false;
    }

    public BlockPos[] getPath() {
        return path;
    }
}
