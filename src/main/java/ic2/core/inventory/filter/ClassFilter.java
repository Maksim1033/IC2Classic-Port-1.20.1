package ic2.core.inventory.filter;

import net.minecraft.world.item.ItemStack;

public class ClassFilter implements IFilter {
   Class<?> clz;

   public ClassFilter(Class<?> clz) {
      this.clz = clz;
   }

   @Override
   public boolean matches(ItemStack input) {
      return this.clz.isInstance(input.getItem());
   }
}
