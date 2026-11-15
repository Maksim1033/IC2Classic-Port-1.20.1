package ic2.core.inventory.handler.filter;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ISlotFilter {
   boolean matches(int var1, ItemStack var2);
}
