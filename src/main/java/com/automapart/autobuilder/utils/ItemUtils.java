package com.automapart.autobuilder.utils;

import com.automapart.AutoMapArt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.SlotActionType;

public class ItemUtils {
    private static final MinecraftClient mc = AutoMapArt.getInstance().mc;
    public static final int HOTBAR_START_SLOT = 0;
    public static final int HOTBAR_END_SLOT = 8;

    public static final int MAIN_START_SLOT = 9;
    public static final int MAIN_END_SLOT = 35;

    private ItemUtils() {

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

    public static int indexToId(int i) {
        ScreenHandler handler = mc.player.currentScreenHandler;

        if (handler instanceof PlayerScreenHandler)
            return survivalInventory(i);
        if (handler instanceof GenericContainerScreenHandler genericContainerScreenHandler)
            return genericContainer(i, genericContainerScreenHandler.getRows());
        if (handler instanceof Generic3x3ContainerScreenHandler)
            return generic3x3(i);
        if (handler instanceof HopperScreenHandler)
            return hopper(i);
        if (handler instanceof ShulkerBoxScreenHandler)
            return genericContainer(i, 3);
        return -1;
    }

    private static int generic3x3(int i) {
        if (isHotbar(i))
            return 36 + i;
        if (isMain(i))
            return i;
        return -1;
    }

    private static int hopper(int i) {
        if (isHotbar(i))
            return 32 + i;
        if (isMain(i))
            return 5 + (i - 9);
        return -1;
    }

    private static int survivalInventory(int i) {
        if (isHotbar(i))
            return 36 + i;
        return i;
    }

    private static int genericContainer(int i, int rows) {
        if (isHotbar(i))
            return (rows + 3) * 9 + i;
        if (isMain(i))
            return rows * 9 + (i - 9);
        return -1;
    }

    public static boolean isHotbar(int i) {
        return i >= HOTBAR_START_SLOT && i <= HOTBAR_END_SLOT;
    }

    public static boolean isMain(int i) {
        return i >= MAIN_START_SLOT && i <= MAIN_END_SLOT;
    }

}
