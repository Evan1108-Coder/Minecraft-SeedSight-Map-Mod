package com.seedsight.worldgen;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StructureFinder {

    private record StructureType(String name, int spacing, int separation, long salt) {}

    private static final StructureType[] STRUCTURES = {
            new StructureType("Village", 34, 8, 10387312L),
            new StructureType("Desert Temple", 32, 8, 14357617L),
            new StructureType("Jungle Temple", 32, 8, 14357619L),
            new StructureType("Swamp Hut", 32, 8, 14357620L),
            new StructureType("Pillager Outpost", 32, 8, 165745296L),
            new StructureType("Ocean Monument", 32, 5, 10387313L),
            new StructureType("Woodland Mansion", 80, 20, 10387319L),
            new StructureType("Ancient City", 24, 8, 20083232L),
            new StructureType("Trail Ruins", 34, 8, 83469867L),
            new StructureType("Trial Chamber", 34, 12, 94251327L),
            new StructureType("Ocean Ruin", 20, 8, 14357621L),
            new StructureType("Shipwreck", 24, 4, 165745295L),
            new StructureType("Ruined Portal", 40, 15, 34222645L),
            new StructureType("Igloo", 32, 8, 14357618L),
            new StructureType("Buried Treasure", 1, 0, 0L),
            new StructureType("Mineshaft", 1, 0, 0L),
            // Nether
            new StructureType("Bastion", 27, 4, 30084232L),
            new StructureType("Fortress", 27, 4, 30084232L),
    };

    public Map<String, BlockPos> findNearby(long worldSeed, int playerX, int playerZ, int radius) {
        Map<String, BlockPos> found = new HashMap<>();

        for (StructureType st : STRUCTURES) {
            if (st.spacing <= 1) continue; // Skip mineshafts/buried treasure (too dense)

            BlockPos pos = findNearest(worldSeed, playerX, playerZ, radius, st);
            if (pos != null) {
                found.put(st.name, pos);
            }
        }

        return found;
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
