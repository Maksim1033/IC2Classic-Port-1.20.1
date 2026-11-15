package ic2.api.items.readers;

import net.minecraft.world.item.ItemStack;

public interface IThermometer {
   boolean isThermometer(ItemStack var1);

   static boolean isThermometerImpl(ItemStack stack) {
      return stack.getItem() instanceof IThermometer && ((IThermometer)stack.getItem()).isThermometer(stack);
   }
}
