package ic2.core.platform.corehacks.mixins.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin({MultiPlayerGameMode.class})
public interface PlayerControllerMixin {
   @Accessor("destroyProgress")
   float getDestroyProgress();
}
