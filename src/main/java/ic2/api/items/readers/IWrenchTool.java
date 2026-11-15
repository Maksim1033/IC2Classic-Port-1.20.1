package ic2.api.items.readers;

import net.minecraft.world.item.ItemStack;

public interface IWrenchTool {
   double getActualLoss(ItemStack var1, double var2);

   default boolean shouldRenderOverlay(ItemStack stack) {
      return true;
   }
}
