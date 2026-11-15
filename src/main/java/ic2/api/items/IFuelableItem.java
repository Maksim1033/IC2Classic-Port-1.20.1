package ic2.api.items;

import net.minecraft.world.item.ItemStack;

public interface IFuelableItem {
   ItemStack fill(ItemStack var1, int var2);

   boolean canFuel(ItemStack var1);

   boolean hasFuel(ItemStack var1);

   int getFuel(ItemStack var1, int var2, boolean var3);
}
