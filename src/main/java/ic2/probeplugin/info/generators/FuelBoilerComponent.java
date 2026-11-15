package ic2.probeplugin.info.generators;

import ic2.core.block.generators.tiles.FuelBoilerTileEntity;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FuelBoilerComponent implements ITileInfoComponent<FuelBoilerTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, FuelBoilerTileEntity tile) {
      Panel generator = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = generator.vertical(IC2Styles.INNER_STYLE);
      IExpandedProbeInfo bars = basicInfo.vertical(IC2Styles.BARS_STYLE);
      bars.progress(tile.getFuel(), tile.getMaxFuel(), IC2Styles.FUEL_BAR);
      bars.progress(tile.heat, 24000, IC2Styles.reactorBar(tile.heat / 30, 800));
      ProbePluginHelper.addTanks((BlockEntity)tile, bars, true);
      ProbePluginHelper.generateDefaultSlots(tile, false, generator);
      this.addSecurely(info, 1, generator);
   }
}
