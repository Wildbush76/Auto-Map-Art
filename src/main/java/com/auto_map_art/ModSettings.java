package com.auto_map_art;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.BlockPos;

public class ModSettings {
    public static final String MOD_ID = "auto-map-art";
    public static final File FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();
    private File file;
    private Map<Item, BlockPos> resourcePositions;
    private double interactRange;
    private int placeDelay;
    private int grabItemDelay;

    public ModSettings() {
        file = new File(FOLDER, MOD_ID + ".nbt");
    }

    public void load() {
        if (file == null || !file.exists()) {
            setDefaultSettings();
            return;
        }
        NbtCompound data;
        try {
            data = NbtIo.read(file);
        } catch (IOException ioException) {
            AutoMapArt.LOGGER.error(ioException.getMessage());
            setDefaultSettings();
            return;
        } catch (CrashException crashException) {
            AutoMapArt.LOGGER.error("issues occured");
            setDefaultSettings();
            return;
        }

        placeDelay = data.getInt("placeDelay");
        interactRange = data.getDouble("interactRange");
        grabItemDelay = data.getInt("grabItemDelay");

        int[] itemIds = data.getIntArray("itemIds");
        long[] blockPositions = data.getLongArray("blockPositions");

        for (int i = 0; i < itemIds.length; i++) {
            resourcePositions.put(Item.byRawId(itemIds[i]), BlockPos.fromLong(blockPositions[i]));
        }

    }

    public void save() {
        AutoMapArt.LOGGER.info("Saving settings....");
        NbtCompound data = new NbtCompound();
        data.putInt("placeDelay", placeDelay);
        data.putInt("grabItemDelay", grabItemDelay);
        data.putDouble("interactRange", interactRange);

        int[] itemIds = new int[resourcePositions.size()];
        long[] blockPositions = new long[resourcePositions.size()];
        data.putLongArray("blockPositions", blockPositions);
        data.putIntArray("itemIds", itemIds);

        try {
            file.getParentFile().mkdirs();
            NbtIo.write(data, file);
            AutoMapArt.LOGGER.info("Sucessfully saved settings");

        } catch (IOException ioException) {
            AutoMapArt.LOGGER.error(ioException.getMessage());
        }
    }

    private void setDefaultSettings() {
        placeDelay = 5;
        interactRange = 5;
        grabItemDelay = 1;
        resourcePositions = new HashMap<>();
    }

    public double getInteractRange() {
        return interactRange;
    }

    public int getPlaceDelay() {
        return placeDelay;
    }

    public int getGrabItemDelay() {
        return grabItemDelay;
    }

    public Map<Item, BlockPos> getResourcePositions() {
        return resourcePositions;
    }

    public BlockPos getResourcePosition(Item item) {
        return resourcePositions.get(item);
    }

    public void addResourcePosition(BlockPos blockPos, Item item) {
        resourcePositions.put(item, blockPos);
    }

    public void setPlaceDelay(int delay) {
        placeDelay = delay;
    }

    public void setGrabItemDelay(int delay) {
        grabItemDelay = delay;
    }

    public void setInteractRange(int range) {
        interactRange = range;
    }
}
