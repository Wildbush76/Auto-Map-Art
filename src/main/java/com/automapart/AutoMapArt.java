package com.automapart;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoMapArt implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("auto-map-art");
	private static AutoMapArt instance;
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	public final ModSettings modSettings = new ModSettings();

	public AutoMapArt() {
		setInstance(this);
	}

	private static void setInstance(AutoMapArt autoMapArt) {
		instance = autoMapArt;
	}

	public static AutoMapArt getInstance() {
		return instance;
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispacher, registryAccess,
				environment) -> dispacher.register(literal("testcommand")
						.executes(context -> {
							BlockPos position = modSettings.getResourcePosition(Items.TNT);
							if (position != null) {
								context.getSource().sendFeedback(() -> Text.literal(position.toShortString()), false);
							} else {
								context.getSource().sendFeedback(() -> Text.literal("location not found"), false);
							}
							return 1;
						})));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess,
				environment) -> dispatcher.register(literal("foo")
						.requires(source -> source.isExecutedByPlayer())
						.then(argument("blockPosition", BlockPosArgumentType.blockPos()))
						.then(argument("blockType", BlockStateArgumentType.blockState(registryAccess))
								.executes(context -> {
									BlockPos pos = BlockPosArgumentType.getBlockPos(context, "blockPosition");
									BlockStateArgument blockType = BlockStateArgumentType.getBlockState(context,
											"blockType");
									modSettings.addResourcePosition(pos, blockType.getBlockState().getBlock().asItem());
									context.getSource().sendFeedback(() -> Text.literal("Settings location"),
											false);

									return 1;
								}))));

		modSettings.load();
		Runtime.getRuntime().addShutdownHook(new Thread(
				modSettings::save));
	}
}
