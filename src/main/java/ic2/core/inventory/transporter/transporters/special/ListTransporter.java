package ic2.core.inventory.transporter.transporters.special;

import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.transporters.BaseTransporter;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class ListTransporter extends BaseTransporter {
   List<IItemTransporter> transporters;

   public ListTransporter(IItemTransporter... transporters) {
      this.transporters = ObjectArrayList.wrap(transporters);
   }

   public ListTransporter(List<IItemTransporter> transporters) {
      this.transporters = transporters;
   }

   @Override
   public int addItem(ItemStack stack, Direction dir, boolean simulate) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         int total = 0;
         int i = 0;

         for (int m = this.transporters.size(); i < m; i++) {
            total += this.transporters.get(i).addItem(StackUtil.copyWithSize(stack, stack.getCount() - total), dir, simulate);
            if (total >= stack.getCount()) {
               return total;
            }
         }

         return total;
      }
   }

   @Override
   public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
      if (amount <= 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = ItemStack.EMPTY;
         int i = 0;

         for (int m = this.transporters.size(); i < m; i++) {
            if (stack.isEmpty()) {
               stack = this.transporters.get(i).removeItem(filter, dir, amount, simulate);
               if (!stack.isEmpty()) {
                  filter = StackFilter.defaultCompare(stack);
               }
            } else {
               if (stack.getCount() >= amount) {
                  break;
               }

               stack.grow(this.transporters.get(i).removeItem(filter, dir, amount - stack.getCount(), simulate).getCount());
            }
         }

         return stack;
      }
   }

   @Override
   public int getInventorySize(Direction dir) {
      int total = 0;
      int i = 0;

      for (int m = this.transporters.size(); i < m; i++) {
         total += this.transporters.get(i).getInventorySize(dir);
      }

      return total;
   }

   @Override
   public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
      Object2IntLinkedOpenCustomHashMap<ItemStack> items = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(compareNBT));
      int i = 0;

      for (int m = this.transporters.size(); i < m; i++) {
         ObjectIterator var6 = Object2IntMaps.fastIterable(this.transporters.get(i).getAllItems(dir, compareNBT)).iterator();

         while (var6.hasNext()) {
            Entry<ItemStack> entry = (Entry<ItemStack>)var6.next();
            items.addTo((ItemStack)entry.getKey(), entry.getIntValue());
         }
      }

      return items;
   }

   @Override
   public IItemTransporter.InvResult getInventory(Direction dir, boolean compareNBT) {
      IItemTransporter.InvResult result = new IItemTransporter.InvResult(compareNBT);
      int i = 0;

      for (int m = this.transporters.size(); i < m; i++) {
         result.add(this.transporters.get(i).getInventory(dir, compareNBT));
      }

      return result;
   }
}
