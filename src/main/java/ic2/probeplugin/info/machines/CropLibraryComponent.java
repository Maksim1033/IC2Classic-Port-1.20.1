package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.tiles.readers.IEUStorage;
import ic2.core.block.base.tiles.impls.BaseCropLibraryTileEntity;
import ic2.core.block.machines.logic.crop.SeedStorage;
import ic2.core.utils.helpers.StackUtil;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class CropLibraryComponent implements ITileInfoComponent<BaseCropLibraryTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseCropLibraryTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      if (tile instanceof IEnergySink) {
         basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(((IEnergySink)tile).getSinkTier()));
         basicInfo.text("ic2.probe.eu.max_in.name", EnergyNet.INSTANCE.getPowerFromTier(((IEnergySink)tile).getSinkTier()));
         basicInfo.text("ic2.probe.eu.usage.name", 1);
      }

      SeedStorage storage = tile.storage;
      if (storage.getTypeLimit() != -1) {
         basicInfo.text("ic2.probe.crop_library.type.name", tile.syncer.getCropCount(), storage.getTypeLimit());
         basicInfo.text("ic2.probe.crop_library.stat.name", tile.syncer.getStatCount(), storage.getStatLimit() * storage.getTypeLimit());
         basicInfo.text("ic2.probe.crop_library.size.name", storage.getSizeLimit());
      }

      if (tile instanceof IEUStorage) {
         basicInfo.vertical(IC2Styles.BARS_STYLE).element(ProbePluginHelper.generateHiddenBar(tile));
      }

      ProbePluginHelper.generateSlotInfo(this.translate("ic2.probe.crop_library.name"), StackUtil.copyNonEmpty(storage.getTypes()), false, basicInfo);
      this.addSecurely(info, 1, machine);
   }
}
