package com.automapart.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.automapart.autobuilder.AutoBuilder;

import net.minecraft.client.gui.screen.BackupPromptScreen.Callback;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;

@Mixin(ClinetPlayNetworkMixin.class)
public class ClinetPlayNetworkMixin {
    @Inject(at = @At("TAIL"), method = "onInventory")
    private void onInventory(InventoryS2CPacket packet, Callback info) {
        AutoBuilder.getInstance().onInventory(packet);
    }
}
