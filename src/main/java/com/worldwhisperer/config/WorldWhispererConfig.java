package com.worldwhisperer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.worldwhisperer.WorldWhispererClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorldWhispererConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean hudVisible = true;
    public float hudScale = 1.0f;
    public float hudOpacity = 0.85f;
    public int hudX = -1;
    public int hudY = 10;

    public int mapZoom = 2;
    public int minZoom = 0;
    public int maxZoom = 5;
    public boolean showStructures = true;
    public boolean showPlayers = true;
    public boolean showEntities = true;
    public boolean showWaypoints = true;
    public boolean showGrid = false;
    public boolean showSlimeChunks = false;
    public boolean northLocked = true;

    public boolean soundIndicators = true;
    public String seedOverride = "";

    public int mapSize = 128;
    public int panelHeight = 80;
    public int tabWidth = 24;
    public int borderWidth = 3;

    public static WorldWhispererConfig load() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                WorldWhispererConfig cfg = GSON.fromJson(json, WorldWhispererConfig.class);
                if (cfg != null) return cfg;
            } catch (IOException e) {
                WorldWhispererClient.LOGGER.warn("Failed to load config, using defaults", e);
            }
        }
        WorldWhispererConfig cfg = new WorldWhispererConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(this));
        } catch (IOException e) {
            WorldWhispererClient.LOGGER.warn("Failed to save config", e);
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("worldwhisperer.json");
    }

    public int getTotalWidth() {
        return tabWidth + borderWidth * 3 + mapSize;
    }

    public int getTotalHeight() {
        return borderWidth * 3 + mapSize + panelHeight;
    }
}
