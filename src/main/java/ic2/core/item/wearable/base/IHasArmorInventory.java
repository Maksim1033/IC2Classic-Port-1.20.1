package ic2.core.item.wearable.base;

import ic2.core.inventory.base.IHasHeldSlotInventory;
import net.minecraft.world.item.ItemStack;

public interface IHasArmorInventory extends IHasHeldSlotInventory {
   default boolean hasArmorInventory(ItemStack armor, ItemStack stack) {
      return true;
   }
}
