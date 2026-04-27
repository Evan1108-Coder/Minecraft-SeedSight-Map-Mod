package com.worldwhisperer;

import com.worldwhisperer.config.WorldWhispererConfig;
import com.worldwhisperer.data.WaypointManager;
import com.worldwhisperer.gui.TabManager;
import com.worldwhisperer.hud.HudRenderer;
import com.worldwhisperer.map.MapManager;
import com.worldwhisperer.stats.GameStats;
import com.worldwhisperer.stats.PerfStats;
import com.worldwhisperer.stats.SoundIndicator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldWhispererClient implements ClientModInitializer {
    public static final String MOD_ID = "worldwhisperer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static WorldWhispererClient instance;

    private WorldWhispererConfig config;
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
    private boolean wasAlive = true;

    public static WorldWhispererClient getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("SeedSight initializing...");

        config = WorldWhispererConfig.load();
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
                "key.worldwhisperer.toggle_hud", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H, "key.worldwhisperer.category"));

        openFullscreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.worldwhisperer.open_fullscreen", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M, "key.worldwhisperer.category"));

        zoomInKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.worldwhisperer.zoom_in", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_EQUAL, "key.worldwhisperer.category"));

        zoomOutKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.worldwhisperer.zoom_out", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_MINUS, "key.worldwhisperer.category"));

        addWaypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.worldwhisperer.add_waypoint", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B, "key.worldwhisperer.category"));

        cycleTabKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.worldwhisperer.cycle_tab", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N, "key.worldwhisperer.category"));
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

            // Death detection
            if (client.player != null) {
                boolean alive = client.player.isAlive();
                if (wasAlive && !alive) {
                    waypointManager.addDeathMarker(
                            client.player.getX(), client.player.getY(), client.player.getZ());
                }
                wasAlive = alive;
            }

            mapManager.tick(client);
            gameStats.tick(client);
            perfStats.tick(client);
            soundIndicator.tick(client);
        });
    }

    public WorldWhispererConfig getConfig() { return config; }
    public MapManager getMapManager() { return mapManager; }
    public WaypointManager getWaypointManager() { return waypointManager; }
    public TabManager getTabManager() { return tabManager; }
    public GameStats getGameStats() { return gameStats; }
    public PerfStats getPerfStats() { return perfStats; }
    public SoundIndicator getSoundIndicator() { return soundIndicator; }
    public HudRenderer getHudRenderer() { return hudRenderer; }
}
