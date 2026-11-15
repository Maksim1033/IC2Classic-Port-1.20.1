package ic2.core.block.base.misc.color;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IItemColorListener extends IColorListener {
   @OnlyIn(Dist.CLIENT)
   int getItemColor(ItemStack var1, int var2);
}
