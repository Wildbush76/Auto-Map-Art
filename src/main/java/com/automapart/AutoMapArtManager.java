package com.automapart;

import com.automapart.autobuilder.AutoBuilder;
import com.automapart.autobuilder.utils.ItemUtils;
import com.automapart.autobuilder.utils.Utils;

import net.minecraft.client.MinecraftClient;

public class AutoMapArtManager {
    private static AutoMapArtManager instance;

    private AutoMapArtManager() {
        mc = MinecraftClient.getInstance();
        settings = new ModSettings();
        autoBuilder = new AutoBuilder();
    }

    public static synchronized AutoMapArtManager getInstance() {
        if (instance == null) {
            instance = new AutoMapArtManager();
        }
        return instance;
    }

    private MinecraftClient mc;
    private ModSettings settings;

    private AutoBuilder autoBuilder;

    public void initialize() {
        Commands.registerCommands(settings);
    }

    public ModSettings getModSettings() {
        return settings;
    }

    public AutoBuilder getAutoBuilder() {
        return autoBuilder;
    }

}
