package com.worldwhisperer.hud;

import com.worldwhisperer.WorldWhispererClient;
import com.worldwhisperer.calculator.McCalculator;
import com.worldwhisperer.data.Waypoint;
import com.worldwhisperer.stats.GameStats;
import com.worldwhisperer.stats.PerfStats;
import com.worldwhisperer.stats.SoundIndicator;
import com.worldwhisperer.util.ColorUtil;
import com.worldwhisperer.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class PanelRenderer {
    private final WorldWhispererClient mod;
    private static final int LINE_H = 10;
    private static final int PAD = 3;

    public PanelRenderer(WorldWhispererClient mod) {
        this.mod = mod;
    }

    public void renderStats(DrawContext ctx, TextRenderer font, int x, int y, int w, int h) {
        GameStats gs = mod.getGameStats();
        int ty = y + PAD;
        int labelColor = ColorUtil.GRAY;
        int valueColor = ColorUtil.WHITE;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "XYZ", String.format("%d / %d / %d", gs.getPlayerX(), gs.getPlayerY(), gs.getPlayerZ()),
                labelColor, valueColor);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Biome", gs.getBiome(), labelColor, ColorUtil.GREEN);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Time", gs.getTimeOfDay(), labelColor, ColorUtil.GOLD);
        ty += LINE_H;

        String dayWeather = gs.getDayCount() + " " + gs.getWeather();
        int weatherColor = switch (gs.getWeather()) {
            case "Rain" -> ColorUtil.AQUA;
            case "Thunder" -> ColorUtil.YELLOW;
            default -> valueColor;
        };
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Day", dayWeather, labelColor, weatherColor);
        ty += LINE_H;

        if (gs.getTickTime() >= 12000) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Moon", gs.getMoonPhaseName(), labelColor, ColorUtil.GRAY);
            ty += LINE_H;
        }

        long tickTime = gs.getTickTime();
        String phaseStr;
        int phaseColor;
        if (tickTime < 12000) {
            int secsLeft = (int) ((13000 - tickTime) / 20);
            phaseStr = String.format("Night in %d:%02d", secsLeft / 60, secsLeft % 60);
            phaseColor = ColorUtil.GOLD;
        } else {
            int secsLeft = (int) ((24000 - tickTime) / 20);
            phaseStr = String.format("Dawn in %d:%02d", secsLeft / 60, secsLeft % 60);
            phaseColor = ColorUtil.AQUA;
        }
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "", phaseStr, labelColor, phaseColor);
        ty += LINE_H;

        int light = gs.getLightLevel();
        int lightColor = light == 0 ? ColorUtil.RED : light <= 7 ? ColorUtil.YELLOW : ColorUtil.GREEN;
        String lightStr = light == 0 ? "0 DANGER" : String.valueOf(light);
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Light", lightStr, labelColor, lightColor);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Facing", gs.getFacing() + " " + gs.getYawDegrees() + "\u00B0", labelColor, valueColor);
        ty += LINE_H;

        if (gs.isInNether()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Dim", "Nether", labelColor, ColorUtil.RED);
            ty += LINE_H;
        } else if (gs.isInEnd()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Dim", "The End", labelColor, ColorUtil.LIGHT_PURPLE);
            ty += LINE_H;
        }

        float hp = gs.getHealth();
        float abs = gs.getAbsorption();
        int hpColor = hp > 10 ? ColorUtil.GREEN : hp > 4 ? ColorUtil.YELLOW : ColorUtil.RED;
        String hpStr = abs > 0 ? String.format("%.0f+%.0f/20", hp, abs) : String.format("%.0f/20", hp);
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "HP", hpStr, labelColor, hpColor);
        ty += LINE_H;

        int hunger = gs.getFood();
        int hungerColor = hunger > 14 ? ColorUtil.GREEN : hunger > 6 ? ColorUtil.YELLOW : ColorUtil.RED;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Food", hunger + "/20", labelColor, hungerColor);
        ty += LINE_H;

        if (gs.getArmor() > 0) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Armor", String.valueOf(gs.getArmor()), labelColor, ColorUtil.AQUA);
            ty += LINE_H;
        }

        if (gs.getXpLevel() > 0) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "XP", "Lv " + gs.getXpLevel(), labelColor, ColorUtil.GREEN);
            ty += LINE_H;
        }

        if (gs.getAir() < gs.getMaxAir()) {
            int airPct = gs.getMaxAir() > 0 ? gs.getAir() * 100 / gs.getMaxAir() : 0;
            int airColor = airPct > 50 ? ColorUtil.AQUA : airPct > 25 ? ColorUtil.YELLOW : ColorUtil.RED;
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Air", airPct + "%", labelColor, airColor);
            ty += LINE_H;
        }

        if (gs.getSpeed() > 0.5f) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Speed", String.format("%.1f m/s", gs.getSpeed()), labelColor, valueColor);
            ty += LINE_H;
        }

        if (gs.getDurability() >= 0) {
            int pct = gs.getMaxDurability() > 0 ? gs.getDurability() * 100 / gs.getMaxDurability() : 0;
            int durColor = pct > 50 ? ColorUtil.GREEN : pct > 20 ? ColorUtil.YELLOW : ColorUtil.RED;
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Tool", gs.getDurability() + "/" + gs.getMaxDurability(), labelColor, durColor);
            ty += LINE_H;
        }

        String entities = gs.getVillagerCount() > 0
                ? String.format("H:%d P:%d V:%d", gs.getHostileCount(), gs.getPassiveCount(), gs.getVillagerCount())
                : String.format("H:%d P:%d", gs.getHostileCount(), gs.getPassiveCount());
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Entities", entities, labelColor, valueColor);
        ty += LINE_H;

        if (gs.isSlimeChunk()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Slime", "YES", labelColor, ColorUtil.GREEN);
        }

        // Active effects
        List<String> effects = gs.getActiveEffects();
        if (!effects.isEmpty()) {
            for (int i = 0; i < Math.min(effects.size(), 3); i++) {
                drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                        i == 0 ? "FX" : "", effects.get(i), labelColor, ColorUtil.LIGHT_PURPLE);
                ty += LINE_H;
            }
        }

        // Sound indicators
        SoundIndicator si = mod.getSoundIndicator();
        List<String> sounds = si.getRecentSounds();
        if (!sounds.isEmpty()) {
            ty = y + h - LINE_H * Math.min(sounds.size(), 2) - PAD;
            for (int i = 0; i < Math.min(sounds.size(), 2); i++) {
                ctx.drawText(font, "\u266A " + sounds.get(i), x + PAD, ty, ColorUtil.AQUA, true);
                ty += LINE_H;
            }
        }
    }

    public void renderPerfStats(DrawContext ctx, TextRenderer font, int x, int y, int w, int h) {
        PerfStats ps = mod.getPerfStats();
        int ty = y + PAD;
        int labelColor = ColorUtil.GRAY;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "FPS", String.valueOf(ps.getFps()),
                labelColor, fpsColor(ps.getFps()));
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "TPS", String.format("%.1f", ps.getTps()),
                labelColor, ps.getTps() >= 19.0 ? ColorUtil.GREEN : ColorUtil.RED);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Memory", ps.getMemoryUsage(),
                labelColor, ColorUtil.WHITE);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Chunks", String.valueOf(ps.getLoadedChunks()),
                labelColor, ColorUtil.WHITE);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Render", String.valueOf(ps.getRenderDistance()),
                labelColor, ColorUtil.WHITE);
        ty += LINE_H;

        if (ps.getPing() >= 0) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Ping", ps.getPing() + "ms",
                    labelColor, pingColor(ps.getPing()));
            ty += LINE_H;
        }

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Entities", String.valueOf(ps.getEntityCount()),
                labelColor, ColorUtil.WHITE);
        ty += LINE_H;

        if (!ps.getServerBrand().isEmpty()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Server", ps.getServerBrand(), labelColor, ColorUtil.GRAY);
            ty += LINE_H;
        }

        if (ps.getOnlinePlayers() > 1) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Players", String.valueOf(ps.getOnlinePlayers()), labelColor, ColorUtil.AQUA);
        }
    }

    public void renderWaypoints(DrawContext ctx, TextRenderer font, int x, int y, int w, int h) {
        int ty = y + PAD;
        int wpCount = mod.getWaypointManager().size();
        String title = wpCount > 0 ? "Waypoints (" + wpCount + ")" : "Waypoints";
        ctx.drawText(font, title, x + PAD, ty, ColorUtil.GOLD, true);
        ty += LINE_H + 2;
        RenderUtil.drawHorizontalDivider(ctx, x + PAD, ty, w - PAD * 2);
        ty += 4;

        String currentDim = mod.getGameStats().getDimension();
        List<Waypoint> wps = new java.util.ArrayList<>(mod.getWaypointManager().getWaypoints());
        if (wps.isEmpty()) {
            ctx.drawText(font, "No waypoints. Press B to add.", x + PAD, ty, ColorUtil.GRAY, true);
            return;
        }
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player != null) {
            double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
            wps.sort((a, b) -> {
                boolean aSameDim = a.dimension().equals(currentDim);
                boolean bSameDim = b.dimension().equals(currentDim);
                if (aSameDim != bSameDim) return aSameDim ? -1 : 1;
                if (!aSameDim) return a.name().compareTo(b.name());
                return Double.compare(a.distanceTo(px, py, pz), b.distanceTo(px, py, pz));
            });
        }
        for (Waypoint wp : wps) {
            if (ty + LINE_H > y + h) break;

            ctx.fill(x + PAD, ty + 1, x + PAD + 6, ty + 7, wp.color());
            String text = String.format("%s  (%d, %d, %d)", wp.name(), wp.x(), wp.y(), wp.z());
            int nameColor = wp.dimension().equals(currentDim) ? ColorUtil.WHITE : ColorUtil.GRAY;
            ctx.drawText(font, text, x + PAD + 9, ty, nameColor, true);

            String dist = wp.distanceStr();
            int distColor = dist.startsWith("[") ? ColorUtil.DARK_GRAY : ColorUtil.GRAY;
            RenderUtil.drawRightAlignedText(ctx, font, dist, x + w - PAD, ty, distColor);

            ty += LINE_H + 1;
        }
    }

    public void renderCalculator(DrawContext ctx, TextRenderer font, int x, int y, int w, int h) {
        int ty = y + PAD;
        ctx.drawText(font, "Calculator", x + PAD, ty, ColorUtil.GOLD, true);
        ty += LINE_H + 2;
        RenderUtil.drawHorizontalDivider(ctx, x + PAD, ty, w - PAD * 2);
        ty += 4;

        McCalculator calc = McCalculator.getInstance();
        GameStats gs = mod.getGameStats();
        int px = gs.getPlayerX();
        int pz = gs.getPlayerZ();

        ctx.drawText(font, "Nether Portal", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;

        if (gs.isInNether()) {
            int[] overworld = calc.netherToOverworld(px, pz);
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Nether\u2192OW", String.format("%d, %d", overworld[0], overworld[1]),
                    ColorUtil.GRAY, ColorUtil.GREEN);
        } else {
            int[] nether = calc.overworldToNether(px, pz);
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "OW\u2192Nether", String.format("%d, %d", nether[0], nether[1]),
                    ColorUtil.GRAY, ColorUtil.RED);
        }
        ty += LINE_H + 4;

        // Spawn chunk info
        ctx.drawText(font, "Spawn Chunks", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Radius", "11 chunks (Lazy: 12-21)", ColorUtil.GRAY, ColorUtil.WHITE);
        ty += LINE_H + 4;

        // Enchanting levels
        ctx.drawText(font, "Enchanting", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Max (30)", "15 bookshelves", ColorUtil.GRAY, ColorUtil.LIGHT_PURPLE);
        ty += LINE_H + 4;

        ctx.drawText(font, "XP Levels", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Lv 30", String.format("%.0f XP", calc.xpForLevel(30)), ColorUtil.GRAY, ColorUtil.GREEN);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Lv 50", String.format("%.0f XP", calc.xpForLevel(50)), ColorUtil.GRAY, ColorUtil.YELLOW);
        ty += LINE_H + 4;

        ctx.drawText(font, "Ore Y Levels", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Diamond", "Y -64 to 16 (best -59)", ColorUtil.GRAY, ColorUtil.AQUA);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Iron", "Y -24 to 56 (best 16)", ColorUtil.GRAY, ColorUtil.WHITE);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Gold", "Y -64 to 32 (best -16)", ColorUtil.GRAY, ColorUtil.GOLD);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Lapis", "Y -64 to 64 (best 0)", ColorUtil.GRAY, ColorUtil.MC_BLUE);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Emerald", "Y -16 to 320 (best 256)", ColorUtil.GRAY, ColorUtil.GREEN);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Ancient", "Y 8 to 22 (best 15)", ColorUtil.GRAY, ColorUtil.MC_RED);
    }

    public void renderSettings(DrawContext ctx, TextRenderer font, int x, int y, int w, int h) {
        int ty = y + PAD;
        ctx.drawText(font, "Settings", x + PAD, ty, ColorUtil.GOLD, true);
        ty += LINE_H + 2;
        RenderUtil.drawHorizontalDivider(ctx, x + PAD, ty, w - PAD * 2);
        ty += 4;

        var cfg = mod.getConfig();

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "HUD Scale", String.format("%.1fx", cfg.hudScale));
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Opacity", String.format("%d%%", (int) (cfg.hudOpacity * 100)));
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Zoom", String.valueOf(cfg.mapZoom));
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Structures", cfg.showStructures ? "ON" : "OFF");
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Players", cfg.showPlayers ? "ON" : "OFF");
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Entities", cfg.showEntities ? "ON" : "OFF");
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Sound Ind.", cfg.soundIndicators ? "ON" : "OFF");
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Grid", cfg.showGrid ? "ON" : "OFF");
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Slime Chunks", cfg.showSlimeChunks ? "ON" : "OFF");
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "North Lock", cfg.northLocked ? "ON" : "OFF");
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Circular Map", cfg.circularMap ? "ON" : "OFF");
        ty += LINE_H + 4;

        if (!cfg.seedOverride.isEmpty()) {
            drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Seed", cfg.seedOverride);
            ty += LINE_H + 4;
        }

        // Keybinding hints
        ctx.drawText(font, "Keybindings", x + PAD, ty, ColorUtil.GOLD, true);
        ty += LINE_H;
        RenderUtil.drawHorizontalDivider(ctx, x + PAD, ty, w - PAD * 2);
        ty += 4;
        String[] hints = {"H: Toggle HUD", "M: Expand map", "+/-: Zoom",
                "B: Waypoint", "N: Tab", "C: Circular", "L: North lock"};
        for (String hint : hints) {
            if (ty + LINE_H > y + h) break;
            ctx.drawText(font, hint, x + PAD, ty, ColorUtil.GRAY, true);
            ty += LINE_H;
        }
    }

    private void drawStatLine(DrawContext ctx, TextRenderer font,
                               int x, int y, int w, String label, String value,
                               int labelColor, int valueColor) {
        ctx.drawText(font, label, x, y, labelColor, true);
        RenderUtil.drawRightAlignedText(ctx, font, value, x + w, y, valueColor);
    }

    private void drawSettingLine(DrawContext ctx, TextRenderer font,
                                  int x, int y, int w, String label, String value) {
        ctx.drawText(font, label, x, y, ColorUtil.GRAY, true);
        int valColor = value.equals("ON") ? ColorUtil.GREEN :
                value.equals("OFF") ? ColorUtil.RED : ColorUtil.WHITE;
        RenderUtil.drawRightAlignedText(ctx, font, value, x + w, y, valColor);
    }

    private int fpsColor(int fps) {
        if (fps >= 60) return ColorUtil.GREEN;
        if (fps >= 30) return ColorUtil.YELLOW;
        return ColorUtil.RED;
    }

    private int pingColor(int ping) {
        if (ping < 50) return ColorUtil.GREEN;
        if (ping < 150) return ColorUtil.YELLOW;
        return ColorUtil.RED;
    }
}
