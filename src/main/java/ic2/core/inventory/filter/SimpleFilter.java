package ic2.core.inventory.filter;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class SimpleFilter implements IFilter {
   Item item;

   public SimpleFilter(ItemLike item) {
      this.item = item.asItem();
   }

   @Override
   public boolean matches(ItemStack input) {
      return input.getItem() == this.item;
   }
}
