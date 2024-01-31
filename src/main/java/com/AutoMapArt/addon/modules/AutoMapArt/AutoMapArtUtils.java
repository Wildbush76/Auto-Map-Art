package com.AutoMapArt.addon.modules.AutoMapArt;

import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;

public class AutoMapArtUtils {
        public static void lookTowardBlock(MinecraftClient mc, BlockPos pos) {
                lookTowardBlock(mc, pos, false);
        }

        public static void lookTowardBlock(MinecraftClient mc, BlockPos pos, boolean pitch) {
                if (pitch) {
                        mc.player.setPitch((float) Rotations.getPitch(pos));
                }
                float yaw = (float) Rotations.getYaw(pos);

                mc.player.setYaw(yaw);
        }

        public static boolean canWalk(MinecraftClient mc, int x, int y, int z) {
                return canWalk(mc, new BlockPos(x, y, z));
        }

        public static boolean canWalk(MinecraftClient mc, BlockPos blockPos) {
                BlockState blockState = mc.world.getBlockState(blockPos);
                if (blockState.canPathfindThrough(null, null, NavigationType.LAND)) {
                        if (!mc.world.getBlockState(blockPos.add(0, -1, 0)).canPathfindThrough(null, null,
                                        NavigationType.LAND)) {
                                if (mc.world.getBlockState(blockPos.add(0, 1, 0)).isAir()) {
                                        return true;
                                }
                        }
                }

                return false;
        }

        public static void setPressed(KeyBinding key, boolean pressed) {
                key.setPressed(pressed);
                Input.setKeyState(key, pressed);
        }

}
