package ic2.core.inventory.filter;

import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.item.ItemStack;

public class ArrayOrFilter implements IFilter {
   ObjectList<IFilter> filters = CollectionUtils.createList();

   public ArrayOrFilter(Iterable<IFilter> iter) {
      ObjectIterators.pour(iter.iterator(), this.filters);
   }

   public ArrayOrFilter(IFilter... filter) {
      this.filters.addAll(ObjectArrayList.wrap(filter));
   }

   @Override
   public boolean matches(ItemStack input) {
      int i = 0;

      for (int m = this.filters.size(); i < m; i++) {
         if (((IFilter)this.filters.get(i)).matches(input)) {
            return true;
         }
      }

      return false;
   }
}
