package com.automapart;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class AutoMapArt implements ModInitializer {
	public static final String MOD_ID = "auto-map-art";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static AutoMapArt instance = getInstance();

	public static AutoMapArt getInstance() {
		if (instance == null) {
			instance = new AutoMapArt();
		}
		return instance;
	}

	public final MinecraftClient mc = MinecraftClient.getInstance();

	public final ModSettings modSettings = new ModSettings();

	private AutoMapArt() {

	}

	@Override
	public void onInitialize() {
		LOGGER.info("Auto map art starting ");
		Commands.registerCommands(modSettings);

		modSettings.load();
		Runtime.getRuntime().addShutdownHook(new Thread(
				modSettings::save));
	}
}
