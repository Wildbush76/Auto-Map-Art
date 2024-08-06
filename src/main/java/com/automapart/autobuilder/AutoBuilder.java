package com.automapart.autobuilder;

import com.automapart.autobuilder.utils.State;
import com.automapart.autobuilder.utils.Utils;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;

public class AutoBuilder {
    private SchematicManager schematicManager;

    private State currentState;

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
        currentState = State.BUILDING;
        Utils.info("Building " + closestPlacement.getName());
        return true;
    }

    public void end() {
        schematicManager = null;
        currentState = State.STOPPED;
    }

    public void onTick() {
        switch (currentState) {
            case BUILDING:
                build();
                break;

            default:
                break;
        }
    }

    public void build() {
        // add logic for building here
    }

    public void onInventory(InventoryS2CPacket packet) {

        // add code to get desired material
    }

    public void onPostTick() {
        // add code to check if block place was successful
    }
}
