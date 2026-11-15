package ic2.core.inventory.transporter.transporters;

import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class SimpleTransporter extends BaseTransporter {
   Container inventory;

   public SimpleTransporter(Container inventory) {
      this.inventory = inventory;
   }

   @Override
   public int addItem(ItemStack stack, Direction dir, boolean simulate) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         int invSize = this.getInventorySize(dir);
         int invCapacity = this.inventory.getMaxStackSize();
         IntArrayList emptySlots = new IntArrayList(invSize);
         int stackSize = stack.getCount();
         int added = 0;

         for (int i = 0; i < invSize; i++) {
            if (this.inventory.canPlaceItem(i, stack)) {
               ItemStack inv = this.inventory.getItem(i);
               if (inv.isEmpty()) {
                  emptySlots.add(i);
               } else {
                  if (StackUtil.isStackEqual(inv, stack)) {
                     int room = Math.min(inv.getMaxStackSize() - inv.getCount(), invCapacity);
                     if (room <= 0) {
                        continue;
                     }

                     int toAdd = Math.min(room, stackSize - added);
                     if (!simulate) {
                        inv.grow(toAdd);
                        this.inventory.setItem(i, inv);
                     }

                     added += toAdd;
                  }

                  if (added >= stackSize) {
                     if (!simulate) {
                        this.inventory.setChanged();
                     }

                     return added;
                  }
               }
            }
         }

         int ix = 0;

         for (int m = emptySlots.size(); ix < m; ix++) {
            int toAdd = Math.min(invCapacity, Math.min(stack.getMaxStackSize(), stackSize - added));
            if (!simulate) {
               this.inventory.setItem(emptySlots.getInt(ix), this.copyWithSize(stack, toAdd));
            }

            added += toAdd;
            if (added >= stackSize) {
               if (!simulate) {
                  this.inventory.setChanged();
               }

               return added;
            }
         }

         return added;
      }
   }

   @Override
   public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
      if (amount <= 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = ItemStack.EMPTY;
         int invSize = this.getInventorySize(dir);

         for (int i = 0; i < invSize; i++) {
            ItemStack inv = this.inventory.getItem(i);
            if (!inv.isEmpty() && filter.matches(inv) && (stack.isEmpty() || StackUtil.isStackEqual(stack, inv))) {
               int toRemove = amount - stack.getCount();
               if (simulate) {
                  if (stack.isEmpty()) {
                     stack = this.copyWithSize(inv, Math.min(toRemove, inv.getCount()));
                  } else {
                     stack.grow(Math.min(inv.getCount(), toRemove));
                  }
               } else if (stack.isEmpty()) {
                  stack = this.inventory.removeItem(i, toRemove);
               } else {
                  stack.grow(this.inventory.removeItem(i, toRemove).getCount());
               }

               if (stack.getCount() >= amount) {
                  if (!simulate) {
                     this.inventory.setChanged();
                  }

                  return stack;
               }
            }
         }

         return stack;
      }
   }

   @Override
   public int getInventorySize(Direction dir) {
      return this.inventory.getContainerSize();
   }

   @Override
   public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
      int slots = this.inventory.getContainerSize();
      if (slots <= 0) {
         return Object2IntMaps.emptyMap();
      } else {
         Object2IntLinkedOpenCustomHashMap<ItemStack> items = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(compareNBT));

         for (int i = 0; i < slots; i++) {
            ItemStack stack = this.inventory.getItem(i);
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
      int slots = this.inventory.getContainerSize();
      if (slots > 0) {
         for (int i = 0; i < slots; i++) {
            result.add(this.inventory.getItem(i), this.inventory.getMaxStackSize());
         }
      }

      return result;
   }
}
