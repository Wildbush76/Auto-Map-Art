package com.AutoMapArt.addon.hud;

import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
//import meteordevelopment.meteorclient.gui.widgets.WWidget;
//import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
//import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
//import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
//import meteordevelopment.meteorclient.gui.widgets.WWidget;
//import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
//import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.awt.*;

public class AutoMapArtTheme extends MeteorGuiTheme {

    public AutoMapArtTheme() {
        settingsFactory = new DefaultSettingsWidgetFactory(this);
        moduleAlignment.set(AlignmentX.Center);
        accentColor.set(new SettingColor(new Color(73, 73, 73, 255)));
        placeholderColor.set(new SettingColor(new Color(33, 173, 169, 255)));
        moduleBackground.set(new SettingColor(new Color(10, 10, 10, 108)));
        backgroundColor.get().set(new SettingColor(new Color(30, 30, 30, 181)));
    }
}
