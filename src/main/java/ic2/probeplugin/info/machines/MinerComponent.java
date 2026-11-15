package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.core.block.machines.tiles.hv.RocketMinerTileEntity;
import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class MinerComponent implements ITileInfoComponent<MinerTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, MinerTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getTier()));
      basicInfo.text("ic2.probe.eu.max_in.name", tile.getMaxInput());
      basicInfo.text("ic2.probe.eu.usage.name", tile.getEnergyUsage());
      if (tile instanceof RocketMinerTileEntity miner) {
         basicInfo.text(miner.state.getState());
      } else {
         basicInfo.text(
            tile.isStuck() ? "ic2.probe.miner.stuck.name" : (tile.isOperating() ? "ic2.probe.miner.mining.name" : "ic2.probe.miner.retracting.name")
         );
      }

      IExpandedProbeInfo bars = basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(tile));
      if (!tile.isStuck()) {
         bars.progress(
            (int)tile.getProgress(),
            (int)tile.getMaxProgress(),
            IC2Styles.progressBar((int)Math.min(6.0E7F, tile.getProgress()), (int)Math.min(6.0E7F, tile.getMaxProgress()))
         );
      }

      int y = tile.getPipeTip().getY();
      bars.progress(
         y - tile.getWorldObj().getMinBuildHeight(),
         tile.getPosition().getY() - tile.getWorldObj().getMinBuildHeight(),
         IC2Styles.SPEED_BAR.copy().prefix("ic2.probe.miner.progress.name", new Object[]{y})
      );
      ProbePluginHelper.generateAnySlots(tile, false, machine);
      this.addSecurely(info, 1, machine);
   }
}
