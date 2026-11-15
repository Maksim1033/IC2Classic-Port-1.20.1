package ic2.probeplugin.info;

import ic2.api.items.readers.IWrenchTool;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.IC2Items;
import ic2.probeplugin.override.components.Panel;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WrenchComponent implements ITileInfoComponent<IWrenchableTile> {
   @Override
   public boolean hasValidReader(PlayerHandler handler) {
      return true;
   }

   public void addInfo(IProbeInfo info, Player player, Direction dir, IWrenchableTile tile) {
      if (tile.canRemoveBlock(player) && tile.isHarvestWrenchRequired(player)) {
         ItemStack tool = this.getTool(player);
         double chance = tool.getItem() instanceof IWrenchTool ? ((IWrenchTool)tool.getItem()).getActualLoss(tool, tile.getDropRate(player)) : 0.0;
         Panel panel = new Panel(Panel.Type.HORIZONTAL);
         panel.item(new ItemStack(IC2Items.WRENCH))
            .text(Component.translatable(Mth.floor(chance * 100.0) + "% Drop Chance").withStyle(ChatFormatting.GRAY), panel.defaultTextStyle().topPadding(5));
         info.element(panel);
      }
   }

   private ItemStack getTool(Player player) {
      ItemStack held = player.getMainHandItem();
      return held.getItem() instanceof IWrenchTool ? held : player.getOffhandItem();
   }
}
