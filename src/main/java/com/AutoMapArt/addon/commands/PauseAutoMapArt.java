
package com.AutoMapArt.addon.commands;

import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import com.AutoMapArt.addon.modules.AutoMapArt.AutoMapArt;

public class PauseAutoMapArt extends Command {
    public PauseAutoMapArt() {
        super("pause", "Pauses auto map art");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            AutoMapArt autoMapArt = Modules.get().get(AutoMapArt.class);
            if (autoMapArt.isActive()) {
                if (!autoMapArt.isPaused()) {
                    autoMapArt.Pause();
                } else {
                    info("Auto map art is already paused");
                }
            } else {
                info("Auto map art not active");
            }
            return SINGLE_SUCCESS;
        });
    }
}
