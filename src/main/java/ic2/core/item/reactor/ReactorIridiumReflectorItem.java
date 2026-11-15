package ic2.core.item.reactor;

import ic2.api.items.IRepairable;
import net.minecraft.world.item.ItemStack;

public class ReactorIridiumReflectorItem extends ReactorReflectorItem implements IRepairable {
   public ReactorIridiumReflectorItem() {
      super("reflector_iridium", "reactor/reflector", "iridium_neutron", 320000, true, 53);
   }

   @Override
   public boolean repairDamage(ItemStack stack, int amount) {
      if (stack.getDamageValue() > 0) {
         stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
         return true;
      } else {
         return false;
      }
   }
}
