package com.AutoMapArt.addon.modules.AutoMapArt;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.block.Block;

public class AutoMapArtBlockDataScreen extends WindowScreen {
    private final AutoMapArtBlockData blockData;
    private final Block block;
    private final BlockDataSetting<AutoMapArtBlockData> setting;

    public AutoMapArtBlockDataScreen(GuiTheme theme, AutoMapArtBlockData blockData, Block block,
            BlockDataSetting<AutoMapArtBlockData> setting) {
        super(theme, "Configure Block");
        this.blockData = blockData;
        this.block = block;
        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        Settings settings = new Settings();
        SettingGroup sgGeneral = settings.getDefaultGroup();

        sgGeneral.add(new BlockPosSetting.Builder()
                .name("Chest position")
                .description("Location of chest containing this resource")
                .onModuleActivated(settingBlockPos -> settingBlockPos.set(blockData.blockPos))
                .onChanged(
                        settingBlockPos -> {
                            blockData.blockPos = settingBlockPos;
                            changed(blockData, block, setting);
                        })
                .build());

        settings.onActivated();
        add(theme.settings(settings)).expandX();
    }

    private void changed(AutoMapArtBlockData blockData, Block block, BlockDataSetting<AutoMapArtBlockData> setting) {
        if (blockData.isChanged() && block != null && setting != null) {
            setting.get().put(block, blockData);
            setting.onChanged();
        }
        blockData.changed();
    }

}
