package ic2.probeplugin.info.electric;

import ic2.api.energy.EnergyNet;
import ic2.core.block.base.tiles.impls.BaseElectricLoaderTileEntity;
import ic2.core.inventory.handler.SlotType;
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

public class ElectricLoaderComponent implements ITileInfoComponent<BaseElectricLoaderTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseElectricLoaderTileEntity tile) {
      Panel generator = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = generator.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getSinkTier()));
      basicInfo.text("ic2.probe.eu.max_in.name", tile.getMaxInput());
      CableProvider.EnergyContainer result = CableProvider.getContainer(tile);
      if (result.getAverageIn() > 0) {
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.cable_flow_in", new Object[]{Formatters.EU_FORMAT.format((long)result.getAverageIn())})
               .withStyle(ChatFormatting.AQUA)
         );
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.packet_flow_in", new Object[]{Formatters.EU_FORMAT.format((long)result.getPacketsIn())})
               .withStyle(ChatFormatting.AQUA)
         );
      }

      basicInfo.vertical(IC2Styles.BARS_STYLE).element(ProbePluginHelper.generateEUBar(tile, 125));
      ProbePluginHelper.generateSlots(tile, false, generator, SlotType.BATTERY);
      this.addSecurely(info, 1, generator);
   }
}
