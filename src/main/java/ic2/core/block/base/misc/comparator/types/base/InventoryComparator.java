package ic2.core.block.base.misc.comparator.types.base;

import ic2.core.block.base.misc.comparator.BaseComparator;
import ic2.core.inventory.base.IHasInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class InventoryComparator extends BaseComparator {
   IHasInventory inventory;
   boolean slots;

   public InventoryComparator(String id, Component name, IHasInventory inventory, boolean slots) {
      super(id, name);
      this.inventory = inventory;
      this.slots = slots;
   }

   @Override
   protected int createValue() {
      if (this.slots) {
         int full = 0;
         int i = 0;

         for (int m = this.inventory.getSlotCount(); i < m; i++) {
            full += this.inventory.getStackInSlot(i).isEmpty() ? 0 : 1;
         }

         return value((float)full, (float)this.inventory.getSlotCount(), 15);
      } else {
         int total = 0;
         int count = 0;
         int i = 0;

         for (int m = this.inventory.getSlotCount(); i < m; i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
               total += this.inventory.getMaxStackSize(i);
            } else {
               total += Math.min(stack.getMaxStackSize(), this.inventory.getMaxStackSize(i));
               count = stack.getCount();
            }
         }

         return value((float)count, (float)total, 15);
      }
   }
}
