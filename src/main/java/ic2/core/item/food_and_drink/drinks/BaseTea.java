package ic2.core.item.food_and_drink.drinks;

import ic2.api.items.IDrinkableFluid;
import ic2.core.IC2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class BaseTea extends IDrinkableFluid {
   public BaseTea(String name) {
      super(new ResourceLocation(IC2.MOD_ID, name));
   }

   @Override
   public boolean drink(ItemStack stack, Level world, Player player) {
      return this.applyEffects(stack, world, player);
   }

   public boolean applyEffects(ItemStack stack, Level world, Player player) {
      this.decreaseEffect(MobEffects.DIG_SLOWDOWN, player, 600);
      this.decreaseEffect(MobEffects.MOVEMENT_SLOWDOWN, player, 600);
      this.decreaseEffect(MobEffects.CONFUSION, player, 1200);
      this.decreaseEffect(MobEffects.HUNGER, player, 2400);
      IC2.PLATFORM.resetSleep(player);
      return true;
   }

   public void applyEffect(MobEffect effect, Player player, int duration, int maxDuration, int amplifier) {
      MobEffectInstance currentEffect = player.getEffect(effect);
      if (currentEffect == null) {
         currentEffect = new MobEffectInstance(effect, duration, amplifier);
      } else {
         if (currentEffect.getAmplifier() > amplifier) {
            return;
         }

         currentEffect = new MobEffectInstance(effect, Math.max(maxDuration, currentEffect.getDuration() + duration), amplifier);
      }

      player.addEffect(currentEffect);
   }

   public void decreaseEffect(MobEffect effect, Player player, int decreaseDuration) {
      if (player.hasEffect(effect)) {
         MobEffectInstance currentEffect = player.getEffect(effect);
         if (currentEffect.getDuration() <= decreaseDuration) {
            player.removeEffectNoUpdate(effect);
            return;
         }

         currentEffect = new MobEffectInstance(
            currentEffect.getEffect(),
            currentEffect.getDuration() - decreaseDuration,
            currentEffect.getAmplifier(),
            currentEffect.isAmbient(),
            currentEffect.isVisible()
         );
         player.removeEffectNoUpdate(effect);
         player.addEffect(currentEffect);
      }
   }
}
