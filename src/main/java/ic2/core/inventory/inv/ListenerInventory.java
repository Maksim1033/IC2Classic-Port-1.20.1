package ic2.core.inventory.inv;

import net.minecraft.world.item.ItemStack;

public class ListenerInventory extends SimpleInventory {
   INotifyInventory listener;

   public ListenerInventory(int size, INotifyInventory listener) {
      super(size);
      this.listener = listener;
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.listener != null) {
         this.listener.onNotify(this, slot);
      }
   }
}
