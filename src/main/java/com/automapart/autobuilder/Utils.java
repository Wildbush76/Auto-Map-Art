package com.automapart.autobuilder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;

public class Utils {
    private Utils() {
    }

    public static double distanceTo(MinecraftClient mc, Vec3i pos) {
        return Math.sqrt(
                Math.pow(mc.player.getX() - pos.getX(), 2) + Math.pow(mc.player.getY() - pos.getY(), 2) +
                        Math.pow(mc.player.getZ() - pos.getZ(), 2));

    }
}
