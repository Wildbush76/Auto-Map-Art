package com.AutoMapArt.addon.modules.AutoMapArt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AutoMapArt.addon.Addon;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AutoMapArt extends Module {
        public class Goal extends BlockPos {
                private PathRunner runner;

                public Goal(BlockPos blockPos) {
                        super(blockPos);
                        double distance = PlayerUtils.distanceTo(this);
                        if (distance < 1) {
                                BlockPos alternateGoal = searchForPlacementLocation();
                                if (alternateGoal != null) {
                                        runner = new PathRunner(mc, alternateGoal);
                                        System.out.println("doing an alternate goal " + alternateGoal.toString());
                                        return;
                                } else {
                                        info("Pathing failed");
                                        Pause();
                                }
                        }
                        runner = new PathRunner(mc, blockPos);

                }

                public boolean TravelTo() {
                        double distance = PlayerUtils.distanceTo(this);

                        if (distance < PlacementRange.get()) {
                                if (currentStage != stage.BUILDING || distance > 1) {
                                        turnOffKeys();
                                        return true;
                                }
                        }
                        if (runner.active) {
                                return runner.run();
                        } else {
                                info("Error while pathfinding");
                                Pause();
                                return false;
                        }
                }

                public BlockPos[] getPath() {
                        if (runner.active) {
                                return runner.getPath();
                        }
                        return null;
                }

                private BlockPos searchForPlacementLocation() {
                        int range = PlacementRange.get();
                        for (int x = -range; x < range; x++) {
                                for (int z = -range; z < range; z++) {
                                        for (int y = -3; y < 3; y++) {
                                                BlockPos pos = this.add(x, y, z);
                                                double dist = PlayerUtils.distanceTo(pos);

                                                if (AutoMapArtUtils.canWalk(mc, pos) && dist > 3) {
                                                        return pos;
                                                }
                                        }
                                }
                        }
                        return null;
                }
        }

        private enum stage {
                BUILDING,
                COLLECTING_MATERIALS, // travelling to collect
                DUMPING_WASTE, // travelling to dump
                WAITING
        }

        private final SettingGroup sgGeneral = settings.getDefaultGroup();
        private final SettingGroup chestLocations = settings.createGroup("Material locations");
        private final int MAX_BLOCK_PLACE_ATTEMPTS = 10;
        private ArrayList<BlockPos> currentBlocks = new ArrayList<BlockPos>();

        private Goal goal;
        private stage currentStage = stage.BUILDING;
        private Block currentBlockType;
        private SchematicPlacement currentSchematic;
        private ArrayList<Block> completedBlockTypes = new ArrayList<Block>();

        private int placeTimer;

        private int currentFailedAttempts = 0;

        private boolean paused;
        private boolean blockPlaced = false;
        private final Block[] defaultBlackListed = { Blocks.COBBLESTONE };
        private final Setting<AutoMapArtBlockData> defaultBlockConfig = sgGeneral
                        .add(new GenericSetting.Builder<AutoMapArtBlockData>()
                                        .name("default block location (just ignore this)")
                                        .description("default location to get blocks")
                                        .defaultValue(new AutoMapArtBlockData(new BlockPos(0, 0, 0)))
                                        .visible(() -> false)
                                        .build()

                        );

        private final Setting<Map<Block, AutoMapArtBlockData>> materialLocations = sgGeneral
                        .add(new BlockDataSetting.Builder<AutoMapArtBlockData>()
                                        .name("materialLocations")
                                        .description("location of where to restock materials")
                                        .defaultData(defaultBlockConfig)
                                        .build());
        private final Setting<Integer> PlacementDelay = sgGeneral.add(new IntSetting.Builder()
                        .name("Place delay (ticks)")
                        .description("Delay between placing each block")
                        .defaultValue(5)
                        .build());

        private final Setting<Integer> PlacementRange = sgGeneral.add(new IntSetting.Builder()
                        .name("Place Range (blocks)")
                        .description("Maxium distance that it will attempt to place a block")
                        .defaultValue(5)
                        .build());
        private final Setting<Integer> grabItemDelay = sgGeneral.add(new IntSetting.Builder()
                        .name("Grab Item delay (miliseconds)")
                        .description("Delay between grabing each item from a chest")
                        .defaultValue(0)
                        .build());
        private final Setting<BlockPos> wasteLocation = chestLocations.add(new BlockPosSetting.Builder()
                        .name("Waste chest location")
                        .description("(only in this version because it is)")
                        .build());
        private final Setting<Boolean> rotateToPlace = sgGeneral.add(new BoolSetting.Builder()
                        .name("Rotate Towards")
                        .description("Wether or not to rotate toward the block your placing")
                        .defaultValue(true)
                        .build());
        private final Setting<Boolean> verbose = sgGeneral.add(new BoolSetting.Builder()
                        .name("Verbose")
                        .description("Wether or not to output info")
                        .defaultValue(true)
                        .build());
        private final Setting<Boolean> forceAutoLog = sgGeneral.add(new BoolSetting.Builder()
                        .name("Force Auto Log")
                        .description("Enables auto log when Auto Map Art is activated")
                        .defaultValue(true)
                        .build());
        private final Setting<Boolean> pauseOnKillAura = sgGeneral.add(new BoolSetting.Builder()
                        .name("Pause on killaura")
                        .description("Pause building when kill aura is attacking")
                        .defaultValue(true)
                        .build());

        private final Setting<List<Block>> blackListedBlocks = sgGeneral.add(new BlockListSetting.Builder()
                        .name("BlackListed Blocks")
                        .description("Blocks to not build")
                        .defaultValue(defaultBlackListed)
                        .build());

        public AutoMapArt() {
                super(Addon.CATEGORY, "Auto Map Art",
                                "Automacally builds the current litamtica schematic, only meant for flat carpet maps");
        }

        @Override
        public void onActivate() {
                SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();
                SchematicPlacement closestPlacement = null;
                Double distance = Double.MAX_VALUE;
                for (SchematicPlacement schematicPlacement : manager.getAllSchematicsPlacements()) {
                        double dist = PlayerUtils.distanceTo(schematicPlacement.getOrigin());
                        if (dist < distance) {
                                distance = dist;
                                closestPlacement = schematicPlacement;
                        }
                }
                if (closestPlacement == null) {
                        error("No schematic loaded");
                        toggle();
                        return;
                }

                currentSchematic = closestPlacement;
                GetNextBlocks();
                currentStage = stage.BUILDING; // good as we dont need to dump waste of course
                paused = false;
                blockPlaced = false;

                if (forceAutoLog.get()) {
                        AutoLog log = Modules.get().get(AutoLog.class);
                        if (!log.isActive()) {
                                log.toggle();
                        }
                }
        }

        public void skipCurrentBlock() {
                currentBlocks.clear();
                GetNextBlocks();
        }

        public boolean isPaused() {
                return paused;
        }

        public void unpause() {
                info("unpuasing");
                paused = false;
        }

        public void Pause() {
                turnOffKeys();
                info("Pausing, run command .unpause to unpause");
                paused = true;
        }

        @Override
        public void onDeactivate() {
                currentSchematic = null;
                currentBlockType = null;
                currentBlocks.clear();
                goal = null;
                goal = null;
                currentStage = stage.BUILDING;
                completedBlockTypes.clear();
                turnOffKeys();
        }

        private void debug(String message) {
                if (verbose.get())
                        super.info(message);

        }

        @EventHandler(priority = EventPriority.HIGH)
        private void onTick(TickEvent.Pre event) {
                if (paused)
                        return;
                else if ((pauseOnKillAura.get() && Modules.get().get(KillAura.class).getTarget() != null)) {
                        turnOffKeys();
                        return;
                }

                KillAura kill = Modules.get().get(KillAura.class);
                if (kill.getTarget() != null) {
                        return;
                }
                switch (currentStage) {
                        case BUILDING:
                                Build();
                                break;
                        case COLLECTING_MATERIALS:
                        case DUMPING_WASTE:
                                TravelToChest();
                                break;
                        case WAITING:
                                // do nothing
                                break;

                }
        }

        @EventHandler
        private void postTick(TickEvent.Post event) {
                if (currentStage == stage.BUILDING && blockPlaced) {
                        BlockState state = mc.world.getBlockState(goal);
                        if (state.getBlock().equals(currentBlockType)) {
                                assignNextBlockGoal();
                        }
                        blockPlaced = false;
                }
        }

        /**
         * moves waste material from players inventory to a chest
         *
         * @param handler the screen handler for the inventory event
         */
        private void dumpWaste(ScreenHandler handler) { // basically the opposite of the thing below lol
                Item itemToFind = completedBlockTypes.get(completedBlockTypes.size() - 1).asItem();

                FindItemResult item = InvUtils.find(itemToFind);

                while (item.found()) {
                        try {
                                Thread.sleep(grabItemDelay.get());
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                        if (mc.currentScreen == null || !Utils.canUpdate())
                                break;
                        InvUtils.shiftClick().slot(item.slot());
                        item = InvUtils.find(itemToFind);
                }
                currentStage = stage.BUILDING;
                assignNextBlockGoal();
                mc.player.closeHandledScreen();

        }

        /**
         * moves resources from a chest to the players inventory
         *
         * @param handler the screene handler for inventory event
         */
        private void getResources(ScreenHandler handler) {
                int stacksNeeded = (int) Math.ceil(currentBlocks.size() / 64d);
                int stacksGathered = 0;
                boolean gotResources = false;
                for (int i = 0; i < SlotUtils.indexToId(SlotUtils.MAIN_START); i++) {
                        if (!handler.getSlot(i).hasStack()
                                        || !handler.getSlot(i).getStack().isOf(currentBlockType.asItem()))
                                continue;
                        if (!InvUtils.findEmpty().found())
                                break;
                        try {
                                Thread.sleep(grabItemDelay.get());
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                        if (mc.currentScreen == null || !Utils.canUpdate())
                                break;
                        InvUtils.shiftClick().slotId(i);
                        gotResources = true;
                        stacksGathered++;
                        if (stacksGathered == stacksNeeded) {
                                break;
                        }
                }
                if (!gotResources) {
                        info("failed to grab resources");
                        Pause();
                }
                currentStage = stage.BUILDING;
                assignNextBlockGoal(); // this uh might actually cause it to skip
                mc.player.closeHandledScreen();// this should close the chest

        }

        @EventHandler
        private void onRender(Render3DEvent event) {// todo make a render showing the current stage
                if (goal != null) {
                        event.renderer.box(new BlockPos(goal), new Color(100, 0, 0), new Color(255, 0, 0),
                                        ShapeMode.Lines, 0);

                        BlockPos[] path = goal.getPath();
                        if (path == null)
                                return;
                        for (int i = 0; i < path.length - 1; i++) {
                                Vec3d startPos = path[i].toCenterPos();
                                Vec3d endPos = path[i + 1].toCenterPos();

                                event.renderer.line(startPos.getX(), startPos.getY(), startPos.getZ(), endPos.getX(),
                                                endPos.getY(), endPos.getZ(), new Color(200, 0, 0));
                        }
                }

        }

        @EventHandler
        private void onInventory(InventoryEvent event) {
                if (paused)
                        return;
                ScreenHandler handler = mc.player.currentScreenHandler;
                if (event.packet.getSyncId() != handler.syncId) {
                        return;
                }
                if (currentStage != stage.COLLECTING_MATERIALS && currentStage != stage.DUMPING_WASTE)
                        return;

                switch (currentStage) {
                        case COLLECTING_MATERIALS: // dude lol
                                MeteorExecutor.execute(() -> getResources(handler));
                                break;
                        case DUMPING_WASTE:
                                MeteorExecutor.execute(() -> dumpWaste(handler));
                                break;
                        default:
                                break;
                }
                currentStage = stage.WAITING;
        }

        /**
         * Gets the next block type
         * If not the first time getting time it will set the dumb waste goal
         *
         * @return returns true if more block positions are set
         */
        private boolean GetNextBlocks() { // gets the next block type once done with the current one

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
                                for (int roughX = 0; roughX < size.getX(); roughX += PlacementRange.get() * 2) {
                                        forward = !forward;
                                        for (int z = 0; z < size.getZ(); z++) {
                                                int realZ;
                                                if (forward) {
                                                        realZ = z;
                                                } else {
                                                        realZ = size.getZ() - 1 - z;
                                                }
                                                for (int x = roughX; x < roughX + PlacementRange.get() * 2; x++) {
                                                        if (x >= size.getX())
                                                                break;

                                                        BlockState blockState = blockStateContainer.get(x, y, realZ);
                                                        if (blockState.isAir()
                                                                        || IsCompletedBlockType(blockState.getBlock())
                                                                        || blackListedBlocks.get().contains(
                                                                                        blockState.getBlock())) {
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
                if (currentBlockType == null) {
                        return false;
                }
                return true;
        }

        /**
         * The main method for buiding
         */
        private void Build() {

                if (placeTimer > 0) {
                        placeTimer--;
                        return;
                }

                if (goal == null || blockAlreadyThere(goal)) {
                        assignNextBlockGoal();
                        return;
                } else if (mc.world.isChunkLoaded(goal.getX(), goal.getZ())) {
                        FindItemResult findItemResult = InvUtils.find(currentBlockType.asItem());
                        if (!findItemResult.found()) {
                                currentBlocks.add(0, goal);
                                SetCollectMaterials();
                                debug("out of " + currentBlockType.asItem().getName().getString());
                                return;
                        }

                }
                if (goal.TravelTo() && placeTimer == 0) {
                        BlockState targetedBlock = mc.world.getBlockState(goal);
                        if (!targetedBlock.isReplaceable() && !targetedBlock.getBlock().equals(currentBlockType)) {
                                BlockUtils.breakBlock(goal, true);
                                return;
                        }
                        FindItemResult findItemResult = InvUtils.find(currentBlockType.asItem());
                        if (!findItemResult.isHotbar() && findItemResult.found()) {
                                InvUtils.move().fromMain(findItemResult.slot() - 9).toHotbar(1);
                        }
                        if (rotateToPlace.get())
                                AutoMapArtUtils.lookTowardBlock(mc, goal, true);
                        boolean result = BlockUtils.place(goal, findItemResult, rotateToPlace.get(), 100,
                                        true, true,
                                        false);
                        if (!result) {
                                currentFailedAttempts++;
                                if (currentFailedAttempts == MAX_BLOCK_PLACE_ATTEMPTS) {
                                        currentFailedAttempts = 0; // lol
                                        currentBlocks.add(goal); // to try again later ig lol
                                        // blockPlaced = true;
                                        assignNextBlockGoal();
                                        // goal = null;

                                }
                        } else {
                                currentFailedAttempts = 0;
                                // assignNextBlockGoal();
                                blockPlaced = true;
                        }
                        placeTimer = PlacementDelay.get();
                }
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

        private void turnOffKeys() {
                AutoMapArtUtils.setPressed(mc.options.forwardKey, false);
                AutoMapArtUtils.setPressed(mc.options.jumpKey, false);
        }

        /**
         * assigns the next block goal from currentBlocks
         */
        private void assignNextBlockGoal() {
                boolean assignedBlock = false;
                while (!assignedBlock) {// this way there is no stack overflow chance
                        if (currentBlocks.size() == 0) {
                                if (!GetNextBlocks()) {
                                        info("FINISIHED BUILDING");
                                        turnOffKeys();
                                        toggle();
                                        return;
                                }
                                if (currentStage != stage.BUILDING) {
                                        return;
                                }

                        }
                        if (currentBlocks.size() == 0)
                                continue;
                        BlockPos nextGoal = currentBlocks.remove(0);
                        if (blockAlreadyThere(nextGoal)) {
                                continue;
                        }
                        goal = new Goal(nextGoal);
                        assignedBlock = true;
                }
        }

        /**
         * Sets the goal for where to dump waste and switches the stage
         */

        private void SetDumpWasteGoal() { // its not a landfill but yknow
                goal = new Goal(wasteLocation.get());
                currentStage = stage.DUMPING_WASTE;
        }

        /**
         * Sets the goal for collecting materials based off whats needed and swaps the
         * stage
         */
        private void SetCollectMaterials() {
                Map<Block, AutoMapArtBlockData> materialPositions = materialLocations.get();

                if (!materialPositions.containsKey(currentBlockType)) {
                        error("Material chest location not specified");
                        Pause();
                        return;
                }

                BlockPos chestLocation = materialPositions.get(currentBlockType).blockPos;
                goal = new Goal(chestLocation);
                currentStage = stage.COLLECTING_MATERIALS;
        }

        /**
         * Travels to goal and interacts with the block goal
         */
        private void TravelToChest() {
                if (goal.TravelTo()) {
                        if (!mc.world.getBlockState(goal).hasBlockEntity()) {
                                error("chest not present at chest location");
                                Pause();
                                return;
                        }
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                                        new BlockHitResult(goal.toCenterPos(), Direction.UP, goal, true));
                }
        }

        /**
         * checks if a block is in the list of completed types
         * you knerd
         * 
         * @param block the block to check
         * @return returns true if block is in completed blocks
         */
        private boolean IsCompletedBlockType(Block block) {
                for (Block compeltedType : completedBlockTypes) {
                        if (compeltedType.getName().equals(block.getName())) {
                                return true;
                        }
                }
                return false;
        }

}
