package com.seedsight.data;

import net.minecraft.client.MinecraftClient;

public record Waypoint(String name, int x, int y, int z, int color, String dimension, boolean visible) {

    public Waypoint(String name, int x, int y, int z, int color) {
        this(name, x, y, z, color, "overworld", true);
    }

    public double distanceTo(double px, double py, double pz) {
        double dx = x - px, dy = y - py, dz = z - pz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public String distanceStr() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return "?";
        String currentDim = client.world.getRegistryKey().getValue().getPath();
        if (!dimension.equals(currentDim)) return "[" + dimLabel() + "]";
        double dist = distanceTo(client.player.getX(), client.player.getY(), client.player.getZ());
        if (dist < 1000) return String.format("%.0fm", dist);
        return String.format("%.1fkm", dist / 1000);
    }

    private String dimLabel() {
        return switch (dimension) {
            case "the_nether" -> "Nether";
            case "the_end" -> "End";
            default -> "OW";
        };
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
