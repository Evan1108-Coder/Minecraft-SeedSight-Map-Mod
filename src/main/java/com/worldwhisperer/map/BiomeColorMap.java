package com.worldwhisperer.map;

import java.util.HashMap;
import java.util.Map;

public final class BiomeColorMap {
    private static final Map<String, Integer> BIOME_COLORS = new HashMap<>();

    static {
        // Overworld biomes
        put("minecraft:plains", 0xFF8DB360);
        put("minecraft:sunflower_plains", 0xFFBFB755);
        put("minecraft:snowy_plains", 0xFFFFFFFF);
        put("minecraft:ice_spikes", 0xFFB4DCDC);
        put("minecraft:desert", 0xFFFA9418);
        put("minecraft:swamp", 0xFF07F9B2);
        put("minecraft:mangrove_swamp", 0xFF67C23A);
        put("minecraft:forest", 0xFF056621);
        put("minecraft:flower_forest", 0xFF2D8E49);
        put("minecraft:birch_forest", 0xFF307444);
        put("minecraft:dark_forest", 0xFF40511A);
        put("minecraft:old_growth_birch_forest", 0xFF589C6C);
        put("minecraft:old_growth_pine_taiga", 0xFF596651);
        put("minecraft:old_growth_spruce_taiga", 0xFF818E79);
        put("minecraft:taiga", 0xFF0B6659);
        put("minecraft:snowy_taiga", 0xFF31554A);
        put("minecraft:savanna", 0xFFBDB25F);
        put("minecraft:savanna_plateau", 0xFFA79D64);
        put("minecraft:windswept_hills", 0xFF597D72);
        put("minecraft:windswept_gravelly_hills", 0xFF789878);
        put("minecraft:windswept_forest", 0xFF589C6C);
        put("minecraft:windswept_savanna", 0xFFA79D64);
        put("minecraft:jungle", 0xFF537B09);
        put("minecraft:sparse_jungle", 0xFF628B17);
        put("minecraft:bamboo_jungle", 0xFF768E14);
        put("minecraft:badlands", 0xFFD94515);
        put("minecraft:eroded_badlands", 0xFFFF6D3D);
        put("minecraft:wooded_badlands", 0xFFB09765);
        put("minecraft:meadow", 0xFF63C26D);
        put("minecraft:cherry_grove", 0xFFE7A5C3);
        put("minecraft:grove", 0xFF4A7A62);
        put("minecraft:snowy_slopes", 0xFFD4E7D4);
        put("minecraft:frozen_peaks", 0xFFA3B8CC);
        put("minecraft:jagged_peaks", 0xFFBBC4D1);
        put("minecraft:stony_peaks", 0xFF898989);
        put("minecraft:river", 0xFF0000FF);
        put("minecraft:frozen_river", 0xFFA0A0FF);
        put("minecraft:beach", 0xFFFADE55);
        put("minecraft:snowy_beach", 0xFFFAF0C0);
        put("minecraft:stony_shore", 0xFFA2A284);
        put("minecraft:warm_ocean", 0xFF0000AC);
        put("minecraft:lukewarm_ocean", 0xFF000090);
        put("minecraft:deep_lukewarm_ocean", 0xFF000050);
        put("minecraft:ocean", 0xFF000070);
        put("minecraft:deep_ocean", 0xFF000030);
        put("minecraft:cold_ocean", 0xFF202070);
        put("minecraft:deep_cold_ocean", 0xFF202038);
        put("minecraft:frozen_ocean", 0xFF7070D6);
        put("minecraft:deep_frozen_ocean", 0xFF404090);
        put("minecraft:mushroom_fields", 0xFFFF00FF);
        put("minecraft:dripstone_caves", 0xFF866043);
        put("minecraft:lush_caves", 0xFF6A8C3C);
        put("minecraft:deep_dark", 0xFF0A2626);
        put("minecraft:pale_garden", 0xFFC8BDA4);

        // Nether biomes
        put("minecraft:nether_wastes", 0xFF801900);
        put("minecraft:soul_sand_valley", 0xFF5E3830);
        put("minecraft:crimson_forest", 0xFF940000);
        put("minecraft:warped_forest", 0xFF167E86);
        put("minecraft:basalt_deltas", 0xFF403636);

        // End biomes
        put("minecraft:the_end", 0xFF8080A0);
        put("minecraft:end_highlands", 0xFFA0A0D0);
        put("minecraft:end_midlands", 0xFF9090B0);
        put("minecraft:end_barrens", 0xFF707090);
        put("minecraft:small_end_islands", 0xFF808090);

        // The void
        put("minecraft:the_void", 0xFF000000);
    }

    private static void put(String id, int color) {
        BIOME_COLORS.put(id, color);
    }

    public static int getColor(String biomeId) {
        return BIOME_COLORS.getOrDefault(biomeId, 0xFF808080);
    }

    public static int getColorWithAlpha(String biomeId) {
        return 0xFF000000 | (getColor(biomeId) & 0x00FFFFFF);
    }

    private BiomeColorMap() {}
}
