
package com.AutoMapArt.addon.commands;

import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.AutoMapArt.addon.modules.AutoMapArt.AutoMapArt;

public class SkipBlocksAutoMapArt extends Command {
    public SkipBlocksAutoMapArt() {
        super("skip", "Skip current material");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            AutoMapArt autoMapArt = Modules.get().get(AutoMapArt.class);
            if (autoMapArt.isActive()) {
                if (autoMapArt.isPaused()) {
                    info("Auto map art is paused");
                } else {
                    autoMapArt.skipCurrentBlock();
                    info("Skipping current blocks");
                }
            } else {
                info("Auto map art not active");
            }
            return SINGLE_SUCCESS;
        });
    }
}
