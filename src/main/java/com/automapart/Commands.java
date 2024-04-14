package com.automapart;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Map;

import com.automapart.autobuilder.AutoBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class Commands {
    public static void registerCommands(ModSettings settings) {
        testCommand(settings);
        setResouceLocation(settings);
        enable();
    }

    private static void testCommand(ModSettings settings) {
        CommandRegistrationCallback.EVENT.register((dispacher, registryAccess,
                environment) -> dispacher.register(literal("testcommand")
                        .executes(context -> {
                            Map<Item, BlockPos> positions = settings.getResourcePositions();
                            context.getSource().sendFeedback(() -> Text.literal("Locations "), false);
                            for (Map.Entry<Item, BlockPos> item : positions.entrySet()) {
                                context.getSource().sendFeedback(() -> Text.literal(
                                        item.getKey().getName().toString() + " at " + item.getValue().toShortString()),
                                        false);
                            }
                            return 1;
                        })));
    }

    private static void setResouceLocation(ModSettings settings) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess,
                environment) -> dispatcher.register(literal("setResourceLocation")
                        .requires(source -> source.isExecutedByPlayer())
                        .then(argument("blockPosition", BlockPosArgumentType.blockPos())
                                .then(argument("blockType",
                                        BlockStateArgumentType.blockState(registryAccess))
                                        .executes(context -> {
                                            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "blockPosition");
                                            BlockStateArgument blockType = BlockStateArgumentType.getBlockState(context,
                                                    "blockType");
                                            settings.addResourcePosition(pos,
                                                    blockType.getBlockState().getBlock().asItem());
                                            context.getSource().sendFeedback(() -> Text.literal("Settings location"),
                                                    false);

                                            return 1;
                                        })))));
    }

    private static void enable() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess,
                environment) -> dispatcher.register(literal("enable")
                        .requires(source -> source.isExecutedByPlayer())
                        .executes(context -> {
                            AutoBuilder.getInstance().enable();
                            return 1;
                        })));
    }

    private Commands() {

    }
}
