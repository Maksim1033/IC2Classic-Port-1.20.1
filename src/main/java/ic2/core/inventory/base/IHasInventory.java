package ic2.core.inventory.base;

import net.minecraft.world.item.ItemStack;

public interface IHasInventory {
   int getSlotCount();

   ItemStack getStackInSlot(int var1);

   void setStackInSlot(int var1, ItemStack var2);

   int getMaxStackSize(int var1);

   boolean canInsert(int var1, ItemStack var2);

   boolean canExtract(int var1, ItemStack var2);
}
