package ic2.core.platform.corehacks.mixins.client.audio;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin({SoundManager.class})
public interface SoundHandlerMixin {
   @Accessor("soundEngine")
   SoundEngine getSoundEngine();
}
