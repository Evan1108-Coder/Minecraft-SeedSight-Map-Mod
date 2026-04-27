package com.seedsight.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seedsight.SeedSightClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int[] WAYPOINT_COLORS = {
            0xFFFF5555, 0xFF55FF55, 0xFF5555FF, 0xFFFFFF55,
            0xFFFF55FF, 0xFF55FFFF, 0xFFFFAA00, 0xFFAA00AA,
            0xFF00AAAA, 0xFFAAAAAA
    };

    private final List<Waypoint> waypoints = new ArrayList<>();
    private int colorIndex = 0;

    public static WaypointManager load() {
        WaypointManager mgr = new WaypointManager();
        Path path = getWaypointsPath();
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path);
                Type listType = new TypeToken<List<Waypoint>>() {}.getType();
                List<Waypoint> loaded = GSON.fromJson(json, listType);
                if (loaded != null) {
                    mgr.waypoints.addAll(loaded);
                }
            } catch (IOException e) {
                SeedSightClient.LOGGER.warn("Failed to load waypoints", e);
            }
        }
        return mgr;
    }

    public void save() {
        try {
            Path path = getWaypointsPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(waypoints));
        } catch (IOException e) {
            SeedSightClient.LOGGER.warn("Failed to save waypoints", e);
        }
    }

    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
        save();
    }

    public void addWaypointAtPlayer(ClientPlayerEntity player) {
        if (player == null) return;
        int x = MathHelper.floor(player.getX());
        int y = MathHelper.floor(player.getY());
        int z = MathHelper.floor(player.getZ());
        String name = "WP-" + (waypoints.size() + 1);
        int color = WAYPOINT_COLORS[colorIndex % WAYPOINT_COLORS.length];
        colorIndex++;
        String dim = getCurrentDimension();
        addWaypoint(new Waypoint(name, x, y, z, color, dim, true));
    }

    public void removeWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
            save();
        }
    }

    public void deleteNearest(double px, double py, double pz, String dimension) {
        if (waypoints.isEmpty()) return;
        int nearestIdx = -1;
        double nearestDist = Double.MAX_VALUE;
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);
            if (!wp.dimension().equals(dimension)) continue;
            double dist = wp.distanceTo(px, py, pz);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearestIdx = i;
            }
        }
        if (nearestIdx >= 0 && nearestDist < 32) {
            waypoints.remove(nearestIdx);
            save();
        }
    }

    public List<Waypoint> getWaypoints() {
        return List.copyOf(waypoints);
    }

    public Waypoint getWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) return waypoints.get(index);
        return null;
    }

    public int size() { return waypoints.size(); }

    public void updateHomeMarker(double x, double y, double z) {
        int ix = MathHelper.floor(x);
        int iy = MathHelper.floor(y);
        int iz = MathHelper.floor(z);
        String dim = getCurrentDimension();
        waypoints.removeIf(wp -> wp.name().equals("Home") && wp.dimension().equals(dim));
        addWaypoint(new Waypoint("Home", ix, iy, iz, 0xFF55FF55, dim, true));
    }

    public void addDeathMarker(double x, double y, double z) {
        int ix = MathHelper.floor(x);
        int iy = MathHelper.floor(y);
        int iz = MathHelper.floor(z);
        String dim = getCurrentDimension();
        String markerName = switch (dim) {
            case "the_nether" -> "Death (Nether)";
            case "the_end" -> "Death (End)";
            default -> "Death";
        };
        waypoints.removeIf(wp -> wp.name().startsWith("Death") && wp.dimension().equals(dim));
        addWaypoint(new Waypoint(markerName, ix, iy, iz, 0xFFFF0000, dim, true));
    }

    private String getCurrentDimension() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            return client.world.getRegistryKey().getValue().getPath();
        }
        return "overworld";
    }

    private static Path getWaypointsPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("seedsight_waypoints.json");
    }
}
