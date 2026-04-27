package com.worldwhisperer.worldgen;

import net.minecraft.util.math.BlockPos;

import com.worldwhisperer.ModVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StructureFinder {

    private record StructureType(String name, String label, int spacing, int separation, long salt,
                                  int color, int minMinor) {}

    private static final StructureType[] STRUCTURES = {
            new StructureType("Village", "VLG", 34, 8, 10387312L, 0xFFFFAA00, 0),
            new StructureType("Desert Temple", "DTP", 32, 8, 14357617L, 0xFFD4A017, 0),
            new StructureType("Jungle Temple", "JTP", 32, 8, 14357619L, 0xFF347C17, 0),
            new StructureType("Swamp Hut", "SWH", 32, 8, 14357620L, 0xFF254117, 0),
            new StructureType("Pillager Outpost", "PLR", 32, 8, 165745296L, 0xFF7E2217, 0),
            new StructureType("Ocean Monument", "MON", 32, 5, 10387313L, 0xFF2B65EC, 0),
            new StructureType("Woodland Mansion", "MAN", 80, 20, 10387319L, 0xFF6C3461, 0),
            new StructureType("Ancient City", "ANC", 24, 8, 20083232L, 0xFF0C090A, 0),
            new StructureType("Trail Ruins", "TRL", 34, 8, 83469867L, 0xFFAD7817, 0),
            new StructureType("Trial Chamber", "TRC", 34, 12, 94251327L, 0xFF7D0552, 2),
            new StructureType("Ocean Ruin", "RUN", 20, 8, 14357621L, 0xFF3B9C9C, 0),
            new StructureType("Shipwreck", "SHP", 24, 4, 165745295L, 0xFF8B6914, 0),
            new StructureType("Ruined Portal", "PTL", 40, 15, 34222645L, 0xFF4B0082, 0),
            new StructureType("Igloo", "IGL", 32, 8, 14357618L, 0xFFBDEDFF, 0),
    };

    private static final int NETHER_SPACING = 27;
    private static final int NETHER_SEPARATION = 4;
    private static final long NETHER_SALT = 30084232L;

    private static final int NETHER_PORTAL_SPACING = 25;
    private static final int NETHER_PORTAL_SEPARATION = 15;
    private static final long NETHER_PORTAL_SALT = 34222645L;

    public record StructureMarker(String name, String label, BlockPos pos, int color, double distance) {}

    private static final int STRONGHOLD_COLOR = 0xFF80FF80;
    private static final int[] STRONGHOLDS_PER_RING = {3, 6, 10, 15, 21, 28, 36, 9};
    private static final int STRONGHOLD_RING_START = 1408;
    private static final int STRONGHOLD_RING_WIDTH = 1280;

    public List<StructureMarker> findNearbyMarkers(long worldSeed, int playerX, int playerZ, int radius,
                                                      String dimension) {
        List<StructureMarker> markers = new ArrayList<>();
        boolean isNether = "the_nether".equals(dimension);
        boolean isEnd = "the_end".equals(dimension);

        if (!isNether && !isEnd) {
            for (StructureType st : STRUCTURES) {
                if (ModVersion.MC_MINOR < st.minMinor) continue;
                BlockPos pos = findNearest(worldSeed, playerX, playerZ, radius, st.spacing, st.separation, st.salt);
                if (pos != null) {
                    double dist = Math.sqrt(Math.pow(pos.getX() - playerX, 2) + Math.pow(pos.getZ() - playerZ, 2));
                    markers.add(new StructureMarker(st.name, st.label, pos, st.color, dist));
                }
            }

            BlockPos stronghold = findNearestStronghold(worldSeed, playerX, playerZ, radius);
            if (stronghold != null) {
                double dist = Math.sqrt(Math.pow(stronghold.getX() - playerX, 2) + Math.pow(stronghold.getZ() - playerZ, 2));
                markers.add(new StructureMarker("Stronghold", "STR", stronghold, STRONGHOLD_COLOR, dist));
            }
        }

        if (isNether) {
            addNetherMarkers(markers, worldSeed, playerX, playerZ, radius);

            BlockPos portal = findNearest(worldSeed, playerX, playerZ, radius,
                    NETHER_PORTAL_SPACING, NETHER_PORTAL_SEPARATION, NETHER_PORTAL_SALT);
            if (portal != null) {
                double dist = Math.sqrt(Math.pow(portal.getX() - playerX, 2) + Math.pow(portal.getZ() - playerZ, 2));
                markers.add(new StructureMarker("Ruined Portal", "PTL", portal, 0xFF4B0082, dist));
            }
        }

        markers.sort((a, b) -> Double.compare(a.distance, b.distance));
        return markers;
    }

    // Strongholds use a ring-based placement algorithm (128 total across 8 rings)
    private BlockPos findNearestStronghold(long worldSeed, int playerX, int playerZ, int radius) {
        Random random = new Random(worldSeed);
        double angle = random.nextDouble() * Math.PI * 2.0;

        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        int ringStart = STRONGHOLD_RING_START;
        for (int ring = 0; ring < STRONGHOLDS_PER_RING.length; ring++) {
            int count = STRONGHOLDS_PER_RING[ring];
            double ringEnd = ringStart + STRONGHOLD_RING_WIDTH;
            double ringDist = (ringStart + ringEnd) / 2.0;

            for (int i = 0; i < count; i++) {
                int sx = (int) (Math.cos(angle) * ringDist);
                int sz = (int) (Math.sin(angle) * ringDist);
                // Snap to chunk center
                sx = ((sx >> 4) << 4) + 8;
                sz = ((sz >> 4) << 4) + 8;

                double dist = Math.sqrt(Math.pow(sx - playerX, 2) + Math.pow(sz - playerZ, 2));
                if (dist < radius && dist < nearestDist) {
                    nearestDist = dist;
                    nearest = new BlockPos(sx, 32, sz);
                }

                angle += 2.0 * Math.PI / count;
            }

            ringStart += STRONGHOLD_RING_WIDTH * 3;
            angle += random.nextDouble() * Math.PI * 2.0;
        }

        return nearest;
    }

    private void addNetherMarkers(List<StructureMarker> markers, long worldSeed,
                                   int playerX, int playerZ, int radius) {
        findNetherPair(worldSeed, playerX, playerZ, radius, (name, pos) -> {
            double dist = Math.sqrt(Math.pow(pos.getX() - playerX, 2) + Math.pow(pos.getZ() - playerZ, 2));
            boolean isFortress = name.equals("Fortress");
            markers.add(new StructureMarker(name,
                    isFortress ? "FOR" : "BAS", pos,
                    isFortress ? 0xFF990000 : 0xFF2C3539, dist));
        });
    }

    private void findNetherPair(long worldSeed, int playerX, int playerZ, int radius,
                                 java.util.function.BiConsumer<String, BlockPos> consumer) {
        int playerChunkX = playerX >> 4;
        int playerChunkZ = playerZ >> 4;
        int regionRadius = (radius >> 4) / NETHER_SPACING + 1;
        int centerRegionX = Math.floorDiv(playerChunkX, NETHER_SPACING);
        int centerRegionZ = Math.floorDiv(playerChunkZ, NETHER_SPACING);

        for (int rx = -regionRadius; rx <= regionRadius; rx++) {
            for (int rz = -regionRadius; rz <= regionRadius; rz++) {
                int regionX = centerRegionX + rx;
                int regionZ = centerRegionZ + rz;

                long regionSeed = regionX * 341873128712L + regionZ * 132897987541L + worldSeed + NETHER_SALT;
                Random random = new Random(regionSeed);

                int range = NETHER_SPACING - NETHER_SEPARATION;
                int offsetX = range > 0 ? random.nextInt(range) : 0;
                int offsetZ = range > 0 ? random.nextInt(range) : 0;

                int structChunkX = regionX * NETHER_SPACING + offsetX;
                int structChunkZ = regionZ * NETHER_SPACING + offsetZ;
                int structBlockX = (structChunkX << 4) + 8;
                int structBlockZ = (structChunkZ << 4) + 8;

                double dist = Math.sqrt(Math.pow(structBlockX - playerX, 2) + Math.pow(structBlockZ - playerZ, 2));
                if (dist >= radius) continue;

                // Determine bastion vs fortress: use a second random roll
                long typeSeed = regionX * 132897987541L + regionZ * 341873128712L + worldSeed + 30084232L;
                Random typeRandom = new Random(typeSeed);
                boolean isFortress = typeRandom.nextInt(5) < 2; // ~40% fortress, ~60% bastion

                consumer.accept(isFortress ? "Fortress" : "Bastion",
                        new BlockPos(structBlockX, 64, structBlockZ));
            }
        }
    }

    private BlockPos findNearest(long worldSeed, int playerX, int playerZ, int radius,
                                  int spacing, int separation, long salt) {
        int playerChunkX = playerX >> 4;
        int playerChunkZ = playerZ >> 4;

        int regionRadius = (radius >> 4) / spacing + 1;
        int centerRegionX = Math.floorDiv(playerChunkX, spacing);
        int centerRegionZ = Math.floorDiv(playerChunkZ, spacing);

        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int rx = -regionRadius; rx <= regionRadius; rx++) {
            for (int rz = -regionRadius; rz <= regionRadius; rz++) {
                int regionX = centerRegionX + rx;
                int regionZ = centerRegionZ + rz;

                long regionSeed = regionX * 341873128712L + regionZ * 132897987541L + worldSeed + salt;
                Random random = new Random(regionSeed);

                int range = spacing - separation;
                int offsetX = range > 0 ? random.nextInt(range) : 0;
                int offsetZ = range > 0 ? random.nextInt(range) : 0;

                int structChunkX = regionX * spacing + offsetX;
                int structChunkZ = regionZ * spacing + offsetZ;

                int structBlockX = (structChunkX << 4) + 8;
                int structBlockZ = (structChunkZ << 4) + 8;

                double dist = Math.sqrt(
                        Math.pow(structBlockX - playerX, 2) +
                                Math.pow(structBlockZ - playerZ, 2));

                if (dist < radius && dist < nearestDist) {
                    nearestDist = dist;
                    nearest = new BlockPos(structBlockX, 64, structBlockZ);
                }
            }
        }

        return nearest;
    }
}
