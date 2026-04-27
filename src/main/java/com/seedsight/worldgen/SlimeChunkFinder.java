package com.seedsight.worldgen;

import java.util.Random;

public final class SlimeChunkFinder {
    private SlimeChunkFinder() {}

    public static boolean isSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        long seed = worldSeed
                + (long) chunkX * (long) chunkX * 0x4c1906L
                + (long) chunkX * 0x5ac0dbL
                + (long) chunkZ * (long) chunkZ * 0x4307a7L
                + (long) chunkZ * 0x5f24fL
                ^ 0x3AD8025FL;
        Random random = new Random(seed);
        return random.nextInt(10) == 0;
    }
}
