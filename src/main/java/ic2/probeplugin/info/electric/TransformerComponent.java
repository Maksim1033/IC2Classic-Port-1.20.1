package ic2.probeplugin.info.electric;

import ic2.core.block.base.tiles.impls.BaseTransformerTileEntity;
import ic2.core.utils.helpers.Formatters;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.info.transport.CableProvider;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.apiimpl.elements.ElementVertical;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class TransformerComponent implements ITileInfoComponent<BaseTransformerTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseTransformerTileEntity tile) {
      ElementVertical machine = new ElementVertical(IC2Styles.OUTER_STYLE);
      IProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.inverted.name", new Object[]{this.translate(tile.isActive() ? "misc.ic2.yes" : "misc.ic2.no")});
      basicInfo.text("ic2.probe.eu.max_in.name", new Object[]{tile.isActive() ? tile.lowOutput : tile.highOutput});
      basicInfo.text("ic2.probe.eu.output.max.name", new Object[]{tile.isActive() ? tile.highOutput : tile.lowOutput});
      basicInfo.text("ic2.probe.transformer.packets.name", new Object[]{tile.isActive() ? 1 : 4});
      CableProvider.EnergyContainer result = CableProvider.getContainer(tile);
      if (result.getAverageOut() > 0) {
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.cable_flow", new Object[]{Formatters.EU_FORMAT.format((long)result.getAverageOut())})
               .withStyle(ChatFormatting.AQUA)
         );
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.packet_flow", new Object[]{Formatters.EU_FORMAT.format((long)result.getPacketsOut())})
               .withStyle(ChatFormatting.AQUA)
         );
      }

      basicInfo.vertical(IC2Styles.BARS_STYLE).element(ProbePluginHelper.generateEUBar(tile));
      this.addSecurely(info, 1, machine);
   }
}
