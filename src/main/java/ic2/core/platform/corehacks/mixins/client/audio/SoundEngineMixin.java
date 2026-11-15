package ic2.core.platform.corehacks.mixins.client.audio;

import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin({SoundEngine.class})
public interface SoundEngineMixin {
   @Accessor("channelAccess")
   ChannelAccess getChannels();

   @Accessor("soundBuffers")
   SoundBufferLibrary getAudioBuffers();
}
