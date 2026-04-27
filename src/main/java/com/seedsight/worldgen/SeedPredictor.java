package com.seedsight.worldgen;

import com.seedsight.map.BiomeColorMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;

public class SeedPredictor {

    public int predictColor(int worldX, int worldZ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return 0xFF808080;

        try {
            BiomeAccess biomeAccess = client.world.getBiomeAccess();
            RegistryEntry<Biome> biomeEntry = biomeAccess.getBiome(new BlockPos(worldX, 64, worldZ));

            // Get biome ID from registry entry
            String biomeId = biomeEntry.getKey()
                    .map(key -> key.getValue().toString())
                    .orElse("minecraft:plains");

            return BiomeColorMap.getColorWithAlpha(biomeId);
        } catch (Exception e) {
            return 0xFF808080;
        }
    }

    public String getBiomeAt(int worldX, int worldY, int worldZ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return "Unknown";

        try {
            RegistryEntry<Biome> biomeEntry = client.world.getBiome(new BlockPos(worldX, worldY, worldZ));
            return biomeEntry.getKey()
                    .map(key -> formatBiomeName(key.getValue().getPath()))
                    .orElse("Unknown");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String formatBiomeName(String path) {
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : path.toCharArray()) {
            if (c == '_') {
                sb.append(' ');
                capitalize = true;
            } else {
                sb.append(capitalize ? Character.toUpperCase(c) : c);
                capitalize = false;
            }
        }
        return sb.toString();
    }
}
