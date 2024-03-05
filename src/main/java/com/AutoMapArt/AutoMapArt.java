package com.AutoMapArt;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoMapArt implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("auto-map-art");
	public static AutoMapArt INSTANCE;
	// public static MinecraftClient mc;
	public static MinecraftClient mc;
	public static ModSettings modSettings;

	@Override
	public void onInitialize() {
		if (INSTANCE == null)
			INSTANCE = this;
		mc = MinecraftClient.getInstance();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess,
				environment) -> dispatcher.register(literal("foo")
						.requires(source -> source.isExecutedByPlayer())
						.then(argument("blockPosition", BlockPosArgumentType.blockPos())
								.then(argument("blockType", BlockPredicateArgumentType.blockPredicate(registryAccess))
										.executes(context -> {
											context.getSource().sendFeedback(() -> Text.literal("Foo to you too"),
													false);

											return 1;
										})))));

		modSettings = new ModSettings();
		modSettings.load();
	}
}
