package com.seedsight.stats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SoundIndicator {
    private final LinkedList<SoundEntry> recentSounds = new LinkedList<>();
    private static final int MAX_SOUNDS = 8;
    private static final long SOUND_LIFETIME_MS = 3000;

    public record SoundEntry(String name, SoundCategory category, long timestamp) {}

    public void tick(MinecraftClient client) {
        long now = System.currentTimeMillis();
        recentSounds.removeIf(entry -> now - entry.timestamp > SOUND_LIFETIME_MS);
    }

    public void onSoundPlayed(String soundId, SoundCategory category, float volume) {
        if (volume < 0.1f) return;

        String displayName = formatSoundName(soundId);
        if (displayName == null) return;

        long now = System.currentTimeMillis();

        // Deduplicate: don't add the same sound within 500ms
        for (SoundEntry entry : recentSounds) {
            if (entry.name.equals(displayName) && now - entry.timestamp < 500) return;
        }

        recentSounds.addFirst(new SoundEntry(displayName, category, now));
        while (recentSounds.size() > MAX_SOUNDS) {
            recentSounds.removeLast();
        }
    }

    public List<String> getRecentSounds() {
        long now = System.currentTimeMillis();
        List<String> result = new ArrayList<>();
        for (SoundEntry entry : recentSounds) {
            if (now - entry.timestamp <= SOUND_LIFETIME_MS) {
                result.add(entry.name);
            }
            if (result.size() >= 4) break;
        }
        return result;
    }

    private String formatSoundName(String soundId) {
        if (soundId == null) return null;

        // Filter to interesting sounds only
        if (soundId.contains("mob.zombie")) return "Zombie";
        if (soundId.contains("mob.skeleton")) return "Skeleton";
        if (soundId.contains("mob.creeper")) return "Creeper";
        if (soundId.contains("mob.spider")) return "Spider";
        if (soundId.contains("mob.enderman")) return "Enderman";
        if (soundId.contains("mob.blaze")) return "Blaze";
        if (soundId.contains("mob.ghast")) return "Ghast";
        if (soundId.contains("mob.wither")) return "Wither";
        if (soundId.contains("mob.warden")) return "Warden";
        if (soundId.contains("mob.breeze")) return "Breeze";
        if (soundId.contains("mob.creaking")) return "Creaking";
        if (soundId.contains("entity.player.attack")) return "Player Attack";
        if (soundId.contains("block.anvil")) return "Anvil";
        if (soundId.contains("block.chest")) return "Chest";
        if (soundId.contains("block.portal")) return "Portal";
        if (soundId.contains("entity.tnt")) return "TNT";
        if (soundId.contains("entity.lightning")) return "Lightning";
        if (soundId.contains("ambient.cave")) return "Cave Ambience";
        if (soundId.contains("block.note_block")) return "Note Block";

        // Generic categories
        if (soundId.contains("entity.hostile") || soundId.contains("mob.")) return "Hostile Mob";
        if (soundId.contains("block.") && soundId.contains("break")) return "Block Break";
        if (soundId.contains("step")) return "Footsteps";

        return null; // Don't display uninteresting sounds
    }
}
