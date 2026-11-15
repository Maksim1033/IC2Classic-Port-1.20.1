package ic2.core.utils.plugins;

import net.minecraft.world.item.ItemStack;

public interface IJEIHelper {
   boolean isValid();

   void triggerUsage(ItemStack var1);

   void triggerRecipe(ItemStack var1);

   public static class Dummy implements IJEIHelper {
      @Override
      public boolean isValid() {
         return false;
      }

      @Override
      public void triggerUsage(ItemStack stack) {
      }

      @Override
      public void triggerRecipe(ItemStack stack) {
      }
   }
}
