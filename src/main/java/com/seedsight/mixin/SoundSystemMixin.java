package com.seedsight.mixin;

import com.seedsight.SeedSightClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onSoundPlay(SoundInstance sound, CallbackInfo ci) {
        try {
            SeedSightClient mod = SeedSightClient.getInstance();
            if (mod == null || mod.getSoundIndicator() == null) return;

            String soundId = sound.getId().toString();
            SoundCategory category = sound.getCategory();
            float volume = sound.getVolume();

            mod.getSoundIndicator().onSoundPlayed(soundId, category, volume);
        } catch (Exception ignored) {
            // Don't crash the sound system
        }
    }
}
