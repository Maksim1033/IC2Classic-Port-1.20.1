package ic2.core.block.base.misc.comparator.types.base;

import ic2.core.block.base.misc.comparator.types.BaseDirectionalComparator;
import ic2.core.inventory.handler.IHasInventoryHandler;
import ic2.core.inventory.handler.InventoryHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class DirectionalInventoryComparator extends BaseDirectionalComparator {
   IHasInventoryHandler inv;
   boolean slots;

   public DirectionalInventoryComparator(String id, Component name, IHasInventoryHandler inv, boolean slots) {
      super(id, name);
      this.inv = inv;
      this.slots = slots;
   }

   @Override
   protected int createValue(Direction dir) {
      InventoryHandler handler = this.inv.getInventoryHandler();
      IItemHandler inv = (IItemHandler)handler.getHandler(dir).orElse(EmptyHandler.INSTANCE);
      if (inv.getSlots() == 0) {
         return 0;
      } else if (this.slots) {
         int full = 0;
         int i = 0;

         for (int m = inv.getSlots(); i < m; i++) {
            full += inv.getStackInSlot(i).isEmpty() ? 0 : 1;
         }

         return value((float)full, (float)inv.getSlots(), 15);
      } else {
         int total = 0;
         int count = 0;
         int i = 0;

         for (int m = inv.getSlots(); i < m; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
               total += inv.getSlotLimit(i);
            } else {
               total += Math.min(stack.getMaxStackSize(), inv.getSlotLimit(i));
               count = stack.getCount();
            }
         }

         return value((float)count, (float)total, 15);
      }
   }
}
