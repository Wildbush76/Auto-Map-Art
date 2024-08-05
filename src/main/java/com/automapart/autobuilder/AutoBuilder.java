package com.automapart.autobuilder;

import com.automapart.autobuilder.utils.Utils;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;

public class AutoBuilder {
    private MinecraftClient mc;
    private SchematicManager schematicManager;

    public AutoBuilder(MinecraftClient mc) {
        this.mc = mc;
    }

    public boolean start() {
        SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();
        SchematicPlacement closestPlacement = null;
        Double distance = Double.MAX_VALUE;
        for (SchematicPlacement schematicPlacement : manager.getAllSchematicsPlacements()) {
            double dist = Utils.distanceTo(schematicPlacement.getOrigin());
            if (dist < distance) {
                distance = dist;
                closestPlacement = schematicPlacement;
            }
        }
        if (closestPlacement == null) {
            Utils.error("No loaded schematic detected");
            return false;
        }
        schematicManager = new SchematicManager(closestPlacement);
        Utils.info("Building " + closestPlacement.getName());
        return true;
    }

    public void end() {
        schematicManager = null;
    }

    public void onTick() {

    }

    public void onInventory(InventoryS2CPacket packet) {

    }

    public void onPostTick() {

    }
}
