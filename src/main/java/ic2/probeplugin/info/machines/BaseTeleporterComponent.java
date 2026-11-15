package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.core.block.machines.tiles.mv.BaseTeleporterTileEntity;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import java.util.Set;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class BaseTeleporterComponent implements ITileInfoComponent<BaseTeleporterTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseTeleporterTileEntity tile) {
      Panel teleporter = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = teleporter.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getTier()));
      basicInfo.text("ic2.probe.eu.max_in.name", EnergyNet.INSTANCE.getPowerFromTier(tile.getSinkTier()));
      basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(tile));
      Set<BaseTeleporterTileEntity.LocalTarget> targets = tile.getTargets();
      if (!targets.isEmpty()) {
         basicInfo.padding(0, 2);
         IExpandedProbeInfo targetInfo = basicInfo.vertical(IC2Styles.HEADER_STYLE);
         targetInfo.text(this.translate("gui.ic2.base_teleporter.name").append(": ").append(tile.name));
         targetInfo.text(this.translate("gui.ic2.base_teleporter.network").append(": ").append(tile.networkID));
         targetInfo.padding(0, 3);
         targetInfo.text("ic2.probe.base_teleporter.connections");
         IExpandedProbeInfo targetList = targetInfo.vertical(IC2Styles.INNER_STYLE);

         for (BaseTeleporterTileEntity.LocalTarget target : tile.getTargets()) {
            if (!target.getPos().equals(tile.getPosition())) {
               targetList.text("- " + target.getName());
            }
         }

         targetInfo.padding(0, 3);
      }

      this.addSecurely(info, 1, teleporter);
   }
}
