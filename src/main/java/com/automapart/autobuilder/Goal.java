package com.automapart.autobuilder;

import java.util.List;

import com.automapart.AutoMapArtManager;
import com.automapart.autobuilder.pathing.Astar;
import com.automapart.autobuilder.utils.Utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Goal {
    private BlockPos finalGoal;
    private List<BlockPos> currentPath;
    private MinecraftClient mc;

    private BlockPos currentTargetPosition;

    public Goal(BlockPos pos) {
        mc = MinecraftClient.getInstance();
        finalGoal = pos;
    }

    public boolean travel() {
        if (currentPath.isEmpty()) {
            if (mc.player.getBlockPos().equals(finalGoal)) {
                Utils.setPressed(mc.options.forwardKey, false);
                Utils.setPressed(mc.options.jumpKey, false);
                return true;
            } else {
                pathToGoal();
                currentTargetPosition = currentPath.remove(0);
            }
        }
        if (currentTargetPosition == mc.player.getBlockPos()) {
            currentTargetPosition = currentPath.remove(0);
        }

        Utils.lookTowardBlock(currentTargetPosition, false);
        Utils.setPressed(mc.options.forwardKey, true);

        boolean shouldJump = currentTargetPosition.getY() - mc.player.getBlockPos().getY() > 0;
        Utils.setPressed(mc.options.jumpKey, shouldJump);
        return false;

    }

    private boolean pathToGoal() {
        double range = AutoMapArtManager.getInstance().getModSettings().getInteractRange();
        BlockPos currentGoal;
        if (mc.player.getBlockPos().isWithinDistance(finalGoal, range)) {
            currentGoal = finalGoal;
        } else {
            currentGoal = createOffsetGoal(finalGoal, (int) range);
        }

        switch (Astar.findPath(mc.player.getBlockPos(), currentGoal, mc, currentPath)) {
            case FULL_PATH:
                finalGoal = currentGoal;
                return true;
            case PARTIAL_PATH:
                return true;
            case FAILED:
                error("Pathing failed");
                break;
            case TIMEOUT:
                error("Pathing timed out");
                break;
        }
        return false;
    }

    private BlockPos createOffsetGoal(BlockPos currentGoal, int range) {
        if (!mc.world.isChunkLoaded(currentGoal.getX(), currentGoal.getZ())) {
            return currentGoal;
        }

        for (int y = -range; y <= range; y++) {
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    Vec3i offsetGoal = currentGoal.add(x, y, z);
                    if (offsetGoal.isWithinDistance(currentGoal, range) && Utils.canWalk(currentGoal)
                            && !new Vec3i(x, y, z).equals(Vec3i.ZERO)) {
                        return new BlockPos(offsetGoal);
                    }
                }
            }
        }
        return null;
    }

    private void error(String message) {
        Utils.error(message);
        AutoMapArtManager.getInstance().getAutoBuilder().pause();
    }

}
