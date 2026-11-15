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

public class Coffee extends IDrinkableFluid {
   int extendDuration = 600;
   int maxAmplifier = 1;

   public Coffee(ResourceLocation id) {
      super(id);
   }

   @Override
   public boolean drink(ItemStack stack, Level world, Player player) {
      this.amplifyEffect(player, MobEffects.MOVEMENT_SPEED);
      this.amplifyEffect(player, MobEffects.DIG_SPEED);
      IC2.PLATFORM.resetSleep(player);
      return true;
   }

   public int amplifyEffect(Player player, MobEffect effect) {
      MobEffectInstance activeEffect = player.getEffect(effect);
      if (activeEffect != null) {
         int amplifier = activeEffect.getAmplifier();
         int duration = activeEffect.getDuration();
         if (amplifier < this.maxAmplifier) {
            amplifier++;
         }

         duration += this.extendDuration;
         activeEffect.update(new MobEffectInstance(activeEffect.getEffect(), duration, amplifier));
         return amplifier;
      } else {
         player.addEffect(new MobEffectInstance(effect, 300, 0));
         return 1;
      }
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/coffee");
   }
}
