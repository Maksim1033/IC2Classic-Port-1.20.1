package ic2.core.utils.tooltips.helper;

import ic2.core.platform.player.PlayerHandler;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EUReaderTooltip extends BasicTextToolTip {
   public EUReaderTooltip(String key, Object... values) {
      super(key, values);
   }

   public EUReaderTooltip(Component comp) {
      super(comp);
   }

   public EUReaderTooltip(Supplier<Component> supplier) {
      super(supplier);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addInformation(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      if (PlayerHandler.getClientHandler().hasEUReader()) {
         super.addInformation(stack, worldIn, tooltip, flagIn);
      }
   }
}
