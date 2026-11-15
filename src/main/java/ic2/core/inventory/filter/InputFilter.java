package ic2.core.inventory.filter;

import ic2.api.recipes.ingridients.inputs.IInput;
import net.minecraft.world.item.ItemStack;

public class InputFilter implements IFilter {
   IInput input;

   public InputFilter(IInput input) {
      this.input = input;
   }

   @Override
   public boolean matches(ItemStack input) {
      return this.input.matches(input);
   }
}
