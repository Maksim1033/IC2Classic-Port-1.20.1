package ic2.core.item.base.features;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IHandlerItem {
   void handleInventory(ItemStack var1, IHandlerItem.IConfigurableInventory var2);

   public interface IConfigurableInventory {
      void setDefaultMaxStackSize(int var1);

      void setMaxStackSizeForItem(Item var1, int var2);
   }
}
