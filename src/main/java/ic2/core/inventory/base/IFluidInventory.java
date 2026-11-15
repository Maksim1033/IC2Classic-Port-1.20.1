package ic2.core.inventory.base;

import net.minecraft.world.level.material.Fluid;

public interface IFluidInventory {
   int getFluidSlots();

   void setFluidInSlot(int var1, Fluid var2);

   boolean canInsert(int var1, Fluid var2);

   Fluid getFluidInSlot(int var1);
}
