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
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

public class SidedTransporter extends BaseTransporter {
   WorldlyContainer inventory;

   public SidedTransporter(WorldlyContainer inventory) {
      this.inventory = inventory;
   }

   @Override
   public int addItem(ItemStack stack, Direction dir, boolean simulate) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         int[] slots = this.inventory.getSlotsForFace(dir);
         if (slots != null && slots.length != 0) {
            int invCapacity = this.inventory.getMaxStackSize();
            IntArrayList emptySlots = new IntArrayList(slots.length);
            int stackSize = stack.getCount();
            int added = 0;

            for (int slot : slots) {
               if (this.inventory.canPlaceItem(slot, stack) && this.inventory.canPlaceItemThroughFace(slot, stack, dir)) {
                  ItemStack inv = this.inventory.getItem(slot);
                  if (inv.isEmpty()) {
                     emptySlots.add(slot);
                  } else {
                     if (StackUtil.isStackEqual(inv, stack)) {
                        int room = Math.min(inv.getMaxStackSize() - inv.getCount(), invCapacity);
                        if (room <= 0) {
                           continue;
                        }

                        int toAdd = Math.min(room, stackSize - added);
                        if (!simulate) {
                           inv.grow(toAdd);
                           this.inventory.setItem(slot, inv);
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

            int i = 0;

            for (int m = emptySlots.size(); i < m; i++) {
               int toAdd = Math.min(invCapacity, Math.min(stack.getMaxStackSize(), stackSize - added));
               if (!simulate) {
                  this.inventory.setItem(emptySlots.getInt(i), this.copyWithSize(stack, toAdd));
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
         } else {
            return 0;
         }
      }
   }

   @Override
   public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
      if (amount <= 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = ItemStack.EMPTY;
         int[] slots = this.inventory.getSlotsForFace(dir);
         if (slots == null) {
            return stack;
         } else {
            for (int slot : slots) {
               ItemStack inv = this.inventory.getItem(slot);
               if (!inv.isEmpty()
                  && this.inventory.canTakeItemThroughFace(slot, stack, dir)
                  && filter.matches(inv)
                  && (stack.isEmpty() || StackUtil.isStackEqual(stack, inv))) {
                  int toRemove = amount - stack.getCount();
                  if (simulate) {
                     if (stack.isEmpty()) {
                        stack = this.copyWithSize(inv, Math.min(toRemove, inv.getCount()));
                        continue;
                     }

                     stack.grow(Math.min(inv.getCount(), toRemove));
                  } else {
                     if (stack.isEmpty()) {
                        stack = this.inventory.removeItem(slot, toRemove);
                        continue;
                     }

                     stack.grow(this.inventory.removeItem(slot, toRemove).getCount());
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
   }

   @Override
   public int getInventorySize(Direction dir) {
      return this.inventory.getSlotsForFace(dir).length;
   }

   @Override
   public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
      int[] slots = this.inventory.getSlotsForFace(dir);
      if (slots != null && slots.length != 0) {
         Object2IntLinkedOpenCustomHashMap<ItemStack> items = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(compareNBT));

         for (int slot : slots) {
            ItemStack stack = this.inventory.getItem(slot);
            if (!stack.isEmpty()) {
               items.addTo(StackUtil.copyWithSize(stack, 1), stack.getCount());
            }
         }

         return items;
      } else {
         return Object2IntMaps.emptyMap();
      }
   }

   @Override
   public IItemTransporter.InvResult getInventory(Direction dir, boolean compareNBT) {
      IItemTransporter.InvResult result = new IItemTransporter.InvResult(compareNBT);
      int[] slots = this.inventory.getSlotsForFace(dir);

      for (int slot : slots) {
         result.add(this.inventory.getItem(slot), this.inventory.getMaxStackSize());
      }

      return result;
   }
}
