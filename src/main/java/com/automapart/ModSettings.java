package com.automapart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.BlockPos;

public class ModSettings {

    public static final File FOLDER = FabricLoader.getInstance().getGameDir().resolve(AutoMapArt.MOD_ID).toFile();
    private File file;
    private Map<Item, BlockPos> resourcePositions;
    private double interactRange;
    private int placeDelay;
    private int grabItemDelay;
    private BlockPos wasteLocation;
    private ArrayList<Block> blackList;
    private boolean rotateToPlace;

    public ModSettings() {
        file = new File(FOLDER, AutoMapArt.MOD_ID + ".nbt");
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
        wasteLocation = BlockPos.fromLong(data.getLong("wasteLocation"));
        rotateToPlace = data.getBoolean("rotateToPlace");
        loadBlockPositions(data);
        loadBlackList(data);
        AutoMapArt.LOGGER.info("Successfully loaded settings");
    }

    public void save() {
        AutoMapArt.LOGGER.info("Saving settings....");
        NbtCompound data = new NbtCompound();
        data.putInt("placeDelay", placeDelay);
        data.putInt("grabItemDelay", grabItemDelay);
        data.putDouble("interactRange", interactRange);
        data.putLong("wasteLocation", wasteLocation.asLong());
        data.putBoolean("rotateToPlace", rotateToPlace);

        saveBlockPositions(data);
        saveBlackList(data);

        try {
            file.getParentFile().mkdirs();
            NbtIo.write(data, file);
            AutoMapArt.LOGGER.info("Sucessfully saved settings");

        } catch (IOException ioException) {
            AutoMapArt.LOGGER.error(ioException.getMessage());
        }
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

    public boolean getRotateToPlace() {
        return rotateToPlace;
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

    public List<Block> getBlackList() {
        return blackList;
    }

    public BlockPos getWasteLocation() {
        return wasteLocation;
    }

    private void loadBlackList(NbtCompound data) {
        int[] blockIds = data.getIntArray("blackList");
        blackList = new ArrayList<>(blockIds.length);
        for (int id : blockIds) {
            blackList.add(Block.getBlockFromItem(Item.byRawId(id)));
        }
    }

    private void loadBlockPositions(NbtCompound data) {
        int[] itemIds = data.getIntArray("itemIds");
        long[] blockPositions = data.getLongArray("blockPositions");
        resourcePositions = new HashMap<>();
        for (int i = 0; i < itemIds.length; i++) {
            resourcePositions.put(Item.byRawId(itemIds[i]), BlockPos.fromLong(blockPositions[i]));
        }
    }

    private void saveBlockPositions(NbtCompound data) {
        int[] itemIds = new int[resourcePositions.size()];
        long[] blockPositions = new long[resourcePositions.size()];

        int i = 0;
        for (Map.Entry<Item, BlockPos> entry : resourcePositions.entrySet()) {
            itemIds[i] = Item.getRawId(entry.getKey());
            blockPositions[i] = entry.getValue().asLong();
            i++;
        }

        data.putLongArray("blockPositions", blockPositions);
        data.putIntArray("itemIds", itemIds);

    }

    private void saveBlackList(NbtCompound data) {
        int[] blackListIds = new int[blackList.size()];
        for (int i = 0; i < blackListIds.length; i++) {
            blackListIds[i] = Item.getRawId(blackList.get(i).asItem());
        }
        data.putIntArray("blackList", blackListIds);
    }

    private void setDefaultSettings() {
        AutoMapArt.LOGGER.info("Loading default settings");
        placeDelay = 3;
        interactRange = 5;
        grabItemDelay = 1;
        resourcePositions = new HashMap<>();
        blackList = new ArrayList<>();
        rotateToPlace = true;
    }
}
