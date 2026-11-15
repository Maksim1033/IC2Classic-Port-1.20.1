package ic2.core.entity.potion;

import ic2.core.platform.registries.IC2Potions;
import java.util.function.Predicate;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ShakyPredicate implements Predicate<Entity> {
   public static final ShakyPredicate INSTANCE = new ShakyPredicate();
   public static final long TOTAL_CYCLE_TICKS = 100L;
   public static final long INVINCIBLE_CYCLE_BASE = 10L;

   public boolean test(Entity entity) {
      if (entity instanceof LivingEntity) {
         MobEffectInstance instance = ((LivingEntity)entity).getEffect(IC2Potions.SHAKY);
         if (instance != null) {
            return entity.getCommandSenderWorld().getGameTime() % 100L >= 10L * (long)(instance.getAmplifier() + 1);
         }
      }

      return true;
   }
}
