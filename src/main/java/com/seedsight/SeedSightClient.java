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
    private KeyBinding toggleCircularKey;
    private KeyBinding toggleNorthLockKey;
    private KeyBinding copyCoordsKey;
    private KeyBinding deleteWaypointKey;
    private KeyBinding cycleCornerKey;
    private boolean wasAlive = true;
    private boolean wasSleeping = false;
    private String modeNotification = "";
    private long modeNotificationTime;

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

        toggleCircularKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.toggle_circular", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C, "key.seedsight.category"));

        toggleNorthLockKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.toggle_north_lock", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L, "key.seedsight.category"));

        copyCoordsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.copy_coords", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V, "key.seedsight.category"));

        deleteWaypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.delete_waypoint", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X, "key.seedsight.category"));

        cycleCornerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.seedsight.cycle_corner", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G, "key.seedsight.category"));
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
            if (toggleCircularKey.wasPressed()) {
                config.circularMap = !config.circularMap;
                config.save();
                showModeNotification(config.circularMap ? "Circular Mode" : "Square Mode");
            }
            if (toggleNorthLockKey.wasPressed()) {
                config.northLocked = !config.northLocked;
                config.save();
                showModeNotification(config.northLocked ? "North Locked" : "Rotation Mode");
            }
            if (copyCoordsKey.wasPressed() && client.player != null) {
                int px = net.minecraft.util.math.MathHelper.floor(client.player.getX());
                int py = net.minecraft.util.math.MathHelper.floor(client.player.getY());
                int pz = net.minecraft.util.math.MathHelper.floor(client.player.getZ());
                String coords = px + " " + py + " " + pz;
                client.keyboard.setClipboard(coords);
            }
            if (deleteWaypointKey.wasPressed() && client.player != null) {
                waypointManager.deleteNearest(
                        client.player.getX(), client.player.getY(), client.player.getZ(),
                        gameStats.getDimension());
            }
            if (cycleCornerKey.wasPressed()) {
                config.cycleCorner();
                showModeNotification("HUD: " + config.getCornerName());
            }

            // Death detection
            if (client.player != null) {
                boolean alive = client.player.isAlive();
                if (wasAlive && !alive) {
                    waypointManager.addDeathMarker(
                            client.player.getX(), client.player.getY(), client.player.getZ());
                }
                wasAlive = alive;

                // Bed/sleep detection — save "Home" waypoint
                boolean sleeping = client.player.isSleeping();
                if (!wasSleeping && sleeping) {
                    waypointManager.updateHomeMarker(
                            client.player.getX(), client.player.getY(), client.player.getZ());
                }
                wasSleeping = sleeping;
            }

            mapManager.tick(client);
            gameStats.tick(client);
            perfStats.tick(client);
            soundIndicator.tick(client);
        });
    }

    private void showModeNotification(String text) {
        modeNotification = text;
        modeNotificationTime = System.currentTimeMillis();
    }

    public String getModeNotification() {
        if (modeNotification.isEmpty()) return null;
        if (System.currentTimeMillis() - modeNotificationTime > 2000) return null;
        return modeNotification;
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
