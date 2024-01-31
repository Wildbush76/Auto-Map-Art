package com.AutoMapArt.addon.modules.AutoMapArt;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.utils.IScreenFactory;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class AutoMapArtBlockData implements ICopyable<AutoMapArtBlockData>, ISerializable<AutoMapArtBlockData>,
        IChangeable, IBlockData<AutoMapArtBlockData>, IScreenFactory {

    public BlockPos blockPos;

    private boolean changed;

    public AutoMapArtBlockData(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @Override
    public WidgetScreen createScreen(GuiTheme theme, Block block, BlockDataSetting<AutoMapArtBlockData> setting) {
        return new AutoMapArtBlockDataScreen(theme, this, block, setting);
    }

    @Override
    public WidgetScreen createScreen(GuiTheme theme) {
        return new AutoMapArtBlockDataScreen(theme, this, null, null);
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    public void changed() {
        changed = true;
    }

    @Override
    public AutoMapArtBlockData set(AutoMapArtBlockData value) {
        blockPos = value.blockPos;
        changed = value.changed;

        return this;
    }

    @Override
    public AutoMapArtBlockData copy() {
        return new AutoMapArtBlockData(blockPos);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("BlockPos", blockPos.asLong());

        return tag;
    }

    @Override
    public AutoMapArtBlockData fromTag(NbtCompound tag) {
        blockPos = BlockPos.fromLong(tag.getLong("BlockPos"));
        return this;
    }

}