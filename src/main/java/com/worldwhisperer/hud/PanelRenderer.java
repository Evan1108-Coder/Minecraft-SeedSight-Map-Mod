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

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Day", String.valueOf(gs.getDayCount()), labelColor, valueColor);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Light", String.valueOf(gs.getLightLevel()), labelColor,
                gs.getLightLevel() <= 7 ? ColorUtil.RED : ColorUtil.YELLOW);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Facing", gs.getFacing(), labelColor, valueColor);
        ty += LINE_H;

        String entities = String.format("H:%d P:%d", gs.getHostileCount(), gs.getPassiveCount());
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Entities", entities, labelColor, valueColor);
        ty += LINE_H;

        if (gs.isSlimeChunk()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Slime", "YES", labelColor, ColorUtil.GREEN);
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
    }

    public void renderWaypoints(DrawContext ctx, TextRenderer font, int x, int y, int w, int h) {
        int ty = y + PAD;
        ctx.drawText(font, "Waypoints", x + PAD, ty, ColorUtil.GOLD, true);
        ty += LINE_H + 2;
        RenderUtil.drawHorizontalDivider(ctx, x + PAD, ty, w - PAD * 2);
        ty += 4;

        List<Waypoint> wps = mod.getWaypointManager().getWaypoints();
        if (wps.isEmpty()) {
            ctx.drawText(font, "No waypoints. Press B to add.", x + PAD, ty, ColorUtil.GRAY, true);
            return;
        }

        for (Waypoint wp : wps) {
            if (ty + LINE_H > y + h) break;

            ctx.fill(x + PAD, ty + 1, x + PAD + 6, ty + 7, wp.color());
            String text = String.format("%s  (%d, %d, %d)", wp.name(), wp.x(), wp.y(), wp.z());
            ctx.drawText(font, text, x + PAD + 9, ty, ColorUtil.WHITE, true);

            String dist = wp.distanceStr();
            RenderUtil.drawRightAlignedText(ctx, font, dist, x + w - PAD, ty, ColorUtil.GRAY);

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

        // Nether portal calculator
        ctx.drawText(font, "Nether Portal", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;

        int[] nether = calc.overworldToNether(
                mod.getGameStats().getPlayerX(), mod.getGameStats().getPlayerZ());
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "OW\u2192Nether", String.format("%d, %d", nether[0], nether[1]),
                ColorUtil.GRAY, ColorUtil.RED);
        ty += LINE_H;

        int[] overworld = calc.netherToOverworld(
                mod.getGameStats().getPlayerX(), mod.getGameStats().getPlayerZ());
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Nether\u2192OW", String.format("%d, %d", overworld[0], overworld[1]),
                ColorUtil.GRAY, ColorUtil.GREEN);
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
        ty += LINE_H + 4;

        if (!cfg.seedOverride.isEmpty()) {
            drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Seed", cfg.seedOverride);
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
