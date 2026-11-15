package ic2.core.item.food_and_drink.drinks;

import ic2.api.items.IDrinkableFluid;
import ic2.core.IC2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Milk extends IDrinkableFluid {
   public Milk() {
      super(new ResourceLocation(IC2.MOD_ID, "milk"));
   }

   @Override
   public boolean drink(ItemStack stack, Level world, Player player) {
      player.removeAllEffects();
      return true;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/milk");
   }
}
