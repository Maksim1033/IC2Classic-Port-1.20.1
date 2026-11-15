package ic2.jeiplugin.core.misc;

import ic2.core.utils.helpers.StackUtil;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;

public class BeerSubInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
   public static final BeerSubInterpreter INSTANCE = new BeerSubInterpreter();

   public String apply(ItemStack stack, UidContext context) {
      return StackUtil.getNbtData(stack).getInt("data") + "";
   }
}
