package ic2.core.block.base.features;

import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IParticleSpawner {
   @OnlyIn(Dist.CLIENT)
   void animationTick(RandomSource var1);
}
