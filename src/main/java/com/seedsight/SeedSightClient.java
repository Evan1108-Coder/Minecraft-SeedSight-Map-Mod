package com.seedsight;

import com.seedsight.config.SeedSightConfig;
import com.seedsight.data.WaypointManager;
import com.seedsight.gui.TabManager;
import com.seedsight.hud.HudRenderer;
import com.seedsight.map.MapManager;
import com.seedsight.stats.GameStats;
import com.seedsight.stats.PerfStats;
import com.seedsight.stats.SoundIndicator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeedSightClient implements ClientModInitializer {
    public static final String MOD_ID = "seedsight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static SeedSightClient instance;

    private SeedSightConfig config;
    private MapManager mapManager;
    private WaypointManager waypointManager;
    private TabManager tabManager;
    private GameStats gameStats;
    private PerfStats perfStats;
    private SoundIndicator soundIndicator;
    private HudRenderer hudRenderer;

    private KeyBinding toggleHudKey;
    private KeyBinding openFullscreenKey;
    private KeyBinding zoomInKey;
    private KeyBinding zoomOutKey;
    private KeyBinding addWaypointKey;
    private KeyBinding cycleTabKey;

    public static SeedSightClient getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("SeedSight initializing...");

        config = SeedSightConfig.load();
        mapManager = new MapManager(config);
        waypointManager = WaypointManager.load();
        tabManager = new TabManager();
        gameStats = new GameStats();
        perfStats = new PerfStats();
        soundIndicator = new SoundIndicator();
        hudRenderer = new HudRenderer(this);

        registerKeybindings();
        registerEvents();

        LOGGER.info("SeedSight initialized.");
    }

    private void registerKeybindings() {
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.toggle_hud", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H, "key.seedsight.category"));

        openFullscreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.open_fullscreen", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M, "key.seedsight.category"));

        zoomInKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.zoom_in", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_EQUAL, "key.seedsight.category"));

        zoomOutKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.zoom_out", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_MINUS, "key.seedsight.category"));

        addWaypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.add_waypoint", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B, "key.seedsight.category"));

        cycleTabKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.cycle_tab", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N, "key.seedsight.category"));
    }

    private void registerEvents() {
        HudRenderCallback.EVENT.register((context, tickCounter) ->
                hudRenderer.render(context, tickCounter));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            if (toggleHudKey.wasPressed()) {
                hudRenderer.toggleVisible();
            }
            if (openFullscreenKey.wasPressed()) {
                hudRenderer.toggleExpanded();
            }
            if (zoomInKey.wasPressed()) {
                mapManager.zoomIn();
            }
            if (zoomOutKey.wasPressed()) {
                mapManager.zoomOut();
            }
            if (addWaypointKey.wasPressed()) {
                waypointManager.addWaypointAtPlayer(client.player);
            }
            if (cycleTabKey.wasPressed()) {
                tabManager.cycleTab();
            }

            mapManager.tick(client);
            gameStats.tick(client);
            perfStats.tick(client);
            soundIndicator.tick(client);
        });
    }

    public SeedSightConfig getConfig() { return config; }
    public MapManager getMapManager() { return mapManager; }
    public WaypointManager getWaypointManager() { return waypointManager; }
    public TabManager getTabManager() { return tabManager; }
    public GameStats getGameStats() { return gameStats; }
    public PerfStats getPerfStats() { return perfStats; }
    public SoundIndicator getSoundIndicator() { return soundIndicator; }
    public HudRenderer getHudRenderer() { return hudRenderer; }
}
