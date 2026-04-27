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

        int localX = ((gs.getPlayerX() % 16) + 16) % 16;
        int localZ = ((gs.getPlayerZ() % 16) + 16) % 16;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Chunk", String.format("%d, %d  (%d, %d)", gs.getPlayerX() >> 4, gs.getPlayerZ() >> 4, localX, localZ),
                labelColor, ColorUtil.DARK_GRAY);
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

        // Day/night progress bar
        int barX = x + PAD;
        int barW = w - PAD * 2;
        int barH = 3;
        ctx.fill(barX, ty, barX + barW, ty + barH, 0xFF222222);
        float progress = tickTime / 24000f;
        int fillW = (int) (barW * progress);
        int barColor = tickTime < 12000 ? ColorUtil.GOLD : 0xFF333399;
        ctx.fill(barX, ty, barX + fillW, ty + barH, barColor);
        // Sunset/sunrise markers
        int sunsetX = barX + (int) (barW * (12000f / 24000f));
        ctx.fill(sunsetX, ty, sunsetX + 1, ty + barH, ColorUtil.RED);
        ty += barH + 2;

        int light = gs.getLightLevel();
        int lightColor = light == 0 ? ColorUtil.RED : light <= 7 ? ColorUtil.YELLOW : ColorUtil.GREEN;
        String lightStr;
        if (light == 0) {
            lightStr = "0 DANGER";
        } else {
            lightStr = light + " (B:" + gs.getBlockLight() + " S:" + gs.getSkyLight() + ")";
        }
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Light", lightStr, labelColor, lightColor);
        ty += LINE_H;

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Facing", gs.getFacing() + " " + gs.getYawDegrees() + "\u00B0", labelColor, valueColor);
        ty += LINE_H;

        String target = gs.getTargetBlock();
        if (!target.isEmpty()) {
            String truncated = target.length() > 16 ? target.substring(0, 16) : target;
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Target", truncated, labelColor, ColorUtil.GRAY);
            ty += LINE_H;
        }

        if (gs.isInNether()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Dim", "Nether", labelColor, ColorUtil.RED);
            ty += LINE_H;
            int owX = gs.getPlayerX() * 8, owZ = gs.getPlayerZ() * 8;
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "OW", String.format("%d, %d", owX, owZ), labelColor, ColorUtil.GREEN);
            ty += LINE_H;
        } else if (gs.isInEnd()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Dim", "The End", labelColor, ColorUtil.LIGHT_PURPLE);
            ty += LINE_H;
        } else {
            int nX = gs.getPlayerX() / 8, nZ = gs.getPlayerZ() / 8;
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Nether", String.format("%d, %d", nX, nZ), labelColor, ColorUtil.RED);
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
        float sat = gs.getSaturation();
        int hungerColor = hunger > 14 ? ColorUtil.GREEN : hunger > 6 ? ColorUtil.YELLOW : ColorUtil.RED;
        String foodStr = sat > 0 ? String.format("%d/20 +%.0f", hunger, sat) : hunger + "/20";
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Food", foodStr, labelColor, hungerColor);
        ty += LINE_H;

        if (gs.getArmor() > 0) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Armor", String.valueOf(gs.getArmor()), labelColor, ColorUtil.AQUA);
            ty += LINE_H;
        }

        if (gs.isOnFire()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Status", "ON FIRE", labelColor, ColorUtil.RED);
            ty += LINE_H;
        } else if (gs.isFreezing()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Status", "FREEZING", labelColor, ColorUtil.AQUA);
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
            String speedLabel;
            String speedValue = String.format("%.1f m/s", gs.getSpeed());
            int speedColor = valueColor;
            if (gs.isGliding()) {
                speedLabel = "Elytra";
                speedColor = ColorUtil.LIGHT_PURPLE;
            } else if (!gs.getRidingEntity().isEmpty()) {
                speedLabel = "Riding";
                speedValue = String.format("%.1f (%s)", gs.getSpeed(), gs.getRidingEntity());
            } else if (gs.isSwimming()) {
                speedLabel = "Swim";
                speedColor = ColorUtil.AQUA;
            } else if (gs.isSprinting()) {
                speedLabel = "Sprint";
                speedColor = ColorUtil.YELLOW;
            } else {
                speedLabel = "Speed";
            }
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    speedLabel, speedValue, labelColor, speedColor);
            ty += LINE_H;
        }

        if (gs.getDurability() >= 0) {
            int pct = gs.getMaxDurability() > 0 ? gs.getDurability() * 100 / gs.getMaxDurability() : 0;
            int durColor = pct > 50 ? ColorUtil.GREEN : pct > 20 ? ColorUtil.YELLOW : ColorUtil.RED;
            String toolName = gs.getHeldItemName();
            String toolLabel = toolName.length() > 8 ? toolName.substring(0, 8) : toolName;
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    toolLabel, gs.getDurability() + "/" + gs.getMaxDurability(), labelColor, durColor);
            ty += LINE_H;
        }

        int totalNearby = gs.getHostileCount() + gs.getPassiveCount() + gs.getVillagerCount();
        String entities = gs.getVillagerCount() > 0
                ? String.format("H:%d P:%d V:%d =%d", gs.getHostileCount(), gs.getPassiveCount(), gs.getVillagerCount(), totalNearby)
                : String.format("H:%d P:%d =%d", gs.getHostileCount(), gs.getPassiveCount(), totalNearby);
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Entities", entities, labelColor, valueColor);
        ty += LINE_H;

        // Nearest structure with bearing
        var markers = mod.getMapManager().getStructureMarkers();
        if (markers != null && !markers.isEmpty() && mod.getConfig().showStructures) {
            var nearest = markers.get(0);
            double dx = nearest.pos().getX() - gs.getPlayerX();
            double dz = nearest.pos().getZ() - gs.getPlayerZ();
            double bearing = Math.toDegrees(Math.atan2(dx, -dz));
            if (bearing < 0) bearing += 360;
            String dir = bearingToCardinal(bearing);
            String distStr = nearest.distance() < 1000
                    ? String.format("%.0fm", nearest.distance())
                    : String.format("%.1fkm", nearest.distance() / 1000);
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    nearest.label(), dir + " " + distStr, labelColor, nearest.color());
            ty += LINE_H;
        }

        if (gs.isSlimeChunk()) {
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Slime", "YES (Y<40 to spawn)", labelColor, ColorUtil.GREEN);
            ty += LINE_H;
        }

        String seed = mod.getConfig().seedOverride;
        if (!seed.isEmpty()) {
            String displaySeed = seed.length() > 12 ? seed.substring(0, 12) + "..." : seed;
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Seed", displaySeed, labelColor, ColorUtil.DARK_GRAY);
            ty += LINE_H;
        }

        // Distance + bearing to Home waypoint
        Waypoint home = null;
        Waypoint death = null;
        for (Waypoint wp : mod.getWaypointManager().getWaypoints()) {
            if (wp.dimension().equals(gs.getDimension())) {
                if (wp.name().equals("Home") && home == null) home = wp;
                if (wp.name().startsWith("Death") && death == null) death = wp;
            }
        }
        if (home != null) {
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player != null) {
                double dist = home.distanceTo(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                double hdx = home.x() - gs.getPlayerX();
                double hdz = home.z() - gs.getPlayerZ();
                double hBearing = Math.toDegrees(Math.atan2(hdx, -hdz));
                if (hBearing < 0) hBearing += 360;
                String dir = bearingToCardinal(hBearing);
                String distStr = dist < 1000 ? String.format("%.0fm", dist) : String.format("%.1fkm", dist / 1000);
                drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                        "Home", dir + " " + distStr, labelColor, ColorUtil.GREEN);
                ty += LINE_H;
            }
        }
        if (death != null) {
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player != null) {
                double dist = death.distanceTo(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                String distStr = dist < 1000 ? String.format("%.0fm", dist) : String.format("%.1fkm", dist / 1000);
                drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                        "Death", distStr, labelColor, ColorUtil.RED);
                ty += LINE_H;
            }
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

        // Session stats
        long secs = gs.getSessionDurationSecs();
        if (secs > 60) {
            String sessionTime = String.format("%d:%02d", secs / 60, secs % 60);
            double dist = gs.getTotalDistance();
            String distStr = dist < 1000 ? String.format("%.0fm", dist) : String.format("%.1fkm", dist / 1000);
            drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                    "Session", sessionTime + " / " + distStr, labelColor, ColorUtil.DARK_GRAY);
            ty += LINE_H;
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
            ty += LINE_H;
        }

        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "MC", com.worldwhisperer.ModVersion.MC_VERSION, labelColor, ColorUtil.DARK_GRAY);
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
            ctx.drawText(font, "No waypoints.", x + PAD, ty, ColorUtil.GRAY, true);
            ty += LINE_H;
            ctx.drawText(font, "B: Add  |  X: Delete nearest", x + PAD, ty, ColorUtil.DARK_GRAY, true);
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
        ty += LINE_H + 4;

        ctx.drawText(font, "Mob Spawning", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Spawns", "Light = 0 (since 1.18)", ColorUtil.GRAY, ColorUtil.RED);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Despawn", ">128 blocks instant", ColorUtil.GRAY, ColorUtil.GRAY);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Mob Cap", "70 hostile / 10 passive", ColorUtil.GRAY, ColorUtil.GRAY);
        ty += LINE_H + 4;

        ctx.drawText(font, "Elytra Flight", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Max Speed", "67.5 m/s (firework)", ColorUtil.GRAY, ColorUtil.WHITE);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Glide", "~33 m/s at -30\u00B0", ColorUtil.GRAY, ColorUtil.GRAY);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Duration", "431 durability (7min)", ColorUtil.GRAY, ColorUtil.GRAY);
        ty += LINE_H + 4;

        ctx.drawText(font, "Furnace Timing", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Furnace", "10s per item", ColorUtil.GRAY, ColorUtil.WHITE);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Blast", "5s per item (ores)", ColorUtil.GRAY, ColorUtil.WHITE);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Smoker", "5s per item (food)", ColorUtil.GRAY, ColorUtil.WHITE);
        ty += LINE_H + 4;

        ctx.drawText(font, "Crop Growth", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Wheat", "~24 min (hydrated)", ColorUtil.GRAY, ColorUtil.GOLD);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Sugar Cane", "~18 min per block", ColorUtil.GRAY, ColorUtil.GREEN);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Bamboo", "~4 min per block", ColorUtil.GRAY, ColorUtil.GREEN);
        ty += LINE_H + 4;

        ctx.drawText(font, "Redstone", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Signal", "15 blocks max", ColorUtil.GRAY, ColorUtil.RED);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Repeater", "1-4 ticks (0.1-0.4s)", ColorUtil.GRAY, ColorUtil.GRAY);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Tick", "0.05s (2 per redstone)", ColorUtil.GRAY, ColorUtil.GRAY);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Hopper", "2.5 items/sec", ColorUtil.GRAY, ColorUtil.GRAY);
        ty += LINE_H + 4;

        ctx.drawText(font, "Villager Trading", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Cure", "Zombie Villager = best prices", ColorUtil.GRAY, ColorUtil.GOLD);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Restock", "2x per day (workstation)", ColorUtil.GRAY, ColorUtil.GRAY);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Hero", "25-55% discount (raid)", ColorUtil.GRAY, ColorUtil.GREEN);
        ty += LINE_H + 4;

        ctx.drawText(font, "Fall Damage", x + PAD, ty, ColorUtil.AQUA, true);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Safe", "3 blocks (no damage)", ColorUtil.GRAY, ColorUtil.GREEN);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Fatal", "23.5 blocks (20 HP)", ColorUtil.GRAY, ColorUtil.RED);
        ty += LINE_H;
        drawStatLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Water", "Any depth = safe", ColorUtil.GRAY, ColorUtil.AQUA);
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
        ty += LINE_H + 2;

        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Trail", cfg.showTrail ? "ON" : "OFF");
        ty += LINE_H + 2;

        String posStr = cfg.hudX < 0 ? cfg.getCornerName() : cfg.hudX + "," + cfg.hudY;
        drawSettingLine(ctx, font, x + PAD, ty, w - PAD * 2,
                "Position", posStr);
        ty += LINE_H + 2;

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
                "B: Waypoint", "X: Delete WP", "N: Tab", "C: Circular",
                "L: North lock", "V: Copy coords", "G: Cycle corner"};
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

    private static String bearingToCardinal(double bearing) {
        String[] dirs = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int idx = ((int) Math.round(bearing / 45) % 8 + 8) % 8;
        return dirs[idx];
    }
}
