package com.seedsight.calculator;

public class McCalculator {
    private static final McCalculator INSTANCE = new McCalculator();

    public static McCalculator getInstance() { return INSTANCE; }

    public int[] overworldToNether(int x, int z) {
        return new int[]{x / 8, z / 8};
    }

    public int[] netherToOverworld(int x, int z) {
        return new int[]{x * 8, z * 8};
    }

    public int enchantingPower(int bookshelves) {
        return Math.min(bookshelves, 15) * 2;
    }

    public int bookshelvesForLevel(int level) {
        return (int) Math.ceil(level / 2.0);
    }

    public boolean isCircleBlock(int cx, int cz, int radius, int x, int z) {
        double dx = x - cx, dz = z - cz;
        double dist = Math.sqrt(dx * dx + dz * dz);
        return dist >= radius - 0.5 && dist <= radius + 0.5;
    }

    public int[][] generateCircle(int radius) {
        int diameter = radius * 2 + 1;
        int[][] grid = new int[diameter][diameter];
        for (int x = 0; x < diameter; x++) {
            for (int z = 0; z < diameter; z++) {
                double dx = x - radius, dz = z - radius;
                double dist = Math.sqrt(dx * dx + dz * dz);
                grid[x][z] = (dist <= radius + 0.5) ? 1 : 0;
            }
        }
        return grid;
    }

    public int blockCount(int radius) {
        int count = 0;
        int[][] circle = generateCircle(radius);
        for (int[] row : circle) {
            for (int val : row) {
                count += val;
            }
        }
        return count;
    }

    public double xpForLevel(int level) {
        if (level <= 16) return level * level + 6 * level;
        if (level <= 31) return 2.5 * level * level - 40.5 * level + 360;
        return 4.5 * level * level - 162.5 * level + 2220;
    }

    public int spawnChunkRadius() {
        return 11;
    }
}
