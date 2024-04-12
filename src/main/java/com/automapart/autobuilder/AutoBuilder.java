package com.automapart.autobuilder;

import java.util.ArrayList;
import java.util.Map;

import com.automapart.AutoMapArt;
import com.automapart.ModSettings;
import com.automapart.autobuilder.utils.Executer;
import com.automapart.autobuilder.utils.ItemUtils;
import com.automapart.autobuilder.utils.Utils;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class AutoBuilder {
    public enum stage {
        BUILDING,
        COLLECTING_MATERIALS, // travelling to collect
        DUMPING_WASTE, // travelling to dump
        WAITING
    }

    private static ModSettings settings = AutoMapArt.getInstance().modSettings;
    private static AutoBuilder instance = new AutoBuilder();
    private static final MinecraftClient mc = AutoMapArt.getInstance().mc;
    private static final int MAX_BLOCK_PLACE_ATTEMPTS = 10;

    public static AutoBuilder getInstance() {
        if (instance == null) {
            setInstance();

        }
        return instance;
    }

    private static void setInstance() {
        instance = new AutoBuilder();
    }

    private Goal goal;
    private SchematicPlacement currentSchematic;
    private stage currentStage;
    private boolean blockPlaced;

    private ArrayList<Block> completedBlockTypes = new ArrayList<>();
    private ArrayList<BlockPos> currentBlocks = new ArrayList<>();

    private Block currentBlockType;

    private int placeTimer;

    private int currentFailedAttempts = 0;

    private boolean enabled = false;

    private boolean paused = false;

    private AutoBuilder() {

    }

    public void onTick() {
        if (!enabled || !paused) {
            return;
        }
        switch (currentStage) {
            case BUILDING:
                build();
                endBuildTick();
                break;
            case COLLECTING_MATERIALS:
            case DUMPING_WASTE:
                travelToChest();
                break;
            case WAITING:
                // do nothing
                break;

        }
    }

    public void onInventory(InventoryS2CPacket packet) {
        if (paused || !enabled)
            return;
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (packet.getSyncId() != handler.syncId) {
            return;
        }
        if (currentStage != stage.COLLECTING_MATERIALS && currentStage != stage.DUMPING_WASTE)
            return;

        switch (currentStage) {
            case COLLECTING_MATERIALS:
                Executer.execute(() -> getResources(handler));
                break;
            case DUMPING_WASTE:
                Executer.execute(this::dumpWaste);
                break;
            default:
                break;
        }
        currentStage = stage.WAITING;
    }

    public stage getCurrentStage() {
        return currentStage;
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
            double dist = Utils.distanceTo(schematicPlacement.getOrigin());
            if (dist < distance) {
                distance = dist;
                closestPlacement = schematicPlacement;
            }
        }
        if (closestPlacement == null) {
            Utils.error("No schematic loaded");
            disable();
            return;
        }

        currentSchematic = closestPlacement;
        getNextBlocks();
        currentStage = stage.BUILDING; // good as we dont need to dump waste of course
        paused = false;
        blockPlaced = false;
    }

    public void skipCurrentBlock() {
        currentBlocks.clear();
        getNextBlocks();
    }

    public void disable() {
        currentSchematic = null;
        currentBlockType = null;
        currentBlocks.clear();
        goal = null;
        currentStage = stage.BUILDING;
        completedBlockTypes.clear();
        Utils.turnOffKeys();
        enabled = false;
        paused = false;
    }

    private void dumpWaste() { // basically the opposite of the thing below lol
        Item itemToFind = completedBlockTypes.get(completedBlockTypes.size() - 1).asItem();

        Integer result = ItemUtils.findItem(itemToFind);

        while (result != null) {
            try {
                Thread.sleep(settings.getGrabItemDelay());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            if (mc.currentScreen == null || !Utils.canUpdate())
                break;

            ItemUtils.shiftClick(result);
            result = ItemUtils.findItem(itemToFind);
        }
        currentStage = stage.BUILDING;
        assignNextBlockGoal();
        mc.player.closeHandledScreen();

    }

    private void getResources(ScreenHandler handler) {
        int stacksNeeded = (int) Math.ceil(currentBlocks.size() / 64d);
        int stacksGathered = 0;
        boolean gotResources = false;
        for (int i = 0; i < ItemUtils.indexToId(9); i++) {
            if (handler.getSlot(i).hasStack()
                    && handler.getSlot(i).getStack().isOf(currentBlockType.asItem())) {

                try {
                    Thread.sleep(settings.getGrabItemDelay());
                } catch (InterruptedException e) {
                    AutoMapArt.LOGGER.error("Interuppted Thread - ", e);
                    Thread.currentThread().interrupt();
                    return;
                }
                if (mc.currentScreen == null || !Utils.canUpdate() || ItemUtils.findEmptySlot() == null
                        || stacksGathered == stacksNeeded)
                    break;
                ItemUtils.shiftClick(i);
                gotResources = true;
                stacksGathered++;
            }
        }
        if (!gotResources) {
            Utils.error(("failed to grab resources"));
            pause();
        }
        currentStage = stage.BUILDING;
        assignNextBlockGoal(); // this uh might actually cause it to skip
        mc.player.closeHandledScreen();// this should close the chest

    }

    private void endBuildTick() {
        if (blockPlaced) {
            BlockState state = mc.world.getBlockState(goal);
            if (state.getBlock().equals(currentBlockType)) {
                assignNextBlockGoal();
            }
            blockPlaced = false;
        }
    }

    private boolean checkIfShouldTravel() {
        if (goal == null || blockAlreadyThere(goal)) {
            assignNextBlockGoal();
            return false;
        } else if (mc.world.isChunkLoaded(goal.getX(), goal.getZ())) {
            Integer findItemResult = ItemUtils.findItem(currentBlockType.asItem());

            if (findItemResult == null) {
                currentBlocks.add(0, goal);
                setCollectMaterials();
                Utils.debug("out of " + currentBlockType.asItem().getName().getString());
                return false;
            }

        }
        return true;
    }

    private void build() {
        if (placeTimer > 0) {
            placeTimer--;
            return;
        }
        if (!checkIfShouldTravel()) {
            return;
        }
        if (goal.travelTo() && placeTimer == 0) {
            tryToPlaceBlock();
        }
    }

    private void tryToPlaceBlock() {
        BlockState targetedBlock = mc.world.getBlockState(goal);
        if (!targetedBlock.isReplaceable() && !targetedBlock.getBlock().equals(currentBlockType)) {
            Utils.breakBlock(goal);
            return;
        }

        Integer result = ItemUtils.findItem(currentBlockType.asItem());
        if (result != null && !ItemUtils.isMain(result)) {
            ItemUtils.moveToHand(result);
        }
        if (settings.getRotateToPlace())
            Utils.lookTowardBlock(goal, true);
        boolean placeResult = Utils.placeBlock(goal, settings.getRotateToPlace(), currentBlockType);
        if (!placeResult) {
            currentFailedAttempts++;
            if (currentFailedAttempts == MAX_BLOCK_PLACE_ATTEMPTS) {
                currentFailedAttempts = 0; // lol
                currentBlocks.add(goal); // to try again later ig lol
                assignNextBlockGoal();
            }
        } else {
            currentFailedAttempts = 0;
            blockPlaced = true;
        }
        placeTimer = settings.getPlaceDelay();
    }

    private void setCollectMaterials() {
        Map<Item, BlockPos> materialPositions = settings.getResourcePositions();

        if (!materialPositions.containsKey(currentBlockType.asItem())) {
            Utils.error("Material chest location not specified");
            pause();
            return;
        }

        BlockPos chestLocation = materialPositions.get(currentBlockType.asItem());
        goal = new Goal(chestLocation);
        currentStage = stage.COLLECTING_MATERIALS;
    }

    private void travelToChest() {
        if (goal.travelTo()) {
            if (!mc.world.getBlockState(goal).hasBlockEntity()) {
                Utils.error("chest not present at chest location");
                pause();
                return;
            }
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                    new BlockHitResult(goal.toCenterPos(), Direction.UP, goal, true));
        }
    }

    private void turnOffKeys() {
        Utils.setPressed(mc.options.forwardKey, false);
        Utils.setPressed(mc.options.jumpKey, false);
    }

    /**
     * assigns the next block goal from currentBlocks
     */
    private void assignNextBlockGoal() {
        boolean assignedBlock = false;
        while (!assignedBlock) {
            if (currentBlocks.isEmpty()) {
                if (!getNextBlocks()) {
                    Utils.info("FINISIHED BUILDING");
                    turnOffKeys();
                    disable();
                    return;
                }
                if (currentStage != stage.BUILDING) {
                    return;
                }
            }
            if (currentBlocks.isEmpty()) {
                BlockPos nextGoal = currentBlocks.remove(0);
                if (!blockAlreadyThere(nextGoal)) {
                    goal = new Goal(nextGoal);
                    assignedBlock = true;
                }

            }
        }
    }

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
            Utils.debug("Completed " + currentBlockType.asItem().getName().getString());
            completedBlockTypes.add(currentBlockType);
            Integer result = ItemUtils.findItem(currentBlockType.asItem());
            if (result == null) {
                setDumpWasteGoal();
            }
            currentBlockType = null;
        }
        LitematicaSchematic schematic = currentSchematic.getSchematic();
        Map<String, BlockPos> regionPositions = schematic.getAreaPositions();
        for (Map.Entry<String, BlockPos> regionName : regionPositions.entrySet()) {
            addBlocksFromRegion(regionName, schematic);

        }
        return currentBlockType == null;
    }

    private void addBlocksFromRegion(Map.Entry<String, BlockPos> regionName, LitematicaSchematic schematic) {
        LitematicaBlockStateContainer blockStateContainer = schematic
                .getSubRegionContainer(regionName.getKey());
        if (blockStateContainer == null) {
            return;
        }
        Vec3i size = blockStateContainer.getSize();
        boolean forward = false;
        for (int y = 0; y < size.getY(); y++) {
            for (int roughX = 0; roughX < size.getX(); roughX += settings.getInteractRange() * 2) {
                forward = !forward;
                for (int z = 0; z < size.getZ(); z++) {
                    int realZ = (forward) ? z : size.getZ() - 1 - z;
                    int end = (int) Math.min(roughX + settings.getInteractRange() * 2, size.getX());
                    for (int x = roughX; x < end; x++) {
                        checkAndAddBlock(blockStateContainer.get(x, y, realZ), new BlockPos(x, y, realZ).add(
                                regionName.getValue()));
                    }
                }
            }
        }
    }

    private void checkAndAddBlock(BlockState blockState, BlockPos blockPosition) {
        if (shouldBePlaced(blockState)) {
            return;
        }

        if (currentBlockType == null) {
            currentBlockType = blockState.getBlock();
        }
        if (blockState.getBlock().equals(currentBlockType)) {
            blockPosition = blockPosition
                    .add(currentSchematic.getOrigin());
            if (!blockAlreadyThere(blockPosition)) {
                currentBlocks.add(blockPosition);
            }
        }
    }

    private void setDumpWasteGoal() {
        goal = new Goal(settings.getWasteLocation());
        currentStage = stage.DUMPING_WASTE;
    }

    private boolean shouldBePlaced(BlockState blockState) {
        if (blockState.isAir())
            return false;
        if (isCompletedBlockType(blockState.getBlock()))
            return false;
        return settings.getBlackList().contains(blockState.getBlock());

    }

    private boolean isCompletedBlockType(Block block) {
        return completedBlockTypes.contains(block);
    }
}
