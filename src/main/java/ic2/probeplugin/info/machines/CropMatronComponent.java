package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.core.block.machines.tiles.lv.CropMatronTileEntity;
import ic2.core.inventory.handler.SlotType;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class CropMatronComponent implements ITileInfoComponent<CropMatronTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, CropMatronTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getTier()));
      basicInfo.text("ic2.probe.eu.max_in.name", tile.getMaxInput());
      ProbePluginHelper.addTanks(tile, basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(tile)), true);
      ProbePluginHelper.generateSlots(tile, false, machine, SlotType.FERTILIZER, SlotType.HYDRATION, SlotType.WEED_EX);
      this.addSecurely(info, 1, machine);
   }
}
