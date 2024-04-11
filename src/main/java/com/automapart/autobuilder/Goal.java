package com.automapart.autobuilder;

import com.automapart.AutoMapArt;
import com.automapart.ModSettings;
import com.automapart.autobuilder.AutoBuilder.stage;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class Goal extends BlockPos {
    private PathRunner runner;
    private static MinecraftClient mc = AutoMapArt.getInstance().mc;
    private static ModSettings settings = AutoMapArt.getInstance().modSettings;

    public Goal(BlockPos blockPos) {
        super(blockPos);
        double distance = Utils.distanceTo(this);
        if (distance < 1) {
            BlockPos alternateGoal = searchForPlacementLocation();
            if (alternateGoal != null) {
                runner = new PathRunner(mc, alternateGoal);
                AutoMapArt.LOGGER.debug("doing an alternate goal " + alternateGoal.toString());
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

        if (distance < settings.getInteractRange()) {
            if (AutoBuilder.getInstance().getCurrentStage() != stage.BUILDING || distance > 1) {
                Utils.turnOffKeys();
                return true;
            }
        }
        if (runner.active) {
            return runner.run();
        } else {
            Utils.info("Error while pathfinding");
            AutoBuilder.getInstance().pause();
            return false;
        }
    }

    public BlockPos[] getPath() {
        if (runner.active) {
            return runner.getPath();
        }
        return null;
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
