package com.worldwhisperer.data;

import net.minecraft.client.MinecraftClient;

public record Waypoint(String name, int x, int y, int z, int color, String dimension, boolean visible) {

    public Waypoint(String name, int x, int y, int z, int color) {
        this(name, x, y, z, color, "overworld", true);
    }

    public double distanceTo(double px, double py, double pz) {
        return Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2) + Math.pow(z - pz, 2));
    }

    public String distanceStr() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return "?";
        double dist = distanceTo(client.player.getX(), client.player.getY(), client.player.getZ());
        if (dist < 1000) return String.format("%.0fm", dist);
        return String.format("%.1fkm", dist / 1000);
    }

    public Waypoint withName(String newName) {
        return new Waypoint(newName, x, y, z, color, dimension, visible);
    }

    public Waypoint withColor(int newColor) {
        return new Waypoint(name, x, y, z, newColor, dimension, visible);
    }

    public Waypoint withVisible(boolean vis) {
        return new Waypoint(name, x, y, z, color, dimension, vis);
    }
}
