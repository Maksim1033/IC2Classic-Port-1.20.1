package ic2.core.inventory.filter;

import net.minecraft.world.item.ItemStack;

public class InvertedFilter implements IFilter {
   IFilter filter;

   public InvertedFilter(IFilter filter) {
      this.filter = filter;
   }

   @Override
   public boolean matches(ItemStack input) {
      return !this.filter.matches(input);
   }
}
