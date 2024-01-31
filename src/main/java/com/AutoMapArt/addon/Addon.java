package com.AutoMapArt.addon;

import com.AutoMapArt.addon.commands.*;
import com.AutoMapArt.addon.hud.AutoMapArtTheme;
import com.AutoMapArt.addon.modules.AutoMapArt.AutoMapArt;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Auto Map Art");

    @Override
    public void onInitialize() {
        Modules.get().add(new AutoMapArt());

        GuiThemes.add(new AutoMapArtTheme());

        // Commands
        Commands.add(new UnpauseAutoMapArt());
        Commands.add(new PauseAutoMapArt());
        Commands.add(new SkipBlocksAutoMapArt());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.AutoMapArt.addon";
    }
}
