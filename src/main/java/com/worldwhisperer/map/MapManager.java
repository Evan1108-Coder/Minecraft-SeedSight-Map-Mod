package com.worldwhisperer.map;

import com.worldwhisperer.WorldWhispererClient;
import com.worldwhisperer.config.WorldWhispererConfig;
import com.worldwhisperer.worldgen.SeedPredictor;
import com.worldwhisperer.worldgen.StructureFinder;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MapManager {
    private final WorldWhispererConfig config;
    private final ConcurrentHashMap<Long, int[]> tileCache = new ConcurrentHashMap<>();
    private final SeedPredictor seedPredictor = new SeedPredictor();
    private final StructureFinder structureFinder = new StructureFinder();

    private volatile List<StructureFinder.StructureMarker> structureMarkers = List.of();
    private String nearbyStructure = "";
    private long nearbyStructureTime;
    private int lastPlayerChunkX = Integer.MIN_VALUE;
    private int lastPlayerChunkZ = Integer.MIN_VALUE;
    private int playerY = 64;
    private String lastDimension = "";
    private boolean lastCaveScan = false;
    private int tickCounter = 0;

    private static final int TILE_SIZE = 16;
    private static final int MAX_CACHE_SIZE = 4096;

    public MapManager(WorldWhispererConfig config) {
        this.config = config;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        tickCounter++;

        String currentDim = client.world.getRegistryKey().getValue().getPath();
        if (!currentDim.equals(lastDimension)) {
            tileCache.clear();
            lastDimension = currentDim;
        }

        int playerChunkX = client.player.getChunkPos().x;
        int playerChunkZ = client.player.getChunkPos().z;
        playerY = client.player.getBlockPos().getY();

        boolean caveScan = isCaveScan(client);
        if (caveScan != lastCaveScan) {
            tileCache.clear();
            lastCaveScan = caveScan;
        }

        boolean moved = playerChunkX != lastPlayerChunkX || playerChunkZ != lastPlayerChunkZ;

        // Scan nearby chunks every 10 ticks or when player crosses chunk boundary
        if (moved || tickCounter % 10 == 0) {
            scanNearbyChunks(client);
            lastPlayerChunkX = playerChunkX;
            lastPlayerChunkZ = playerChunkZ;
        }

        // Update structure predictions every 100 ticks or on chunk change
        if (moved || tickCounter % 100 == 0) {
            updateStructures(client);
        }

        // Evict old cache entries
        if (tileCache.size() > MAX_CACHE_SIZE) {
            evictDistantTiles(playerChunkX, playerChunkZ);
        }
    }

    public int getColorAt(int worldX, int worldZ) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;
        long key = chunkKey(chunkX, chunkZ);

        int[] tile = tileCache.get(key);
        if (tile != null) {
            int localX = worldX & 0xF;
            int localZ = worldZ & 0xF;
            return tile[localZ * TILE_SIZE + localX];
        }

        // Fallback to seed-based prediction
        return seedPredictor.predictColor(worldX, worldZ);
    }

    private void scanNearbyChunks(MinecraftClient client) {
        World world = client.world;
        int cx = client.player.getChunkPos().x;
        int cz = client.player.getChunkPos().z;
        int viewDist = client.options.getViewDistance().getValue();
        int scanRadius = Math.min(viewDist, 16);
        boolean caveScan = lastCaveScan;

        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                int chunkX = cx + dx;
                int chunkZ = cz + dz;
                long key = chunkKey(chunkX, chunkZ);

                if (tileCache.containsKey(key)) continue;

                WorldChunk chunk = world.getChunkManager().getWorldChunk(chunkX, chunkZ);
                if (chunk == null) continue;

                int[] colors = scanChunk(world, chunk, caveScan);
                tileCache.put(key, colors);
            }
        }
    }

    private int[] scanChunk(World world, WorldChunk chunk, boolean caveScan) {
        int[] colors = new int[TILE_SIZE * TILE_SIZE];
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        for (int x = 0; x < TILE_SIZE; x++) {
            for (int z = 0; z < TILE_SIZE; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;
                int topY;
                if (caveScan) {
                    topY = findNetherSurface(world, worldX, worldZ);
                } else {
                    topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, worldX, worldZ);
                }
                BlockPos pos = new BlockPos(worldX, topY - 1, worldZ);
                BlockState state = world.getBlockState(pos);

                MapColor mapColor = state.getMapColor(world, pos);
                int color = mapColor.color;

                float shade = 0.8f + (Math.min(Math.max(topY, 40), 120) - 40) * 0.004f;

                int r = Math.min(255, (int) (((color >> 16) & 0xFF) * shade));
                int g = Math.min(255, (int) (((color >> 8) & 0xFF) * shade));
                int b = Math.min(255, (int) ((color & 0xFF) * shade));

                colors[z * TILE_SIZE + x] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
        return colors;
    }

    private int findNetherSurface(World world, int worldX, int worldZ) {
        int startY = Math.min(playerY + 10, 120);
        for (int y = startY; y > 30; y--) {
            BlockState above = world.getBlockState(new BlockPos(worldX, y, worldZ));
            BlockState below = world.getBlockState(new BlockPos(worldX, y - 1, worldZ));
            if (above.isAir() && !below.isAir()) {
                return y;
            }
        }
        return playerY;
    }

    private boolean isCaveScan(MinecraftClient client) {
        if (client.world == null || client.player == null) return false;
        if (isNether(client.world)) return true;
        if (isEnd(client.world)) return false;
        int surfaceY = client.world.getTopY(Heightmap.Type.MOTION_BLOCKING,
                client.player.getBlockPos().getX(), client.player.getBlockPos().getZ());
        return playerY < surfaceY - 4;
    }

    private static boolean isNether(World world) {
        return "the_nether".equals(world.getRegistryKey().getValue().getPath());
    }

    private static boolean isEnd(World world) {
        return "the_end".equals(world.getRegistryKey().getValue().getPath());
    }

    private void updateStructures(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        long seed = getSeed(client);
        if (seed == 0) return;

        int playerX = (int) client.player.getX();
        int playerZ = (int) client.player.getZ();
        String dimension = client.world.getRegistryKey().getValue().getPath();
        structureMarkers = structureFinder.findNearbyMarkers(seed, playerX, playerZ, 2048, dimension);
        if (!structureMarkers.isEmpty()) {
            var nearest = structureMarkers.get(0);
            if (nearest.distance() < 100) {
                String name = nearest.name();
                if (!name.equals(nearbyStructure)) {
                    nearbyStructure = name;
                    nearbyStructureTime = System.currentTimeMillis();
                }
            } else {
                nearbyStructure = "";
            }
        }
    }

    private long getSeed(MinecraftClient client) {
        if (!config.seedOverride.isEmpty()) {
            try {
                return Long.parseLong(config.seedOverride);
            } catch (NumberFormatException e) {
                return config.seedOverride.hashCode();
            }
        }

        if (client.isIntegratedServerRunning() && client.getServer() != null) {
            return client.getServer().getOverworld().getSeed();
        }
        return 0;
    }

    public List<StructureFinder.StructureMarker> getStructureMarkers() {
        return structureMarkers;
    }

    public String getNearbyStructureAlert() {
        if (nearbyStructure.isEmpty()) return null;
        if (System.currentTimeMillis() - nearbyStructureTime > 5000) return null;
        return nearbyStructure;
    }

    public void zoomIn() {
        config.mapZoom = Math.max(config.minZoom, config.mapZoom - 1);
        config.save();
    }

    public void zoomOut() {
        config.mapZoom = Math.min(config.maxZoom, config.mapZoom + 1);
        config.save();
    }

    public int getZoom() {
        return config.mapZoom;
    }

    private void evictDistantTiles(int centerChunkX, int centerChunkZ) {
        int maxDist = 32;
        tileCache.entrySet().removeIf(entry -> {
            long key = entry.getKey();
            int cx = (int) (key >> 32);
            int cz = (int) key;
            return Math.abs(cx - centerChunkX) > maxDist
                    || Math.abs(cz - centerChunkZ) > maxDist;
        });
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
