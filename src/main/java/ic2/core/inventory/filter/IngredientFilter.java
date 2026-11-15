package ic2.core.inventory.filter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientFilter implements IFilter {
   Ingredient ingredient;

   public IngredientFilter(Ingredient ingredient) {
      this.ingredient = ingredient;
   }

   @Override
   public boolean matches(ItemStack input) {
      return this.ingredient.test(input);
   }
}
