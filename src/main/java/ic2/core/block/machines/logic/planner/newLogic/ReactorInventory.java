package ic2.core.block.machines.logic.planner.newLogic;

import ic2.core.inventory.inv.SimpleInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ReactorInventory extends SimpleInventory {
   ReactorLogic logic;

   public ReactorInventory(int size, ReactorLogic logic) {
      super(size);
      this.logic = logic;
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      this.logic.setStackInSlot(slot, stack);
   }

   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt);
      int i = 0;

      for (int m = this.getSlotCount(); i < m; i++) {
         this.logic.setStackInSlot(i, (ItemStack)this.slots.get(i));
      }
   }

   @Override
   public void clear() {
      super.clear();
      int i = 0;

      for (int m = this.getSlotCount(); i < m; i++) {
         this.logic.setStackInSlot(i, ItemStack.EMPTY);
      }
   }
}
