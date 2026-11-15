package ic2.probeplugin.info.electric;

import ic2.api.util.DirectionList;
import ic2.core.block.storage.tiles.RedirectorMasterTileEntity;
import ic2.core.utils.helpers.Formatters;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.info.transport.CableProvider;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class RedirectorMasterComponent implements ITileInfoComponent<RedirectorMasterTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, RedirectorMasterTileEntity tile) {
      Panel generator = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = generator.vertical(IC2Styles.INNER_STYLE);

      for (Direction side : DirectionList.ALL) {
         int value = tile.shares[side.get3DDataValue()];
         if (value > 0) {
            basicInfo.text(DirectionList.getName(side).append(": " + value + "%"));
         }
      }

      CableProvider.EnergyContainer result = CableProvider.getContainer(tile);
      if (result.getAverageIn() > 0) {
         basicInfo.padding(0, 5);
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.cable_flow", new Object[]{Formatters.EU_FORMAT.format((long)result.getAverageIn())})
               .withStyle(ChatFormatting.AQUA)
         );
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.packet_flow", new Object[]{Formatters.EU_FORMAT.format((long)result.getPacketsIn())})
               .withStyle(ChatFormatting.AQUA)
         );
      }

      this.addSecurely(info, 1, generator);
   }
}
