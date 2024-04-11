package com.automapart.autobuilder.utils;

import com.automapart.AutoMapArt;

import net.minecraft.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

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

    public static void debug(String message) {
        info(message);
    }

    public static void error(String message) {
        // make it send a chat error
    }

    public static void info(String message) {
        // make it send an info
    }

    public static void lookTowardBlock(BlockPos pos) {
        lookTowardBlock(pos, false);
    }

    public static void lookTowardBlock(BlockPos pos, boolean pitch) {
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

    public static boolean canUpdate() {
        return mc != null && mc.world != null && mc.player != null;
    }

    public static boolean placeBlock(BlockPos blockPos, boolean rotate, Block block) {
        if (!canPlaceBlock(blockPos, block))
            return false;

        Vec3d hitPos = blockPos.toCenterPos();
        Direction side = getSide(blockPos);
        BlockPos neighbor;

        if (side == null) {
            side = Direction.UP;
            neighbor = blockPos;
        } else {
            neighbor = blockPos.offset(side);
            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        BlockHitResult blockHitResult = new BlockHitResult(hitPos, side.getOpposite(), neighbor, false);

        if (rotate) {
            lookTowardBlock(blockPos);
        }
        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);

        if (result.shouldSwingHand()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        return true;

    }

    public static boolean breakBlock(BlockPos blockPos) {
        BlockPos pos = blockPos instanceof BlockPos.Mutable ? new BlockPos(blockPos) : blockPos;
        if (mc.interactionManager.isBreakingBlock())
            mc.interactionManager.updateBlockBreakingProgress(pos, getSide(blockPos));
        else
            mc.interactionManager.attackBlock(pos, getSide(blockPos));

        mc.player.swingHand(Hand.MAIN_HAND);
        return true;

    }

    public static boolean canBeClicked(Block block) {
        return block instanceof CraftingTableBlock
                || block instanceof AnvilBlock
                || block instanceof ButtonBlock
                || block instanceof AbstractPressurePlateBlock
                || block instanceof BlockWithEntity
                || block instanceof BedBlock
                || block instanceof FenceGateBlock
                || block instanceof DoorBlock
                || block instanceof NoteBlock
                || block instanceof TrapdoorBlock;
    }

    public static Direction getSide(BlockPos blockPos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = mc.world.getBlockState(neighbor);

            if (state.isAir() || canBeClicked(state.getBlock()) || !state.getFluidState().isEmpty())
                continue;
            return side;
        }

        return null;
    }

    public static boolean canPlaceBlock(BlockPos blockPos, Block block) {
        if (!World.isValid(blockPos))
            return false;
        if (!mc.world.getBlockState(blockPos).isReplaceable())
            return false;
        return mc.world.canPlace(block.getDefaultState(), blockPos, ShapeContext.absent());
    }

    private Utils() {
    }

}
