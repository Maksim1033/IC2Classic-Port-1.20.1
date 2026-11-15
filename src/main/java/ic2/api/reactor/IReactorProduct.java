package ic2.api.reactor;

import net.minecraft.world.item.ItemStack;

public interface IReactorProduct {
   boolean isValidForReactor(ItemStack var1, IReactor var2);
}
