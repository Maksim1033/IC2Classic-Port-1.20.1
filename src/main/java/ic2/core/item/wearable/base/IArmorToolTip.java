package ic2.core.item.wearable.base;

import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IArmorToolTip {
   @OnlyIn(Dist.CLIENT)
   void addToolTip(ItemStack var1, ItemStack var2, Player var3, TooltipFlag var4, ToolTipHelper var5);
}
