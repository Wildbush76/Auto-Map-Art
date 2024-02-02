package com.AutoMapArt;

import java.io.File;

import net.fabricmc.loader.api.FabricLoader;

public class SettingsManager {
    public static final String MOD_ID = "auto-map-art";
    public static final File FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();

    
}
