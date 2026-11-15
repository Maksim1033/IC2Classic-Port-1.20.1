package ic2.core.inventory.base;

import net.minecraft.world.item.ItemStack;

public interface IPortableInventory extends IHasGui, IHasInventory {
   ItemStack getInventoryStack();

   IPortableInventory load(ItemStack var1);
}
