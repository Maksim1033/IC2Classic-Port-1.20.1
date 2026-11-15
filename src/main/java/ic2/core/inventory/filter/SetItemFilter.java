package ic2.core.inventory.filter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SetItemFilter implements IFilter {
   Set<Item> filter;
   boolean result;

   public SetItemFilter(Item... item) {
      this(new ObjectOpenHashSet(item), true);
   }

   public SetItemFilter(boolean result, Item... item) {
      this(new ObjectOpenHashSet(item), result);
   }

   public SetItemFilter(Set<Item> filter, boolean result) {
      this.filter = filter;
      this.result = result;
   }

   @Override
   public boolean matches(ItemStack input) {
      return !input.isEmpty() && this.filter.contains(input.getItem()) == this.result;
   }
}
