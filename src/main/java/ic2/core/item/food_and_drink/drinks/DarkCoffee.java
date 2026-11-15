package ic2.core.item.food_and_drink.drinks;

import ic2.core.IC2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DarkCoffee extends Coffee {
   public DarkCoffee(ResourceLocation id) {
      super(id);
      this.extendDuration = 1200;
      this.maxAmplifier = 5;
   }

   @Override
   public boolean drink(ItemStack stack, Level world, Player player) {
      int highestAmplifier = this.amplifyEffect(player, MobEffects.MOVEMENT_SPEED);
      int currentAmplifier = this.amplifyEffect(player, MobEffects.DIG_SPEED);
      IC2.PLATFORM.resetSleep(player);
      if (currentAmplifier > highestAmplifier) {
         highestAmplifier = currentAmplifier;
      }

      if (highestAmplifier >= 3) {
         player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, (highestAmplifier - 2) * 200, 0));
         if (highestAmplifier >= 4) {
            MobEffects.HARM.applyInstantenousEffect(null, null, player, highestAmplifier - 3, 0.25);
         }
      }

      return true;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/dark_coffee");
   }
}
