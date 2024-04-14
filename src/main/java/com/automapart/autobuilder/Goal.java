package com.automapart.autobuilder;

import com.automapart.AutoMapArt;
import com.automapart.ModSettings;
import com.automapart.autobuilder.AutoBuilder.stage;
import com.automapart.autobuilder.utils.Utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class Goal extends BlockPos {
    private PathRunner runner;
    private static ModSettings settings = AutoMapArt.getInstance().modSettings;

    public Goal(MinecraftClient mc, BlockPos blockPos) {
        super(blockPos);
        double distance = Utils.distanceTo(this);
        if (distance < 1) {
            BlockPos alternateGoal = searchForPlacementLocation();
            if (alternateGoal != null) {
                runner = new PathRunner(mc, alternateGoal);

                AutoMapArt.LOGGER.debug("doing an alternate goal");
                return;
            } else {
                Utils.info("Pathing failed");
                AutoBuilder.getInstance().pause();
            }
        }
        runner = new PathRunner(mc, blockPos);

    }

    public boolean travelTo() {
        double distance = Utils.distanceTo(this);

        if (distance < settings.getInteractRange()
                && (AutoBuilder.getInstance().getCurrentStage() != stage.BUILDING || distance > 1)) {
            Utils.turnOffKeys();
            return true;

        }
        if (runner.isActive()) {
            return runner.run();
        } else {
            Utils.info("Error while pathfinding");
            AutoBuilder.getInstance().pause();
            return false;
        }
    }

    public BlockPos[] getPath() {
        if (runner.isActive()) {
            return runner.getPath();
        }
        return new BlockPos[0];
    }

    private BlockPos searchForPlacementLocation() {
        int range = (int) settings.getInteractRange();
        for (int x = -range; x < range; x++) {
            for (int z = -range; z < range; z++) {
                for (int y = -3; y < 3; y++) {
                    BlockPos pos = this.add(x, y, z);
                    double dist = Utils.distanceTo(pos);

                    if (Utils.canWalk(pos) && dist > 3) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}
