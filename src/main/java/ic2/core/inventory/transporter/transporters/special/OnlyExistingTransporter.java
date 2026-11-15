package ic2.core.inventory.transporter.transporters.special;

import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class OnlyExistingTransporter implements IItemTransporter {
   IItemHandler handler;

   public OnlyExistingTransporter(IItemHandler handler) {
      this.handler = handler;
   }

   @Override
   public int addItem(ItemStack stack, Direction dir, boolean simulate) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         int size = this.handler.getSlots();
         if (size <= 0) {
            return 0;
         } else {
            int stackSize = stack.getCount();
            IntList emptySlots = new IntArrayList(size);
            boolean found = false;
            int added = 0;

            for (int i = 0; i < size; i++) {
               ItemStack inv = this.handler.getStackInSlot(i);
               if (inv.isEmpty()) {
                  emptySlots.add(i);
               } else if (StackUtil.isStackEqual(inv, stack)) {
                  found = true;
                  int adding = stackSize - added;
                  added += adding - this.handler.insertItem(i, StackUtil.copyWithSize(stack, adding), simulate).getCount();
                  if (added >= stackSize) {
                     return added;
                  }
               } else if (!found && inv.getItem() == stack.getItem()) {
                  found = true;
               }
            }

            if (!found) {
               return added;
            } else {
               size = emptySlots.size();

               for (int ix = 0; ix < size; ix++) {
                  int adding = stackSize - added;
                  added += adding - this.handler.insertItem(emptySlots.getInt(ix), StackUtil.copyWithSize(stack, adding), simulate).getCount();
                  if (added >= stackSize) {
                     return added;
                  }
               }

               return added;
            }
         }
      }
   }

   @Override
   public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
      return ItemStack.EMPTY;
   }

   @Override
   public int getInventorySize(Direction dir) {
      return this.handler.getSlots();
   }

   @Override
   public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
      int slots = this.handler.getSlots();
      if (slots <= 0) {
         return Object2IntMaps.emptyMap();
      } else {
         Object2IntLinkedOpenCustomHashMap<ItemStack> items = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(compareNBT));

         for (int i = 0; i < slots; i++) {
            ItemStack stack = this.handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
               items.addTo(StackUtil.copyWithSize(stack, 1), stack.getCount());
            }
         }

         return items;
      }
   }

   @Override
   public IItemTransporter.InvResult getInventory(Direction dir, boolean compareNBT) {
      IItemTransporter.InvResult result = new IItemTransporter.InvResult(compareNBT);
      int slots = this.handler.getSlots();
      if (slots > 0) {
         for (int i = 0; i < slots; i++) {
            ItemStack stack = this.handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
               result.add(stack, this.handler.getSlotLimit(i));
            }
         }
      }

      return result;
   }
}
