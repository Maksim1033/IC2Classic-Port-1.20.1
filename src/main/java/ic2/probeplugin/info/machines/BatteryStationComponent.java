package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.core.block.base.tiles.impls.BaseBatteryStationTileEntity;
import ic2.core.utils.helpers.Formatters;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.info.transport.CableProvider;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.NumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class BatteryStationComponent implements ITileInfoComponent<BaseBatteryStationTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseBatteryStationTileEntity tile) {
      Panel generator = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = generator.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getSourceTier()));
      basicInfo.text("ic2.probe.eu.output.max.name", tile.getMaxEnergyOutput());
      CableProvider.EnergyContainer result = CableProvider.getContainer(tile);
      if (result.getAverageOut() > 0) {
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.cable_flow_out", new Object[]{Formatters.EU_FORMAT.format((long)result.getAverageOut())})
               .withStyle(ChatFormatting.AQUA)
         );
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.packet_flow_out", new Object[]{Formatters.EU_FORMAT.format((long)result.getPacketsOut())})
               .withStyle(ChatFormatting.AQUA)
         );
      }

      IExpandedProbeInfo bars = basicInfo.vertical(IC2Styles.BARS_STYLE).element(ProbePluginHelper.generateEUBar(tile, 125));
      int missing = tile.getMissingEnergy().getIntKey();
      if (missing > 0) {
         int slots = 0;

         for (int i = 0; i < 16; i++) {
            if (!tile.getStackInSlot(i).isEmpty()) {
               slots++;
            }
         }

         int average = Math.min(tile.averager.getAverage(), missing);
         bars.progress(
            slots - tile.getChargingSlots(),
            slots,
            IC2Styles.PROGRESS_BAR
               .copy()
               .numberFormat(NumberFormat.NONE)
               .prefix(
                  "ic2.probe.discharging.eta.name",
                  new Object[]{DurationFormatUtils.formatDuration(average <= 0 ? 0L : (long)(missing / average) * 50L, "HH:mm:ss")}
               )
               .suffix(Component.empty())
               .width(125)
         );
      }

      ProbePluginHelper.generateDefaultSlots(tile, false, generator);
      this.addSecurely(info, 1, generator);
   }
}
