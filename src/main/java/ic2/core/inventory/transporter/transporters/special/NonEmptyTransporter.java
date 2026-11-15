package ic2.core.inventory.transporter.transporters.special;

import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.transporters.BaseTransporter;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class NonEmptyTransporter extends BaseTransporter {
   IHasInventory inventory;

   public NonEmptyTransporter(IHasInventory inventory) {
      this.inventory = inventory;
   }

   @Override
   public int addItem(ItemStack stack, Direction dir, boolean simulate) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         int invSize = this.getInventorySize(dir);
         int stackSize = stack.getCount();
         int added = 0;

         for (int i = 0; i < invSize; i++) {
            if (this.inventory.canInsert(i, stack)) {
               ItemStack inv = this.inventory.getStackInSlot(i);
               if (!inv.isEmpty()) {
                  if (StackUtil.isStackEqual(inv, stack)) {
                     int room = Math.min(inv.getMaxStackSize() - inv.getCount(), this.inventory.getMaxStackSize(i));
                     if (room <= 0) {
                        continue;
                     }

                     int toAdd = Math.min(room, stackSize - added);
                     if (!simulate) {
                        inv.grow(toAdd);
                        this.inventory.setStackInSlot(i, inv);
                     }

                     added += toAdd;
                  }

                  if (added >= stackSize) {
                     return added;
                  }
               }
            }
         }

         return added;
      }
   }

   @Override
   public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
      return ItemStack.EMPTY;
   }

   @Override
   public int getInventorySize(Direction dir) {
      return this.inventory.getSlotCount();
   }

   @Override
   public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
      int slots = this.inventory.getSlotCount();
      if (slots <= 0) {
         return Object2IntMaps.emptyMap();
      } else {
         Object2IntLinkedOpenCustomHashMap<ItemStack> items = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(compareNBT));

         for (int i = 0; i < slots; i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
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
      int slots = this.inventory.getSlotCount();
      if (slots > 0) {
         for (int i = 0; i < slots; i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               result.add(stack, this.inventory.getMaxStackSize(i));
            }
         }
      }

      return result;
   }
}
