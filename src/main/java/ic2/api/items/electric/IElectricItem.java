package ic2.api.items.electric;

import net.minecraft.world.item.ItemStack;

public interface IElectricItem {
   boolean canProvideEnergy(ItemStack var1);

   int getCapacity(ItemStack var1);

   int getTier(ItemStack var1);

   int getTransferLimit(ItemStack var1);
}
