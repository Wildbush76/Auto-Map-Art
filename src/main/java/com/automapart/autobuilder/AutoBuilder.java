package com.automapart.autobuilder;

import java.util.ArrayList;
import java.util.Map;

import com.automapart.AutoMapArt;
import com.automapart.ModSettings;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class AutoBuilder {
    private static ModSettings settings = AutoMapArt.getInstance().modSettings;
    private static AutoBuilder instance = new AutoBuilder();
    private SchematicPlacement currentSchematic;
    private stage currentStage;
    private boolean blockPlaced;
    private static final MinecraftClient mc = AutoMapArt.getInstance().mc;
    private ArrayList<Block> completedBlockTypes = new ArrayList<>();
    private ArrayList<BlockPos> currentBlocks = new ArrayList<>();
    private Block currentBlockType;
    private final Block[] blackListed = { Blocks.COBBLESTONE };// TODO add something in the settings manager

    private enum stage {
        BUILDING,
        COLLECTING_MATERIALS, // travelling to collect
        DUMPING_WASTE, // travelling to dump
        WAITING
    }

    public static AutoBuilder getInstance() {
        if (instance == null) {
            setInstance();

        }
        return instance;
    }

    private static void setInstance() {
        instance = new AutoBuilder();
    }

    private boolean enabled = false;

    private boolean paused = false;

    private AutoBuilder() {

    }

    public void onTick() {
        if (!enabled || !paused) {
            return;
        }

        // more stuff here
    }

    public void onPostTick() {
        if (!enabled || !paused) {
            return;
        }

        // stuff goes here i think
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    public void enable() {
        enabled = true;
        paused = false;

        SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();
        SchematicPlacement closestPlacement = null;
        Double distance = Double.MAX_VALUE;
        for (SchematicPlacement schematicPlacement : manager.getAllSchematicsPlacements()) {
            double dist = Utils.distanceTo(mc, schematicPlacement.getOrigin());
            if (dist < distance) {
                distance = dist;
                closestPlacement = schematicPlacement;
            }
        }
        if (closestPlacement == null) {
            error("No schematic loaded");
            disable();
            return;
        }

        currentSchematic = closestPlacement;
        getNextBlocks();
        currentStage = stage.BUILDING; // good as we dont need to dump waste of course
        paused = false;
        blockPlaced = false;
    }

    private void error(String message) {
        // make it send a chat error
    }

    private void info(String message) {
        // make it send an info
    }

    /**
     * checks if a block is already in its correct state
     * 
     * @param goal position of block to check
     * @return returns true if its in the correct state
     */
    private boolean blockAlreadyThere(Vec3i goal) {
        return !mc.world.isChunkLoaded(goal.getX(), goal.getZ())
                || mc.world.getBlockState(new BlockPos(goal)).getBlock().equals(currentBlockType);
    }

    /**
     * Gets the next block type
     * If not the first time getting time it will set the dumb waste goal
     *
     * @return returns true if more block positions are set
     */
    private boolean getNextBlocks() { // gets the next block type once done with the current one

        if (currentBlockType != null) {
            debug("Completed " + currentBlockType.asItem().getName().getString());
            completedBlockTypes.add(currentBlockType);
            FindItemResult findItemResult = InvUtils.find(currentBlockType.asItem());
            if (findItemResult.found()) {
                SetDumpWasteGoal();
            }
            currentBlockType = null;
        }
        LitematicaSchematic schematic = currentSchematic.getSchematic();
        Map<String, BlockPos> regionPositions = schematic.getAreaPositions();
        for (String regionName : regionPositions.keySet()) {
            LitematicaBlockStateContainer blockStateContainer = schematic
                    .getSubRegionContainer(regionName);

            if (blockStateContainer == null) {
                continue;
            }
            Vec3i size = blockStateContainer.getSize();
            boolean forward = false;
            for (int y = 0; y < size.getY(); y++) {
                for (int roughX = 0; roughX < size.getX(); roughX += settings.getInteractRange() * 2) {
                    forward = !forward;
                    for (int z = 0; z < size.getZ(); z++) {
                        int realZ;
                        if (forward) {
                            realZ = z;
                        } else {
                            realZ = size.getZ() - 1 - z;
                        }
                        for (int x = roughX; x < roughX + settings.getInteractRange() * 2; x++) {// TODO fix this logic
                            if (x >= size.getX())
                                break;

                            BlockState blockState = blockStateContainer.get(x, y, realZ);
                            if (shouldBePlaced(blockState)) {
                                continue;
                            }

                            if (currentBlockType == null) {
                                currentBlockType = blockState.getBlock();
                            }
                            if (blockState.getBlock().equals(currentBlockType)) {
                                BlockPos blockPosition = new BlockPos(x, y, realZ);
                                blockPosition = blockPosition
                                        .add(regionPositions.get(
                                                regionName));
                                blockPosition = blockPosition
                                        .add(currentSchematic.getOrigin());
                                if (!blockAlreadyThere(blockPosition)) {
                                    currentBlocks.add(blockPosition);
                                }
                            }

                        }
                    }
                }
            }

        }
        return currentBlockType == null;
    }

    private void debug(String message) {
        info(message);
    }

    private boolean shouldBePlaced(BlockState blockState) {
        if (blockState.isAir())
            return false;
        if (IsCompletedBlockType(blockState.getBlock()))
            return false;
        for (Block block : blackListed) {
            if (block.equals(blockState.getBlock()))
                return false;
        }
        return true;
    }

    private boolean IsCompletedBlockType(Block block) {
        return false;// Add this
    }

    public void disable() {
        enabled = false;
        paused = false;
    }
}
