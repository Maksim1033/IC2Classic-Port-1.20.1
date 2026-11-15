package ic2.api.crops;

import net.minecraft.world.item.ItemStack;

public interface ICropModifier {
   boolean canChangeSeedMode(ItemStack var1);

   static boolean canToggleSeedMode(ItemStack stack) {
      if (stack.getItem() instanceof ICropModifier mod && mod.canChangeSeedMode(stack)) {
         return true;
      }

      return false;
   }
}
