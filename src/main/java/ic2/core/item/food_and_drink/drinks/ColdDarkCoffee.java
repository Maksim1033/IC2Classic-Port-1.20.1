package ic2.core.item.food_and_drink.drinks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ColdDarkCoffee extends DarkCoffee {
   public ColdDarkCoffee(ResourceLocation id) {
      super(id);
      this.extendDuration = 1000;
      this.maxAmplifier = 6;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/cold_dark_coffee");
   }
}
