package ic2.core.block.base.drops;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public interface IRarityProvider {
   Rarity getRarity(ItemStack var1);
}
