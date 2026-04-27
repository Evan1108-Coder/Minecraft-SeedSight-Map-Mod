package com.worldwhisperer.worldgen;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StructureFinder {

    private record StructureType(String name, String label, int spacing, int separation, long salt,
                                  int color) {}

    private static final StructureType[] STRUCTURES = {
            new StructureType("Village", "VLG", 34, 8, 10387312L, 0xFFFFAA00),
            new StructureType("Desert Temple", "DTP", 32, 8, 14357617L, 0xFFD4A017),
            new StructureType("Jungle Temple", "JTP", 32, 8, 14357619L, 0xFF347C17),
            new StructureType("Swamp Hut", "SWH", 32, 8, 14357620L, 0xFF254117),
            new StructureType("Pillager Outpost", "PLR", 32, 8, 165745296L, 0xFF7E2217),
            new StructureType("Ocean Monument", "MON", 32, 5, 10387313L, 0xFF2B65EC),
            new StructureType("Woodland Mansion", "MAN", 80, 20, 10387319L, 0xFF6C3461),
            new StructureType("Ancient City", "ANC", 24, 8, 20083232L, 0xFF0C090A),
            new StructureType("Trail Ruins", "TRL", 34, 8, 83469867L, 0xFFAD7817),
            new StructureType("Trial Chamber", "TRC", 34, 12, 94251327L, 0xFF7D0552),
            new StructureType("Ocean Ruin", "RUN", 20, 8, 14357621L, 0xFF3B9C9C),
            new StructureType("Shipwreck", "SHP", 24, 4, 165745295L, 0xFF8B6914),
            new StructureType("Ruined Portal", "PTL", 40, 15, 34222645L, 0xFF4B0082),
            new StructureType("Igloo", "IGL", 32, 8, 14357618L, 0xFFBDEDFF),
            new StructureType("Bastion", "BAS", 27, 4, 30084232L, 0xFF2C3539),
            new StructureType("Fortress", "FOR", 27, 4, 30084232L, 0xFF990000),
    };

    public record StructureMarker(String name, String label, BlockPos pos, int color, double distance) {}

    public Map<String, BlockPos> findNearby(long worldSeed, int playerX, int playerZ, int radius) {
        Map<String, BlockPos> found = new HashMap<>();

        for (StructureType st : STRUCTURES) {
            BlockPos pos = findNearest(worldSeed, playerX, playerZ, radius, st);
            if (pos != null) {
                found.put(st.name, pos);
            }
        }

        return found;
    }

    public List<StructureMarker> findNearbyMarkers(long worldSeed, int playerX, int playerZ, int radius) {
        List<StructureMarker> markers = new ArrayList<>();

        for (StructureType st : STRUCTURES) {
            BlockPos pos = findNearest(worldSeed, playerX, playerZ, radius, st);
            if (pos != null) {
                double dist = Math.sqrt(Math.pow(pos.getX() - playerX, 2) + Math.pow(pos.getZ() - playerZ, 2));
                markers.add(new StructureMarker(st.name, st.label, pos, st.color, dist));
            }
        }

        markers.sort((a, b) -> Double.compare(a.distance, b.distance));
        return markers;
    }

    private BlockPos findNearest(long worldSeed, int playerX, int playerZ, int radius,
                                  StructureType structure) {
        int spacing = structure.spacing;
        int separation = structure.separation;
        long salt = structure.salt;

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
