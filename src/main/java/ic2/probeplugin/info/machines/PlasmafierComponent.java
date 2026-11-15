package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.core.block.machines.tiles.ev.PlasmafierTileEntity;
import ic2.core.inventory.handler.SlotType;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class PlasmafierComponent implements ITileInfoComponent<PlasmafierTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, PlasmafierTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getTier()));
      basicInfo.text("ic2.probe.eu.max_in.name", tile.getMaxInput());
      basicInfo.text("ic2.probe.eu.usage.name", 10240);
      IExpandedProbeInfo bars = basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(tile));
      if (tile.isActive() || tile.plasma > 0) {
         bars.progress(tile.plasma, 10000, IC2Styles.UU_BAR.copy().prefix("ic2.probe.plasma.name", new Object[]{tile.plasma}));
      }

      if (tile.uuMatter > 0) {
         bars.progress(tile.uuMatter, 150, IC2Styles.UU_BAR.copy().prefix("ic2.probe.matter.name", new Object[]{tile.uuMatter}));
      }

      ProbePluginHelper.generateSlots(tile, false, machine, SlotType.INPUT, SlotType.CONTAINER, SlotType.OUTPUT);
      this.addSecurely(info, 1, machine);
   }
}
