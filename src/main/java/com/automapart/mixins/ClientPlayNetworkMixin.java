package com.automapart.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.automapart.autobuilder.AutoBuilder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkMixin {
    @Inject(at = @At("TAIL"), method = "onInventory")
    private void onInventory(InventoryS2CPacket packet, CallbackInfo info) {
        AutoBuilder.getInstance().onInventory(packet);
    }
}
