package com.automapart.autobuilder;

import com.automapart.AutoMapArt;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Utils {
    private static final MinecraftClient mc = AutoMapArt.getInstance().mc;

    public static void turnOffKeys() {
        setPressed(mc.options.forwardKey, false);
        setPressed(mc.options.jumpKey, false);
    }

    public static double distanceTo(Vec3i pos) {
        return Math.sqrt(
                getFlatDistanceSquared(pos) +
                        Math.pow(mc.player.getZ() - pos.getZ(), 2));

    }

    public static Integer findItem(Item item) {
        return findItem(item, 0, mc.player.getInventory().size());
    }

    public static Integer findItem(Item item, int start, int end) {
        if (mc == null) {
            return null;
        }
        for (int i = start; i < end; i++) {
            Item itemInSlot = mc.player.getInventory().getStack(i).getItem();
            if (item.equals(itemInSlot)) {
                return i;
            }
        }
        return null;
    }

    public static void debug(String message) {
        info(message);
    }

    public static void error(String message) {
        // make it send a chat error
    }

    public static void info(String message) {
        // make it send an info
    }

    public static void lookTowardBlock(MinecraftClient mc, BlockPos pos) {
        lookTowardBlock(mc, pos, false);
    }

    public static void lookTowardBlock(MinecraftClient mc, BlockPos pos, boolean pitch) {
        if (pitch) {
            mc.player.setPitch((float) getPitch(pos));
        }
        float yaw = (float) getYaw(pos);

        mc.player.setYaw(yaw);
    }

    public static double getFlatDistance(Vec3i pos) {
        return Math.sqrt(getFlatDistanceSquared(pos));
    }

    public static double getFlatDistanceSquared(Vec3i pos) {
        return Math.pow(mc.player.getX() - pos.getX(), 2) + Math.pow(mc.player.getY() - pos.getY(), 2);
    }

    public static double getPitch(Vec3i pos) {
        return Math.atan2(pos.getZ() - mc.player.getZ(), getFlatDistance(pos));
    }

    public static double getYaw(Vec3i pos) {
        return Math.atan2(pos.getY() - mc.player.getY(), pos.getX() - mc.player.getX());
    }

    public static boolean canWalk(int x, int y, int z) {
        return canWalk(new BlockPos(x, y, z));
    }

    public static boolean canWalk(BlockPos blockPos) {
        BlockState blockState = mc.world.getBlockState(blockPos);

        if (!blockState.canPathfindThrough(null, null, NavigationType.LAND))
            return false;

        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).canPathfindThrough(null, null,
                NavigationType.LAND))
            return false;
        return mc.world.getBlockState(blockPos.add(0, 1, 0)).isAir();
    }

    public static void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        // Input.setKeyState(key, pressed);
    }

    public static void moveToHand(int slot) {
        int to = mc.player.getInventory().selectedSlot;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, to, SlotActionType.SWAP,
                mc.player);
    }

    public static void shiftClick(int slot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, slot, SlotActionType.QUICK_MOVE,
                mc.player);
    }

    public static Integer findEmptySlot() {
        int end = mc.player.getInventory().size();
        mc.player.getInventory().swapSlotWithHotbar(end);
        for (int i = 0; i < end; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }
        return null;
    }

    public static boolean canUpdate() {
        return mc != null && mc.world != null && mc.player != null;
    }

    private Utils() {
    }

}
