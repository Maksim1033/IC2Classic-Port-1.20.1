package ic2.core.inventory.filter;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IFilter {
   boolean matches(ItemStack var1);
}
