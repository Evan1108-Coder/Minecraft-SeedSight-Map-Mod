package com.seedsight.hud;

import com.seedsight.SeedSightClient;
import com.seedsight.config.SeedSightConfig;
import com.seedsight.data.Waypoint;
import com.seedsight.map.BiomeColorMap;
import com.seedsight.map.MapManager;
import com.seedsight.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MinimapRenderer {
    private final SeedSightClient mod;

    public MinimapRenderer(SeedSightClient mod) {
        this.mod = mod;
    }

    public void render(DrawContext ctx, int x, int y, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        SeedSightConfig cfg = mod.getConfig();
        MapManager map = mod.getMapManager();

        double playerX = client.player.getX();
        double playerZ = client.player.getZ();
        float playerYaw = client.player.getYaw();

        int zoom = cfg.mapZoom;
        int blocksPerPixel = 1 << zoom;
        int radiusBlocks = (w / 2) * blocksPerPixel;

        int centerBlockX = MathHelper.floor(playerX);
        int centerBlockZ = MathHelper.floor(playerZ);

        // Draw map tiles
        for (int px = 0; px < w; px++) {
            for (int pz = 0; pz < h; pz++) {
                int worldX, worldZ;
                if (cfg.northLocked) {
                    worldX = centerBlockX - radiusBlocks + px * blocksPerPixel;
                    worldZ = centerBlockZ - radiusBlocks + pz * blocksPerPixel;
                } else {
                    double rad = Math.toRadians(playerYaw);
                    int dx = px - w / 2;
                    int dz = pz - h / 2;
                    worldX = centerBlockX + (int) ((dx * Math.cos(rad) - dz * Math.sin(rad)) * blocksPerPixel);
                    worldZ = centerBlockZ + (int) ((dx * Math.sin(rad) + dz * Math.cos(rad)) * blocksPerPixel);
                }

                int color = map.getColorAt(worldX, worldZ);
                ctx.fill(x + px, y + pz, x + px + 1, y + pz + 1, color);
            }
        }

        // Draw grid overlay
        if (cfg.showGrid) {
            drawGrid(ctx, x, y, w, h, centerBlockX, centerBlockZ, blocksPerPixel);
        }

        // Draw entities
        if (cfg.showEntities || cfg.showPlayers) {
            drawEntities(ctx, x, y, w, h, centerBlockX, centerBlockZ, blocksPerPixel, cfg);
        }

        // Draw waypoints
        if (cfg.showWaypoints) {
            drawWaypoints(ctx, x, y, w, h, centerBlockX, centerBlockZ, blocksPerPixel);
        }

        // Draw structure icons
        if (cfg.showStructures) {
            drawStructures(ctx, x, y, w, h, centerBlockX, centerBlockZ, blocksPerPixel);
        }

        // Draw player arrow (center)
        drawPlayerArrow(ctx, x + w / 2, y + h / 2, playerYaw, cfg.northLocked);

        // Draw compass directions
        drawCompass(ctx, client.textRenderer, x, y, w, h);

        // Draw coordinates at bottom
        String coords = String.format("X: %d  Z: %d", centerBlockX, centerBlockZ);
        ctx.fill(x, y + h - 10, x + w, y + h, 0x99000000);
        ctx.drawText(client.textRenderer, coords,
                x + (w - client.textRenderer.getWidth(coords)) / 2,
                y + h - 9, ColorUtil.WHITE, true);
    }

    private void drawGrid(DrawContext ctx, int x, int y, int w, int h,
                           int centerX, int centerZ, int blocksPerPixel) {
        int gridColor = 0x33FFFFFF;
        int chunkSize = 16;

        for (int px = 0; px < w; px++) {
            int worldX = centerX - (w / 2) * blocksPerPixel + px * blocksPerPixel;
            if (worldX % chunkSize == 0) {
                ctx.fill(x + px, y, x + px + 1, y + h, gridColor);
            }
        }
        for (int pz = 0; pz < h; pz++) {
            int worldZ = centerZ - (h / 2) * blocksPerPixel + pz * blocksPerPixel;
            if (worldZ % chunkSize == 0) {
                ctx.fill(x, y + pz, x + w, y + pz + 1, gridColor);
            }
        }
    }

    private void drawEntities(DrawContext ctx, int x, int y, int w, int h,
                               int centerX, int centerZ, int bpp, SeedSightConfig cfg) {
        MinecraftClient client = MinecraftClient.getInstance();
        int radiusBlocks = (w / 2) * bpp;

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player) continue;

            int ex = MathHelper.floor(entity.getX());
            int ez = MathHelper.floor(entity.getZ());
            int dx = ex - centerX;
            int dz = ez - centerZ;

            if (Math.abs(dx) > radiusBlocks || Math.abs(dz) > radiusBlocks) continue;

            int px = x + w / 2 + dx / bpp;
            int pz = y + h / 2 + dz / bpp;

            int color;
            int size;

            if (entity instanceof PlayerEntity) {
                if (!cfg.showPlayers) continue;
                color = ColorUtil.AQUA;
                size = 3;
            } else if (entity instanceof HostileEntity) {
                color = ColorUtil.RED;
                size = 2;
            } else if (entity instanceof AnimalEntity) {
                color = ColorUtil.GREEN;
                size = 2;
            } else {
                continue;
            }

            ctx.fill(px - size / 2, pz - size / 2,
                    px + size / 2 + 1, pz + size / 2 + 1, color);
        }
    }

    private void drawWaypoints(DrawContext ctx, int x, int y, int w, int h,
                                int centerX, int centerZ, int bpp) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int radiusBlocks = (w / 2) * bpp;

        for (Waypoint wp : mod.getWaypointManager().getWaypoints()) {
            if (!wp.visible()) continue;
            int dx = wp.x() - centerX;
            int dz = wp.z() - centerZ;

            if (Math.abs(dx) > radiusBlocks || Math.abs(dz) > radiusBlocks) continue;

            int px = x + w / 2 + dx / bpp;
            int pz = y + h / 2 + dz / bpp;

            // Diamond shape
            ctx.fill(px - 1, pz - 2, px + 2, pz + 3, 0xFF000000);
            ctx.fill(px, pz - 1, px + 1, pz + 2, wp.color());
            ctx.fill(px - 1, pz, px + 2, pz + 1, wp.color());

            // Label
            ctx.drawText(font, wp.name(), px + 3, pz - 3, ColorUtil.WHITE, true);
        }
    }

    private void drawStructures(DrawContext ctx, int x, int y, int w, int h,
                                 int centerX, int centerZ, int bpp) {
        // Structure icons rendered from StructureFinder predictions
        var structures = mod.getMapManager().getNearbyStructures();
        if (structures == null) return;

        int radiusBlocks = (w / 2) * bpp;
        TextRenderer font = MinecraftClient.getInstance().textRenderer;

        for (var entry : structures.entrySet()) {
            BlockPos pos = entry.getValue();
            int dx = pos.getX() - centerX;
            int dz = pos.getZ() - centerZ;

            if (Math.abs(dx) > radiusBlocks || Math.abs(dz) > radiusBlocks) continue;

            int px = x + w / 2 + dx / bpp;
            int pz = y + h / 2 + dz / bpp;

            // Structure marker
            ctx.fill(px - 2, pz - 2, px + 3, pz + 3, 0xFFFFAA00);
            ctx.fill(px - 1, pz - 1, px + 2, pz + 2, 0xFFFFFF55);

            String label = entry.getKey();
            if (label.length() > 3) label = label.substring(0, 3);
            ctx.drawText(font, label, px + 4, pz - 3, ColorUtil.GOLD, true);
        }
    }

    private void drawPlayerArrow(DrawContext ctx, int cx, int cy, float yaw, boolean northLocked) {
        float angle = northLocked ? yaw : 0;
        double rad = Math.toRadians(angle);

        int size = 4;
        double tipX = cx + Math.sin(rad) * size;
        double tipY = cy - Math.cos(rad) * size;
        double leftX = cx + Math.sin(rad + 2.5) * (size - 1);
        double leftY = cy - Math.cos(rad + 2.5) * (size - 1);
        double rightX = cx + Math.sin(rad - 2.5) * (size - 1);
        double rightY = cy - Math.cos(rad - 2.5) * (size - 1);

        // Simple arrow approximation using rectangles
        ctx.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFFFFFFF);
        ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFF00FF00);
    }

    private void drawCompass(DrawContext ctx, TextRenderer font,
                              int x, int y, int w, int h) {
        int color = ColorUtil.WHITE;
        ctx.drawText(font, "N", x + w / 2 - 2, y + 2, color, true);
        ctx.drawText(font, "S", x + w / 2 - 2, y + h - 11, ColorUtil.GRAY, true);
        ctx.drawText(font, "W", x + 2, y + h / 2 - 4, ColorUtil.GRAY, true);
        ctx.drawText(font, "E", x + w - 8, y + h / 2 - 4, ColorUtil.GRAY, true);
    }
}
