package ic2.api.items.electric;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.extensions.IForgeItem;

public interface IElectricEnchantable extends IForgeItem {
   InteractionResult getEnchantmentCompatibility(ItemStack var1, Enchantment var2);

   EnchantmentCategory getEnchantmentType(ItemStack var1);

   default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return false;
   }
}
