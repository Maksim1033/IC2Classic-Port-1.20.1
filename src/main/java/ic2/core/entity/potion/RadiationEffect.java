package ic2.core.entity.potion;

import ic2.api.util.IC2DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class RadiationEffect extends MobEffect {
   public RadiationEffect() {
      super(MobEffectCategory.HARMFUL, 5149489);
   }

   public void applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
      entityLivingBaseIn.hurt(IC2DamageSource.RADIATION, (float)(amplifier + 1));
   }

   public boolean isDurationEffectTick(int duration, int amplifier) {
      return amplifier >= 4 ? true : duration % (25 >> amplifier) == 0;
   }
}
