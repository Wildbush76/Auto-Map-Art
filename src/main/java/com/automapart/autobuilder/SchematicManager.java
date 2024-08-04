package com.automapart.autobuilder;

import fi.dy.masa.litematica.Litematica;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import java.util.HashMap;

import java.util.Map;
import java.util.ArrayList;
import com.automapart.AutoMapArt;

public class SchematicManager {
    private SchematicPlacement currentSchematic;
    private Map<Block, ArrayList<BlockPos>> remainingLocations;

    private long[] currentLocations;

    public SchematicManager(SchematicPlacement placement) {
        currentSchematic = placement;
        remainingLocations = new HashMap<>();

        LitematicaSchematic schematic = placement.getSchematic();

        Map<String, BlockPos> regionPositions = schematic.getAreaPositions();

        for (String regionName : regionPositions.keySet()) {

            LitematicaBlockStateContainer blockStateContainer = schematic.getSubRegionContainer(regionName);
            if (blockStateContainer == null) {
                continue;
            }
            loadPositions(blockStateContainer, remainingLocations,
                    placement.getOrigin().add(regionPositions.get(regionName)));
        }
    }

    private void loadPositions(LitematicaBlockStateContainer blockStateContainer,
            Map<Block, ArrayList<BlockPos>> locations, Vec3i Position) {// TODO improve this generation
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
                        Block block = blockState.getBlock();
                        BlockPos blockPosition = new BlockPos(x, y, realZ);

                        blockPosition = blockPosition.add(Position);

                        if (locations.containsKey(block)) {
                            locations.get(block).add(blockPosition);
                        } else {
                            ArrayList<BlockPos> blockLocations = new ArrayList<>();
                            blockLocations.add(blockPosition);
                            locations.put(block, blockLocations);
                        }

                    }
                }
            }
        }
    }
}
