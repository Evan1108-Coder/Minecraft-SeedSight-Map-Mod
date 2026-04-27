package com.worldwhisperer.stats;

import com.worldwhisperer.ModVersion;
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
        synchronized (recentSounds) {
            recentSounds.removeIf(entry -> now - entry.timestamp > SOUND_LIFETIME_MS);
        }
    }

    public void onSoundPlayed(String soundId, SoundCategory category, float volume) {
        if (volume < 0.1f) return;

        String displayName = formatSoundName(soundId);
        if (displayName == null) return;

        long now = System.currentTimeMillis();

        synchronized (recentSounds) {
            for (SoundEntry entry : recentSounds) {
                if (entry.name.equals(displayName) && now - entry.timestamp < 500) return;
            }

            recentSounds.addFirst(new SoundEntry(displayName, category, now));
            while (recentSounds.size() > MAX_SOUNDS) {
                recentSounds.removeLast();
            }
        }
    }

    public List<String> getRecentSounds() {
        long now = System.currentTimeMillis();
        List<String> result = new ArrayList<>();
        synchronized (recentSounds) {
            for (SoundEntry entry : recentSounds) {
                if (now - entry.timestamp <= SOUND_LIFETIME_MS) {
                    result.add(entry.name);
                }
                if (result.size() >= 4) break;
            }
        }
        return result;
    }

    private String formatSoundName(String soundId) {
        if (soundId == null) return null;

        if (soundId.contains("entity.zombie")) return "Zombie";
        if (soundId.contains("entity.skeleton")) return "Skeleton";
        if (soundId.contains("entity.creeper")) return "Creeper";
        if (soundId.contains("entity.spider")) return "Spider";
        if (soundId.contains("entity.enderman")) return "Enderman";
        if (soundId.contains("entity.blaze")) return "Blaze";
        if (soundId.contains("entity.ghast")) return "Ghast";
        if (soundId.contains("entity.wither")) return "Wither";
        if (soundId.contains("entity.warden")) return "Warden";
        if (soundId.contains("entity.breeze")) return "Breeze";
        if (soundId.contains("entity.ender_dragon")) return "Ender Dragon";
        if (soundId.contains("entity.phantom")) return "Phantom";
        if (soundId.contains("entity.elder_guardian")) return "Elder Guardian";
        if (soundId.contains("entity.shulker")) return "Shulker";
        if (soundId.contains("entity.witch")) return "Witch";
        if (soundId.contains("entity.pillager")) return "Pillager";
        if (soundId.contains("entity.ravager")) return "Ravager";
        if (soundId.contains("entity.evoker")) return "Evoker";
        if (soundId.contains("entity.vindicator")) return "Vindicator";
        if (soundId.contains("entity.vex")) return "Vex";
        if (soundId.contains("entity.hoglin")) return "Hoglin";
        if (soundId.contains("entity.piglin_brute")) return "Piglin Brute";
        if (soundId.contains("entity.piglin")) return "Piglin";
        if (ModVersion.hasCreaking() && soundId.contains("entity.creaking")) return "Creaking";
        if (soundId.contains("entity.player.attack")) return "Player Attack";
        if (soundId.contains("block.anvil")) return "Anvil";
        if (soundId.contains("block.chest")) return "Chest";
        if (soundId.contains("block.portal")) return "Portal";
        if (soundId.contains("entity.tnt")) return "TNT";
        if (soundId.contains("entity.lightning_bolt")) return "Lightning";
        if (soundId.contains("ambient.cave")) return "Cave Ambience";
        if (soundId.contains("block.note_block")) return "Note Block";

        if (soundId.contains("entity.hostile")) return "Hostile Mob";
        if (soundId.contains("block.") && soundId.contains("break")) return "Block Break";
        if (soundId.contains("step")) return "Footsteps";

        return null; // Don't display uninteresting sounds
    }
}
