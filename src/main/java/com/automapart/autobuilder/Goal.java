package com.automapart.autobuilder;

import net.minecraft.util.math.BlockPos;

public class Goal {
    private BlockPos goal;

    public Goal(BlockPos pos) {
        goal = pos;
    }

    public boolean travel() {

        return true;
    }
}
