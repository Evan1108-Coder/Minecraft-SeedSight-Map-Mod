package com.worldwhisperer.stats;

import com.worldwhisperer.WorldWhispererClient;
import com.worldwhisperer.worldgen.SeedPredictor;
import com.worldwhisperer.worldgen.SlimeChunkFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
    private int yawDegrees;
    private int hostileCount;
    private int passiveCount;
    private int villagerCount;
    private boolean slimeChunk;
    private String dimension = "overworld";
    private String weather = "Clear";
    private int moonPhase;
    private String lastBiome = "";
    private long biomeChangeTime;
    private int tickCounter;
    private float health;
    private float absorption;
    private int food;
    private int armor;
    private float speed;
    private int xpLevel;
    private int air;
    private int maxAir;
    private String heldItemName = "";
    private int durability = -1;
    private int maxDurability = -1;
    private volatile List<String> activeEffects = List.of();

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        playerX = MathHelper.floor(client.player.getX());
        playerY = MathHelper.floor(client.player.getY());
        playerZ = MathHelper.floor(client.player.getZ());

        biome = biomePredictor.getBiomeAt(playerX, playerY, playerZ);
        if (!biome.equals(lastBiome) && !lastBiome.isEmpty()) {
            biomeChangeTime = System.currentTimeMillis();
        }
        lastBiome = biome;

        dimension = client.world.getRegistryKey().getValue().getPath();

        long worldTime = client.world.getTimeOfDay();
        tickTime = worldTime % 24000;
        dayCount = (int) (worldTime / 24000);
        timeOfDay = formatTime(tickTime);

        BlockPos pos = client.player.getBlockPos();
        lightLevel = client.world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = client.world.getLightLevel(LightType.SKY, pos);
        lightLevel = Math.max(lightLevel, skyLight);

        facing = formatDirection(client.player.getHorizontalFacing());
        yawDegrees = ((int) client.player.getYaw() % 360 + 360) % 360;

        health = client.player.getHealth();
        absorption = client.player.getAbsorptionAmount();
        food = client.player.getHungerManager().getFoodLevel();
        armor = client.player.getArmor();
        xpLevel = client.player.experienceLevel;
        air = client.player.getAir();
        maxAir = client.player.getMaxAir();
        double dx = client.player.getX() - client.player.prevX;
        double dz = client.player.getZ() - client.player.prevZ;
        speed = (float) Math.sqrt(dx * dx + dz * dz) * 20;

        ItemStack held = client.player.getMainHandStack();
        if (held != null && !held.isEmpty() && held.isDamageable()) {
            heldItemName = held.getName().getString();
            maxDurability = held.getMaxDamage();
            durability = maxDurability - held.getDamage();
        } else {
            heldItemName = "";
            durability = -1;
        }

        if (client.world.isThundering()) weather = "Thunder";
        else if (client.world.isRaining()) weather = "Rain";
        else weather = "Clear";

        moonPhase = client.world.getMoonPhase();

        tickCounter++;
        if (tickCounter % 20 == 0) {
            hostileCount = 0;
            passiveCount = 0;
            villagerCount = 0;
            for (Entity entity : client.world.getEntities()) {
                double dist = entity.squaredDistanceTo(client.player);
                if (dist > 128 * 128) continue;
                if (entity instanceof HostileEntity) hostileCount++;
                else if (entity instanceof VillagerEntity) villagerCount++;
                else if (entity instanceof AnimalEntity) passiveCount++;
            }

            slimeChunk = false;
            if (!isInNether() && !isInEnd()) {
                long seed = getWorldSeed(client);
                if (seed != 0) {
                    slimeChunk = SlimeChunkFinder.isSlimeChunk(seed,
                            client.player.getChunkPos().x, client.player.getChunkPos().z);
                }
            }

            List<String> effects = new ArrayList<>();
            for (StatusEffectInstance effect : client.player.getStatusEffects()) {
                int amp = effect.getAmplifier();
                int secs = effect.getDuration() / 20;
                String time = String.format("%d:%02d", secs / 60, secs % 60);
                String name = effect.getEffectType().value().getName().getString();
                effects.add(name + (amp > 0 ? " " + (amp + 1) : "") + " " + time);
            }
            activeEffects = List.copyOf(effects);
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

    private long getWorldSeed(MinecraftClient client) {
        WorldWhispererClient mod = WorldWhispererClient.getInstance();
        if (mod != null) {
            String override = mod.getConfig().seedOverride;
            if (!override.isEmpty()) {
                try { return Long.parseLong(override); } catch (NumberFormatException e) { return override.hashCode(); }
            }
        }
        if (client.isIntegratedServerRunning() && client.getServer() != null) {
            return client.getServer().getOverworld().getSeed();
        }
        return 0;
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
    public int getYawDegrees() { return yawDegrees; }
    public int getHostileCount() { return hostileCount; }
    public int getPassiveCount() { return passiveCount; }
    public int getVillagerCount() { return villagerCount; }
    public boolean isSlimeChunk() { return slimeChunk; }
    public String getDimension() { return dimension; }
    public boolean isInNether() { return "the_nether".equals(dimension); }
    public boolean isInEnd() { return "the_end".equals(dimension); }
    public String getWeather() { return weather; }
    public int getMoonPhase() { return moonPhase; }
    public String getMoonPhaseName() {
        return switch (moonPhase) {
            case 0 -> "Full";
            case 1 -> "Waning Gibbous";
            case 2 -> "Third Quarter";
            case 3 -> "Waning Crescent";
            case 4 -> "New";
            case 5 -> "Waxing Crescent";
            case 6 -> "First Quarter";
            case 7 -> "Waxing Gibbous";
            default -> "?";
        };
    }
    public boolean isBiomeChanged() { return System.currentTimeMillis() - biomeChangeTime < 3000; }
    public float getHealth() { return health; }
    public float getAbsorption() { return absorption; }
    public int getFood() { return food; }
    public int getArmor() { return armor; }
    public float getSpeed() { return speed; }
    public int getXpLevel() { return xpLevel; }
    public int getAir() { return air; }
    public int getMaxAir() { return maxAir; }
    public String getHeldItemName() { return heldItemName; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public List<String> getActiveEffects() { return activeEffects; }
}
