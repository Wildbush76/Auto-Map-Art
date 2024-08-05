package com.automapart.autobuilder;

import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import java.util.HashMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import com.automapart.AutoMapArt;

public class SchematicManager {
    private SchematicPlacement currentSchematic;
    private Map<Block, ArrayList<BlockPos>> remainingLocations;

    private ArrayList<BlockPos> currentLocations;
    private Block currentBlockType;

    public SchematicManager(SchematicPlacement placement) {
        currentSchematic = placement;
        remainingLocations = new HashMap<>();

        LitematicaSchematic schematic = placement.getSchematic();

        Map<String, BlockPos> regionPositions = schematic.getAreaPositions();

        for (Entry<String, BlockPos> region : regionPositions.entrySet()) {

            LitematicaBlockStateContainer blockStateContainer = schematic.getSubRegionContainer(region.getKey());
            if (blockStateContainer == null) {
                continue;
            }
            loadPositions(blockStateContainer, remainingLocations,
                    placement.getOrigin().add(region.getValue()));
        }
    }

    private void loadPositions(LitematicaBlockStateContainer blockStateContainer,
            Map<Block, ArrayList<BlockPos>> locations, Vec3i position) {// TODO improve this generation
        Vec3i size = blockStateContainer.getSize();
        boolean forward = false;

        double roughXStep = (int) (AutoMapArt.getInstance().modSettings.getInteractRange() * 2);

        for (int y = 0; y < size.getY(); y++) {
            for (int roughX = 0; roughX < size.getX(); roughX += roughXStep) {
                for (int z = 0; z < size.getX(); z++) {
                    int realZ = (forward) ? z : size.getZ() - z - 1;

                    for (int x = roughX; x < roughX + roughXStep; x++) {
                        if (x >= size.getX())
                            break;
                        BlockState blockState = blockStateContainer.get(x, y, realZ);
                        checkAndAddBlock(locations, blockState, new BlockPos(x, y, realZ).add(position));

                    }
                }
            }
        }
    }

    private void checkAndAddBlock(Map<Block, ArrayList<BlockPos>> locations, BlockState blockState, BlockPos blockPos) {
        Block block = blockState.getBlock();

        if (blockState.isAir() || AutoMapArt.getInstance().modSettings.getBlackList().contains(block)) {
            return;
        }

        if (locations.containsKey(block)) {
            locations.get(block).add(blockPos);
        } else {
            ArrayList<BlockPos> blockLocations = new ArrayList<>();
            blockLocations.add(blockPos);
            locations.put(block, blockLocations);
        }
    }

    public BlockPos getNextBlockPos() {
        if (currentLocations.isEmpty()) {
            Entry<Block, ArrayList<BlockPos>> maxEntry = null;
            int max = 0;

            for (Entry<Block, ArrayList<BlockPos>> entry : remainingLocations.entrySet()) {
                if (entry.getValue().size() > max) {
                    maxEntry = entry;
                    max = entry.getValue().size();
                }
            }

            if (maxEntry == null) {
                return null;
            }
            currentLocations = maxEntry.getValue();
            currentBlockType = maxEntry.getKey();
        }

        return currentLocations.remove(0);
    }

    public Block getCurrentBlockType() {
        return currentBlockType;
    }

    public SchematicPlacement getCurrentPlacement() {
        return currentSchematic;
    }
}
