package com.seedsight.stats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

public class PerfStats {
    private int fps;
    private double tps = 20.0;
    private String memoryUsage = "0/0 MB";
    private int loadedChunks;
    private int renderDistance;
    private int ping = -1;
    private int entityCount;

    private long lastTickTime;
    private int tickCount;
    private long tickAccumulator;

    public void tick(MinecraftClient client) {
        fps = client.getCurrentFps();

        // Memory
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long max = rt.maxMemory() / (1024 * 1024);
        memoryUsage = used + "/" + max + " MB";

        // Chunks
        if (client.worldRenderer != null) {
            loadedChunks = client.worldRenderer.getChunksDebugString().length(); // fallback
            try {
                String debug = client.worldRenderer.getChunksDebugString();
                if (debug.contains("/")) {
                    String[] parts = debug.split("/");
                    loadedChunks = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                }
            } catch (Exception ignored) {}
        }

        renderDistance = client.options.getViewDistance().getValue();

        // Ping
        if (client.getNetworkHandler() != null && client.player != null) {
            PlayerListEntry entry = client.getNetworkHandler()
                    .getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                ping = entry.getLatency();
            }
        } else {
            ping = -1;
        }

        // Entity count
        if (client.world != null) {
            entityCount = 0;
            for (var ignored : client.world.getEntities()) {
                entityCount++;
            }
        }

        // TPS estimation (client-side approximation)
        long now = System.nanoTime();
        if (lastTickTime > 0) {
            long delta = now - lastTickTime;
            tickAccumulator += delta;
            tickCount++;
            if (tickCount >= 20) {
                double avgNanos = (double) tickAccumulator / tickCount;
                tps = Math.min(20.0, 1_000_000_000.0 / avgNanos);
                tickAccumulator = 0;
                tickCount = 0;
            }
        }
        lastTickTime = now;
    }

    public int getFps() { return fps; }
    public double getTps() { return tps; }
    public String getMemoryUsage() { return memoryUsage; }
    public int getLoadedChunks() { return loadedChunks; }
    public int getRenderDistance() { return renderDistance; }
    public int getPing() { return ping; }
    public int getEntityCount() { return entityCount; }
}
