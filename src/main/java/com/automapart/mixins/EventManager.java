package com.automapart.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.automapart.autobuilder.AutoBuilder;
import com.mojang.authlib.minecraft.client.MinecraftClient;

@Mixin(value = MinecraftClient.class, priority = 1001)
public class EventManager {
    private static final AutoBuilder autobuilder = AutoBuilder.getInstance();

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo info) {
        autobuilder.onTick();
    }

}
