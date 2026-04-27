package com.worldwhisperer.hud;

import com.worldwhisperer.WorldWhispererClient;
import com.worldwhisperer.config.WorldWhispererConfig;
import com.worldwhisperer.data.Waypoint;
import com.worldwhisperer.map.MapManager;
import com.worldwhisperer.stats.SoundIndicator;
import com.worldwhisperer.worldgen.SlimeChunkFinder;
import com.worldwhisperer.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class MinimapRenderer {
    private final WorldWhispererClient mod;
    private double cosYaw, sinYaw;
    private boolean curNorthLocked, curCircular;
    private int centerBX, centerBZ, screenCX, screenCY, curBpp, mapRadius;

    public MinimapRenderer(WorldWhispererClient mod) {
        this.mod = mod;
    }

    private int toScreenX(int worldX, int worldZ) {
        int dx = worldX - centerBX;
        int dz = worldZ - centerBZ;
        if (curNorthLocked) return screenCX + dx / curBpp;
        return screenCX + (int) ((dx * cosYaw + dz * sinYaw) / curBpp);
    }

    private boolean isOutsideMap(int px, int pz, int x, int y, int w, int h) {
        if (curCircular) {
            int dx = px - screenCX;
            int dy = pz - screenCY;
            return dx * dx + dy * dy > mapRadius * mapRadius;
        }
        return px < x || px >= x + w || pz < y || pz >= y + h;
    }

    private int toScreenY(int worldX, int worldZ) {
        int dx = worldX - centerBX;
        int dz = worldZ - centerBZ;
        if (curNorthLocked) return screenCY + dz / curBpp;
        return screenCY + (int) ((-dx * sinYaw + dz * cosYaw) / curBpp);
    }

    public void render(DrawContext ctx, int x, int y, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        WorldWhispererConfig cfg = mod.getConfig();
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
        double cosYaw = Math.cos(Math.toRadians(playerYaw));
        double sinYaw = Math.sin(Math.toRadians(playerYaw));
        int halfW = w / 2;
        int halfH = h / 2;
        this.cosYaw = cosYaw;
        this.sinYaw = sinYaw;
        this.curNorthLocked = cfg.northLocked;
        this.centerBX = centerBlockX;
        this.centerBZ = centerBlockZ;
        this.screenCX = x + halfW;
        this.screenCY = y + halfH;
        this.curBpp = blocksPerPixel;
        this.curCircular = cfg.circularMap;
        this.mapRadius = halfW;

        int radiusSq = halfW * halfW;
        for (int px = 0; px < w; px++) {
            for (int pz = 0; pz < h; pz++) {
                if (curCircular) {
                    int cdx = px - halfW;
                    int cdz = pz - halfH;
                    if (cdx * cdx + cdz * cdz > radiusSq) continue;
                }
                int worldX, worldZ;
                if (cfg.northLocked) {
                    worldX = centerBlockX - radiusBlocks + px * blocksPerPixel;
                    worldZ = centerBlockZ - radiusBlocks + pz * blocksPerPixel;
                } else {
                    int dx = px - halfW;
                    int dz = pz - halfH;
                    worldX = centerBlockX + (int) ((dx * cosYaw - dz * sinYaw) * blocksPerPixel);
                    worldZ = centerBlockZ + (int) ((dx * sinYaw + dz * cosYaw) * blocksPerPixel);
                }

                int color = map.getColorAt(worldX, worldZ);
                ctx.fill(x + px, y + pz, x + px + 1, y + pz + 1, color);
            }
        }

        // Draw circular border
        if (curCircular) {
            drawCircleBorder(ctx, x + halfW, y + halfH, halfW);
        }

        // Draw slime chunk overlay (Overworld + north-locked only)
        if (cfg.showSlimeChunks && cfg.northLocked
                && !mod.getGameStats().isInNether() && !mod.getGameStats().isInEnd()) {
            drawSlimeChunks(ctx, x, y, w, h, centerBlockX, centerBlockZ, blocksPerPixel);
        }

        // Draw grid overlay (north-locked only)
        if (cfg.showGrid && cfg.northLocked) {
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

        // Draw sound source markers on minimap
        if (cfg.soundIndicators) {
            drawSoundMarkers(ctx, x, y, w, h);
        }

        // Draw player arrow (center)
        drawPlayerArrow(ctx, x + w / 2, y + h / 2, playerYaw, cfg.northLocked);

        // Draw compass directions (rotated in rotation-following mode)
        drawCompass(ctx, client.textRenderer, x, y, w, h);

        // Draw coordinates at bottom
        int playerY = MathHelper.floor(client.player.getY());
        String coords = w >= 200
                ? String.format("%d / %d / %d  [%d, %d]", centerBlockX, playerY, centerBlockZ,
                        centerBlockX >> 4, centerBlockZ >> 4)
                : String.format("%d / %d / %d", centerBlockX, playerY, centerBlockZ);
        ctx.fill(x, y + h - 10, x + w, y + h, 0x99000000);
        ctx.drawText(client.textRenderer, coords,
                x + (w - client.textRenderer.getWidth(coords)) / 2,
                y + h - 9, ColorUtil.WHITE, true);

        // Draw zoom level indicator (top-right corner)
        String zoomText = zoom + "x";
        ctx.drawText(client.textRenderer, zoomText,
                x + w - client.textRenderer.getWidth(zoomText) - 2,
                y + 2, ColorUtil.GRAY, true);

        // Draw biome change notification
        if (mod.getGameStats().isBiomeChanged()) {
            String biomeName = mod.getGameStats().getBiome();
            int bw = client.textRenderer.getWidth(biomeName);
            ctx.fill(x + w / 2 - bw / 2 - 2, y + 12, x + w / 2 + bw / 2 + 2, y + 23, 0xCC000000);
            ctx.drawText(client.textRenderer, biomeName,
                    x + (w - bw) / 2, y + 13, ColorUtil.GREEN, true);
        }

        // Draw hostile count badge (bottom-left, above coords bar)
        int hostiles = mod.getGameStats().getHostileCount();
        if (hostiles > 0 && cfg.showEntities) {
            String badge = hostiles + " hostile" + (hostiles > 1 ? "s" : "");
            int badgeColor = hostiles > 5 ? ColorUtil.RED : ColorUtil.YELLOW;
            ctx.drawText(client.textRenderer, badge, x + 2, y + h - 19, badgeColor, true);
        }

        // Draw nearest structure indicator (bottom-right, above coords bar)
        if (cfg.showStructures) {
            var markers = map.getStructureMarkers();
            if (markers != null && !markers.isEmpty()) {
                var nearest = markers.get(0);
                String distStr = nearest.distance() < 1000
                        ? String.format("%.0fm", nearest.distance())
                        : String.format("%.1fkm", nearest.distance() / 1000);
                String structText = nearest.label() + " " + distStr;
                int tw = client.textRenderer.getWidth(structText);
                ctx.drawText(client.textRenderer, structText,
                        x + w - tw - 2, y + h - 19, nearest.color(), true);
            }
        }
    }

    private void drawSlimeChunks(DrawContext ctx, int x, int y, int w, int h,
                                  int centerX, int centerZ, int blocksPerPixel) {
        long seed = 0;
        String override = mod.getConfig().seedOverride;
        if (!override.isEmpty()) {
            try { seed = Long.parseLong(override); } catch (NumberFormatException e) { seed = override.hashCode(); }
        }
        if (seed == 0) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.isIntegratedServerRunning() && client.getServer() != null) {
                seed = client.getServer().getOverworld().getSeed();
            }
        }
        if (seed == 0) return;

        int chunkPixels = 16 / blocksPerPixel;
        if (chunkPixels < 2) return;

        int radiusBlocks = (w / 2) * blocksPerPixel;
        int minChunkX = (centerX - radiusBlocks) >> 4;
        int maxChunkX = (centerX + radiusBlocks) >> 4;
        int minChunkZ = (centerZ - radiusBlocks) >> 4;
        int maxChunkZ = (centerZ + radiusBlocks) >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!SlimeChunkFinder.isSlimeChunk(seed, cx, cz)) continue;

                int blockX = cx << 4;
                int blockZ = cz << 4;
                int px1 = x + w / 2 + (blockX - centerX) / blocksPerPixel;
                int pz1 = y + h / 2 + (blockZ - centerZ) / blocksPerPixel;
                int px2 = px1 + 16 / blocksPerPixel;
                int pz2 = pz1 + 16 / blocksPerPixel;

                px1 = Math.max(px1, x);
                pz1 = Math.max(pz1, y);
                px2 = Math.min(px2, x + w);
                pz2 = Math.min(pz2, y + h);

                if (px2 > px1 && pz2 > pz1) {
                    ctx.fill(px1, pz1, px2, pz2, 0x3300FF00);
                }
            }
        }
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
                               int centerX, int centerZ, int bpp, WorldWhispererConfig cfg) {
        MinecraftClient client = MinecraftClient.getInstance();
        int radiusBlocks = (w / 2) * bpp;

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player) continue;

            int ex = MathHelper.floor(entity.getX());
            int ez = MathHelper.floor(entity.getZ());
            if (Math.abs(ex - centerX) > radiusBlocks || Math.abs(ez - centerZ) > radiusBlocks) continue;

            int px = toScreenX(ex, ez);
            int pz = toScreenY(ex, ez);
            if (isOutsideMap(px, pz, x, y, w, h)) continue;

            int color;
            int size;

            if (entity instanceof PlayerEntity otherPlayer) {
                if (!cfg.showPlayers) continue;
                color = ColorUtil.AQUA;
                size = 3;
                ctx.fill(px - size / 2, pz - size / 2,
                        px + size / 2 + 1, pz + size / 2 + 1, color);
                String name = otherPlayer.getName().getString();
                TextRenderer font = client.textRenderer;
                ctx.drawText(font, name, px - font.getWidth(name) / 2, pz - 8, ColorUtil.AQUA, true);
                continue;
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

        String currentDim = mod.getGameStats().getDimension();
        for (Waypoint wp : mod.getWaypointManager().getWaypoints()) {
            if (!wp.visible()) continue;
            if (!wp.dimension().equals(currentDim)) continue;

            int px = toScreenX(wp.x(), wp.z());
            int pz = toScreenY(wp.x(), wp.z());

            if (!isOutsideMap(px, pz, x, y, w, h)) {
                boolean isDeath = wp.name().startsWith("Death");
                int wpColor = wp.color();
                if (isDeath) {
                    float pulse = (float) (0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 300.0));
                    wpColor = ColorUtil.lerp(0xFFAA0000, 0xFFFF5555, pulse);
                }
                int markerSize = isDeath ? 3 : 2;
                ctx.fill(px - markerSize / 2 - 1, pz - markerSize / 2 - 1,
                        px + markerSize / 2 + 2, pz + markerSize / 2 + 2, 0xFF000000);
                if (isDeath) {
                    ctx.fill(px - 1, pz - 2, px + 2, pz + 3, wpColor);
                    ctx.fill(px - 2, pz, px + 3, pz + 1, wpColor);
                } else {
                    ctx.fill(px, pz - 1, px + 1, pz + 2, wpColor);
                    ctx.fill(px - 1, pz, px + 2, pz + 1, wpColor);
                }
                ctx.drawText(font, wp.name(), px + 3, pz - 3, ColorUtil.WHITE, true);
            } else {
                // Off-screen: draw edge indicator pointing toward waypoint
                int ex = Math.max(x + 2, Math.min(x + w - 3, px));
                int ey = Math.max(y + 2, Math.min(y + h - 3, pz));
                ctx.fill(ex - 1, ey - 1, ex + 2, ey + 2, 0xFF000000);
                ctx.fill(ex, ey, ex + 1, ey + 1, wp.color());
            }
        }
    }

    private void drawStructures(DrawContext ctx, int x, int y, int w, int h,
                                 int centerX, int centerZ, int bpp) {
        var markers = mod.getMapManager().getStructureMarkers();
        if (markers == null || markers.isEmpty()) return;

        int radiusBlocks = (w / 2) * bpp;
        TextRenderer font = MinecraftClient.getInstance().textRenderer;

        for (var marker : markers) {
            if (Math.abs(marker.pos().getX() - centerX) > radiusBlocks
                    || Math.abs(marker.pos().getZ() - centerZ) > radiusBlocks) continue;

            int px = toScreenX(marker.pos().getX(), marker.pos().getZ());
            int pz = toScreenY(marker.pos().getX(), marker.pos().getZ());
            if (isOutsideMap(px, pz, x, y, w, h)) continue;

            int borderColor = ColorUtil.brighten(marker.color(), 1.4f) | 0xFF000000;
            ctx.fill(px - 3, pz - 3, px + 4, pz + 4, 0xFF000000);
            ctx.fill(px - 2, pz - 2, px + 3, pz + 3, borderColor);
            ctx.fill(px - 1, pz - 1, px + 2, pz + 2, marker.color());

            ctx.drawText(font, marker.label(), px + 5, pz - 3, marker.color(), true);
        }
    }

    private void drawPlayerArrow(DrawContext ctx, int cx, int cy, float yaw, boolean northLocked) {
        float angle = northLocked ? yaw : 0;
        double rad = Math.toRadians(angle);

        int size = 4;
        int tipX = cx + (int) (Math.sin(rad) * size);
        int tipY = cy - (int) (Math.cos(rad) * size);

        // Arrow body
        ctx.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFFFFFFF);
        ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFF00FF00);

        // Direction tip
        ctx.fill(tipX - 1, tipY - 1, tipX + 2, tipY + 2, 0xFFFFFFFF);
    }

    private void drawCircleBorder(DrawContext ctx, int cx, int cy, int radius) {
        int borderColor = 0xFF333333;
        int highlightColor = 0xFF555555;
        int rSq = radius * radius;
        int rInnerSq = (radius - 1) * (radius - 1);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int distSq = dx * dx + dy * dy;
                if (distSq <= rSq && distSq > rInnerSq) {
                    ctx.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, borderColor);
                } else if (distSq > rSq && distSq <= (radius + 1) * (radius + 1)) {
                    ctx.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, highlightColor);
                }
            }
        }
    }

    private void drawSoundMarkers(DrawContext ctx, int x, int y, int w, int h) {
        for (SoundIndicator.SoundEntry entry : mod.getSoundIndicator().getRecentEntries()) {
            int sx = toScreenX((int) entry.x(), (int) entry.z());
            int sy = toScreenY((int) entry.x(), (int) entry.z());
            if (isOutsideMap(sx, sy, x, y, w, h)) continue;

            long age = System.currentTimeMillis() - entry.timestamp();
            if (age > 2000) continue;

            int alpha = (int) (200 * (1.0 - age / 2000.0));
            int color = entry.hostile()
                    ? ColorUtil.withAlpha(0xFF5555, alpha)
                    : ColorUtil.withAlpha(0xFFFF55, alpha);
            ctx.fill(sx, sy, sx + 1, sy + 1, color);
        }
    }

    private void drawCompass(DrawContext ctx, TextRenderer font,
                              int x, int y, int w, int h) {
        int cx = x + w / 2;
        int cy = y + h / 2;
        if (curNorthLocked) {
            ctx.drawText(font, "N", cx - 2, y + 2, ColorUtil.WHITE, true);
            ctx.drawText(font, "S", cx - 2, y + h - 11, ColorUtil.GRAY, true);
            ctx.drawText(font, "W", x + 2, cy - 4, ColorUtil.GRAY, true);
            ctx.drawText(font, "E", x + w - 8, cy - 4, ColorUtil.GRAY, true);
        } else {
            int r = w / 2 - 6;
            drawDirLabel(ctx, font, "N", cx, cy, r, 0, -1, ColorUtil.WHITE);
            drawDirLabel(ctx, font, "S", cx, cy, r, 0, 1, ColorUtil.GRAY);
            drawDirLabel(ctx, font, "W", cx, cy, r, -1, 0, ColorUtil.GRAY);
            drawDirLabel(ctx, font, "E", cx, cy, r, 1, 0, ColorUtil.GRAY);
        }
    }

    private void drawDirLabel(DrawContext ctx, TextRenderer font, String label,
                               int cx, int cy, int r, int wdx, int wdz, int color) {
        int sx = cx + (int) ((wdx * cosYaw + wdz * sinYaw) * r) - font.getWidth(label) / 2;
        int sy = cy + (int) ((-wdx * sinYaw + wdz * cosYaw) * r) - 4;
        ctx.drawText(font, label, sx, sy, color, true);
    }
}
