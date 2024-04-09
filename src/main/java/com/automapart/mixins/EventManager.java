package com.automapart.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class EventManager {

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo info) {
        // add something
    }
}
