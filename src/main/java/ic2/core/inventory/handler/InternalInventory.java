package ic2.core.inventory.handler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class InternalInventory implements IItemHandler {
   InventoryHandler handler;
   IntList slots;

   public InternalInventory(InventoryHandler theHandler) {
      this.handler = theHandler;
      this.slots = new IntArrayList(theHandler.allSlots.keySet());
   }

   public int getSlots() {
      return this.slots.size();
   }

   public ItemStack getStackInSlot(int slot) {
      return this.handler.getInventory().getStackInSlot(this.slots.getInt(slot));
   }

   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      return stack;
   }

   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return ItemStack.EMPTY;
   }

   public int getSlotLimit(int slot) {
      return 64;
   }

   public boolean isItemValid(int slot, ItemStack stack) {
      return false;
   }
}
