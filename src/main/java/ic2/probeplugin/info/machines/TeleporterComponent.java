package ic2.probeplugin.info.machines;

import ic2.core.IC2;
import ic2.core.block.machines.tiles.hv.TeleporterTileEntity;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.SanityHelper;
import ic2.core.utils.helpers.TeleportUtil;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class TeleporterComponent implements ITileInfoComponent<TeleporterTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, TeleporterTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.teleporter.type", this.translate("ic2.probe.teleporter.type." + tile.getProbeSendType().name().toLowerCase()));
      if (tile.target == null) {
         basicInfo.text("ic2.probe.teleporter.no_target");
      } else if (!tile.hasMatchingType()) {
         basicInfo.text("ic2.probe.teleporter.invalid_target");
      } else {
         basicInfo.text(
            "ic2.probe.teleporter.target",
            SanityHelper.toPascalCase(tile.target.getDimension().location().getPath()),
            tile.target.getTargetPosition().getX(),
            tile.target.getTargetPosition().getY(),
            tile.target.getTargetPosition().getZ()
         );
      }

      int baseCost = tile.getBaseCost();
      if (baseCost > 0) {
         switch (tile.getSendType()) {
            case ENTITY:
               basicInfo.text(
                  "ic2.probe.teleporter.cost",
                  Formatters.EU_FORMAT.format((long)TeleportUtil.getWeightOfEntity(player, IC2.CONFIG.teleporterKeepItems.get()) * (long)baseCost * 5L)
               );
               break;
            case ENERGY:
               basicInfo.text("ic2.probe.teleporter.cost", Formatters.EU_FORMAT.format((long)baseCost));
               break;
            case FLUID:
               basicInfo.text("ic2.probe.teleporter.cost", Formatters.EU_FORMAT.format((long)baseCost));
               basicInfo.text(
                  "ic2.probe.teleporter.capacity",
                  this.translate(
                     "ic2.probe.teleporter.capacity.fluid",
                     new Object[]{ProbePluginHelper.formatNumber((double)(tile.getAvailableEnergy() / (long)baseCost * 10L), 6)}
                  )
               );
               break;
            case ITEM:
               basicInfo.text("ic2.probe.teleporter.cost", Formatters.EU_FORMAT.format((long)baseCost));
               basicInfo.text(
                  "ic2.probe.teleporter.capacity",
                  this.translate(
                     "ic2.probe.teleporter.capacity.item",
                     new Object[]{ProbePluginHelper.formatNumber((double)(tile.getAvailableEnergy() / (long)baseCost / 100L * 64L), 6)}
                  )
               );
               break;
            case SPAWNER:
               basicInfo.text("ic2.probe.teleporter.cost", Formatters.EU_FORMAT.format((long)baseCost * 25000L));
         }
      }

      basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(tile));
      this.addSecurely(info, 1, machine);
   }
}
