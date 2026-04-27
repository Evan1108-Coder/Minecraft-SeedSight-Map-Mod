package com.seedsight.stats;

import com.seedsight.worldgen.SeedPredictor;
import com.seedsight.worldgen.SlimeChunkFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;

public class GameStats {
    private final SeedPredictor biomePredictor = new SeedPredictor();

    private int playerX, playerY, playerZ;
    private String biome = "Unknown";
    private String timeOfDay = "Day";
    private long tickTime;
    private int dayCount;
    private int lightLevel;
    private String facing = "N";
    private int hostileCount;
    private int passiveCount;
    private boolean slimeChunk;
    private int tickCounter;

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        playerX = MathHelper.floor(client.player.getX());
        playerY = MathHelper.floor(client.player.getY());
        playerZ = MathHelper.floor(client.player.getZ());

        biome = biomePredictor.getBiomeAt(playerX, playerY, playerZ);

        long worldTime = client.world.getTimeOfDay();
        tickTime = worldTime % 24000;
        dayCount = (int) (worldTime / 24000);
        timeOfDay = formatTime(tickTime);

        BlockPos pos = client.player.getBlockPos();
        lightLevel = client.world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = client.world.getLightLevel(LightType.SKY, pos);
        lightLevel = Math.max(lightLevel, skyLight);

        facing = formatDirection(client.player.getHorizontalFacing());

        tickCounter++;
        if (tickCounter % 20 == 0) {
            hostileCount = 0;
            passiveCount = 0;
            for (Entity entity : client.world.getEntities()) {
                double dist = entity.squaredDistanceTo(client.player);
                if (dist > 128 * 128) continue;
                if (entity instanceof HostileEntity) hostileCount++;
                else if (entity instanceof AnimalEntity) passiveCount++;
            }

            if (client.isIntegratedServerRunning() && client.getServer() != null) {
                long seed = client.getServer().getOverworld().getSeed();
                slimeChunk = SlimeChunkFinder.isSlimeChunk(seed,
                        client.player.getChunkPos().x, client.player.getChunkPos().z);
            }
        }
    }

    private String formatTime(long ticks) {
        int hours = (int) ((ticks / 1000 + 6) % 24);
        int minutes = (int) ((ticks % 1000) * 60 / 1000);
        String period = hours >= 12 ? "PM" : "AM";
        int h12 = hours % 12;
        if (h12 == 0) h12 = 12;
        return String.format("%d:%02d %s", h12, minutes, period);
    }

    private String formatDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> "N (-Z)";
            case SOUTH -> "S (+Z)";
            case EAST -> "E (+X)";
            case WEST -> "W (-X)";
            default -> "?";
        };
    }

    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getPlayerZ() { return playerZ; }
    public String getBiome() { return biome; }
    public String getTimeOfDay() { return timeOfDay; }
    public long getTickTime() { return tickTime; }
    public int getDayCount() { return dayCount; }
    public int getLightLevel() { return lightLevel; }
    public String getFacing() { return facing; }
    public int getHostileCount() { return hostileCount; }
    public int getPassiveCount() { return passiveCount; }
    public boolean isSlimeChunk() { return slimeChunk; }
}
