package ic2.core.item.food_and_drink.drinks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ColdFruitTea extends BaseTea {
   public ColdFruitTea() {
      super("cold_fruit_tea");
   }

   @Override
   public boolean applyEffects(ItemStack stack, Level world, Player player) {
      this.applyEffect(MobEffects.MOVEMENT_SPEED, player, 200, 1200, 0);
      this.applyEffect(MobEffects.DIG_SPEED, player, 200, 1200, 0);
      this.applyEffect(MobEffects.REGENERATION, player, 150, 300, 0);
      this.decreaseEffect(MobEffects.DIG_SLOWDOWN, player, 150);
      this.decreaseEffect(MobEffects.MOVEMENT_SLOWDOWN, player, 150);
      this.decreaseEffect(MobEffects.CONFUSION, player, 6300);
      this.decreaseEffect(MobEffects.HUNGER, player, 600);
      return true;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/cold_fruit_tea");
   }
}
