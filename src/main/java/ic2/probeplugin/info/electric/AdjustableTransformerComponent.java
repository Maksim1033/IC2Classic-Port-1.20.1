package ic2.probeplugin.info.electric;

import ic2.api.energy.EnergyNet;
import ic2.core.block.storage.tiles.transformer.AdjustableTransformerTileEntity;
import ic2.core.utils.helpers.Formatters;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.info.transport.CableProvider;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class AdjustableTransformerComponent implements ITileInfoComponent<AdjustableTransformerTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, AdjustableTransformerTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.max_in.name", EnergyNet.INSTANCE.getPowerFromTier(tile.getSinkTier()));
      basicInfo.text("ic2.probe.eu.output.max.name", tile.energyPacket);
      basicInfo.text("ic2.probe.transformer.packets.name", tile.packetCount);
      CableProvider.EnergyContainer result = CableProvider.getContainer(tile);
      if (result.getAverageOut() > 0) {
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.cable_flow", new Object[]{Formatters.EU_FORMAT.format((long)result.getAverageOut())})
               .withStyle(ChatFormatting.AQUA)
         );
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.packet_flow", new Object[]{Formatters.EU_READER_FORMAT.format((long)result.getPacketsOut())})
               .withStyle(ChatFormatting.AQUA)
         );
      }

      basicInfo.vertical(IC2Styles.BARS_STYLE).element(ProbePluginHelper.generateEUBar(tile));
      this.addSecurely(info, 1, machine);
   }
}
