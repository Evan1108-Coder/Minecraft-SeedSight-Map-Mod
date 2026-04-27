package com.seedsight.data;

import com.seedsight.SeedSightClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class DeathMarker {

    public static void register() {
        // Track player death via health check in tick
        // A mixin approach is more reliable, but this is simpler
    }

    public static void onPlayerDeath(double x, double y, double z) {
        SeedSightClient.getInstance().getWaypointManager().addDeathMarker(x, y, z);
    }
}
