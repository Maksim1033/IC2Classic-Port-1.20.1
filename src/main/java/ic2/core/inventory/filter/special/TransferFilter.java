package ic2.core.inventory.filter.special;

import ic2.core.inventory.filter.IFilter;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.world.item.ItemStack;

public class TransferFilter implements IFilter {
   ItemStack stack;
   int flags;
   int durability;
   boolean ignoreDurability;

   public TransferFilter(ItemStack stack, int flags, int durability) {
      this.stack = stack;
      this.flags = flags;
      this.durability = durability;
      this.ignoreDurability = !stack.isDamageableItem() || (flags & 256) == 0;
   }

   @Override
   public boolean matches(ItemStack input) {
      if (!StackUtil.isStackEqual(this.stack, input, this.flags)) {
         return false;
      } else if (this.ignoreDurability) {
         return true;
      } else {
         switch (this.durability) {
            case 0:
               return this.stack.getDamageValue() == input.getDamageValue();
            case 1:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() < 0.01;
            case 2:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() > 0.01;
            case 3:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() < 0.25;
            case 4:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() > 0.25;
            case 5:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() < 0.5;
            case 6:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() > 0.5;
            case 7:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() < 0.75;
            case 8:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() > 0.75;
            case 9:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() < 0.9;
            case 10:
               return (double)input.getDamageValue() / (double)input.getMaxDamage() > 0.9;
            default:
               return true;
         }
      }
   }
}
