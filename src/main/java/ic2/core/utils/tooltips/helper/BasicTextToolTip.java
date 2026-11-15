package ic2.core.utils.tooltips.helper;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BasicTextToolTip implements ITooltipProvider {
   protected Component comp;
   protected Supplier<Component> supplier;

   public BasicTextToolTip(String key, Object... values) {
      this.comp = this.translate(key, values).withStyle(ChatFormatting.GRAY);
   }

   public BasicTextToolTip(Component comp) {
      this.comp = comp.copy().withStyle(ChatFormatting.GRAY);
   }

   public BasicTextToolTip(Supplier<Component> supplier) {
      this.supplier = supplier;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addInformation(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      tooltip.add(this.supplier != null ? this.supplier.get() : this.comp);
   }
}
