package ic2.api.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface IRepairable extends ItemLike {
   boolean repairDamage(ItemStack var1, int var2);
}
