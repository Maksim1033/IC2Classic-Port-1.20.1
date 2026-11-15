package ic2.core.utils.tooltips;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IAdvancedTooltip {
   @OnlyIn(Dist.CLIENT)
   void addToolTip(ItemStack var1, Player var2, TooltipFlag var3, ToolTipHelper var4);
}
