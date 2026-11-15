package ic2.probeplugin.info.transport;

import ic2.api.energy.EnergyNet;
import ic2.core.block.transport.fluid.tiles.ElectricPipePumpTileEntity;
import ic2.core.block.transport.fluid.tiles.SimplePipePumpTileEntity;
import ic2.core.utils.helpers.Formatters;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class PipePumpProvider implements ITileInfoComponent<SimplePipePumpTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, SimplePipePumpTileEntity tile) {
      Panel electric = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = electric.vertical(IC2Styles.INNER_STYLE);
      if (tile instanceof ElectricPipePumpTileEntity pump) {
         basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(pump.getTier()));
         basicInfo.text("ic2.probe.eu.max_in.name", EnergyNet.INSTANCE.getPowerFromTier(pump.getTier()));
         electric.vertical().element(ProbePluginHelper.generateHiddenBar(tile));
      }

      basicInfo.text(this.translate("ic2.probe.pump.pressure", new Object[]{tile.getPressure()}));
      basicInfo.text(
         this.translate(
            "ic2.probe.pump.amount",
            new Object[]{Formatters.EU_FORMAT.format((long)(tile.getDrainAmount() / 20)), Formatters.EU_FORMAT.format((long)(tile.getMaxDrain() / 20))}
         )
      );
      basicInfo.text(this.translate("ic2.probe.pump.active", new Object[]{this.translate(tile.getPumpedAmount() > 0 ? "misc.ic2.yes" : "misc.ic2.no")}));
      if (tile instanceof ElectricPipePumpTileEntity) {
         basicInfo.text(this.translate("ic2.probe.eu.usage.name", new Object[]{tile.getPumpedAmount() * 100}));
      }

      this.addSecurely(info, 1, electric);
   }
}
