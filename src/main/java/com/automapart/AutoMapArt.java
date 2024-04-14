package com.automapart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automapart.autobuilder.AutoBuilder;
import com.automapart.autobuilder.utils.ItemUtils;
import com.automapart.autobuilder.utils.Utils;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;

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

	private MinecraftClient mc;

	public final ModSettings modSettings = new ModSettings();

	public MinecraftClient getMinecraftClient() {
		return mc;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Auto map art starting ");

		mc = MinecraftClient.getInstance();
		AutoBuilder.getInstance().initialize(mc);
		Utils.initialize(mc);
		ItemUtils.initialize(mc);

		Commands.registerCommands(modSettings);

		modSettings.load();
		Runtime.getRuntime().addShutdownHook(new Thread(
				modSettings::save));
	}
}
