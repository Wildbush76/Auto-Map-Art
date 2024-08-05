package com.automapart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;

public class AutoMapArt implements ModInitializer {

	private AutoMapArt() {
	}

	public static final String MOD_ID = "auto-map-art";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Auto map art starting ");
		AutoMapArtManager manager = AutoMapArtManager.getInstance();
		manager.initialize();

		Runtime.getRuntime().addShutdownHook(new Thread(
				manager.getModSettings()::save));
	}
}
