package com.automapart;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoMapArt implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("auto-map-art");
	private static AutoMapArt instance = getInstance();
	public final MinecraftClient mc = MinecraftClient.getInstance();

	public final ModSettings modSettings = new ModSettings();

	private AutoMapArt() {

	}

	public static AutoMapArt getInstance() {
		if (instance == null) {
			instance = new AutoMapArt();
		}
		return instance;
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
