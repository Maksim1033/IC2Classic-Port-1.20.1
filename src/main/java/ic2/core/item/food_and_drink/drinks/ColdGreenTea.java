package ic2.core.item.food_and_drink.drinks;

import ic2.core.IC2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ColdGreenTea extends BaseTea {
   public ColdGreenTea() {
      super("cold_green_tea");
   }

   @Override
   public boolean applyEffects(ItemStack stack, Level world, Player player) {
      this.decreaseEffect(MobEffects.DIG_SLOWDOWN, player, 300);
      this.decreaseEffect(MobEffects.MOVEMENT_SLOWDOWN, player, 300);
      this.decreaseEffect(MobEffects.CONFUSION, player, 600);
      this.decreaseEffect(MobEffects.HUNGER, player, 1200);
      this.applyEffect(MobEffects.MOVEMENT_SPEED, player, 100, 600, 0);
      this.applyEffect(MobEffects.DIG_SPEED, player, 100, 600, 0);
      this.applyEffect(MobEffects.REGENERATION, player, 75, 150, 0);
      IC2.PLATFORM.resetSleep(player);
      return true;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/cold_green_tea");
   }
}
