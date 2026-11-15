package ic2.probeplugin.info.electric;

import ic2.api.energy.EnergyNet;
import ic2.core.block.base.tiles.impls.BaseEnergyStorageTileEntity;
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

public class BaseEnergyStorageComponent implements ITileInfoComponent<BaseEnergyStorageTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseEnergyStorageTileEntity tile) {
      Panel main = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = main.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getSourceTier()));
      basicInfo.text("ic2.probe.eu.output.name", tile.getProvidedEnergy());
      CableProvider.EnergyContainer result = CableProvider.getContainer(tile);
      if (result.getAverageOut() > 0 || result.getAverageIn() > 0) {
         if (result.getAverageIn() > 0) {
            basicInfo.text(
               this.translate("tooltip.item.ic2.eu_reader.cable_flow_in", new Object[]{Formatters.EU_FORMAT.format((long)result.getAverageIn())})
                  .withStyle(ChatFormatting.AQUA)
            );
         }

         if (result.getAverageOut() > 0) {
            basicInfo.text(
               this.translate("tooltip.item.ic2.eu_reader.cable_flow_out", new Object[]{Formatters.EU_FORMAT.format((long)result.getAverageOut())})
                  .withStyle(ChatFormatting.AQUA)
            );
         }

         if (result.getPacketsIn() > 0) {
            basicInfo.text(
               this.translate("tooltip.item.ic2.eu_reader.packet_flow_in", new Object[]{Formatters.EU_READER_FORMAT.format((long)result.getPacketsIn())})
                  .withStyle(ChatFormatting.AQUA)
            );
         }

         if (result.getPacketsOut() > 0) {
            basicInfo.text(
               this.translate("tooltip.item.ic2.eu_reader.packet_flow_out", new Object[]{Formatters.EU_READER_FORMAT.format((long)result.getPacketsOut())})
                  .withStyle(ChatFormatting.AQUA)
            );
         }
      }

      basicInfo.vertical(IC2Styles.BARS_STYLE).element(ProbePluginHelper.generateEUBar(tile));
      this.addSecurely(info, 1, main);
   }
}
