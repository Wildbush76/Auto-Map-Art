package com.automapart;

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
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	public final ModSettings modSettings = new ModSettings();

	private static void setInstance(AutoMapArt autoMapArt) {
		INSTANCE = autoMapArt;
	}

	@Override
	public void onInitialize() {
		if (INSTANCE == null)
			setInstance(INSTANCE);

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

		modSettings.load();
	}
}
