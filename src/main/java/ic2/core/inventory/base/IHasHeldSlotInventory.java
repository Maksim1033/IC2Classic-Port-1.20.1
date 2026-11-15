package ic2.core.inventory.base;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface IHasHeldSlotInventory extends IHasHeldGui {
   default boolean hasInventory(ItemStack stack) {
      return true;
   }

   IPortableInventory getInventory(Player var1, ItemStack var2, Slot var3);
}
