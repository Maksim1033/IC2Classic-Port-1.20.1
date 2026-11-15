package ic2.core.item.food_and_drink.drinks;

import ic2.api.items.IDrinkableFluid;
import ic2.core.IC2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Cocoa extends IDrinkableFluid {
   public Cocoa() {
      super(new ResourceLocation(IC2.MOD_ID, "cocoa"));
   }

   @Override
   public boolean drink(ItemStack stack, Level world, Player player) {
      player.getFoodData().eat(1, 0.0F);
      player.heal(0.5F);
      return true;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/cocoa");
   }
}
