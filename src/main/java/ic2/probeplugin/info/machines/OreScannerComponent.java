package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.core.block.machines.tiles.hv.OreScannerTileEntity;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class OreScannerComponent implements ITileInfoComponent<OreScannerTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, OreScannerTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getTier()));
      basicInfo.text("ic2.probe.eu.max_in.name", tile.getMaxInput());
      basicInfo.text("ic2.probe.eu.usage.name", 1000);
      IExpandedProbeInfo bars = basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(tile));
      if (tile.isActive() || tile.blocks > 0) {
         bars.progress(tile.blocks, tile.maxBlocks, IC2Styles.progressBar(Math.min(60000000, tile.blocks / 25), Math.min(60000000, tile.maxBlocks / 25)));
      }

      ProbePluginHelper.generateSlotInfo(this.translate("ic2.probe.scanner.result.name"), tile.results.getFoundItems(), false, basicInfo);
      this.addSecurely(info, 1, machine);
   }
}
