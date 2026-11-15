package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.core.block.base.tiles.impls.machine.single.BaseAdvMachineTileEntity;
import ic2.core.block.base.tiles.impls.machine.single.BaseMachineTileEntity;
import ic2.core.block.machines.tiles.lv.RareEarthExtractorTileEntity;
import ic2.core.block.machines.tiles.mv.RareEarthCentrifugeTileEntity;
import ic2.core.block.machines.tiles.mv.RefineryTileEntity;
import ic2.core.block.machines.tiles.mv.SlowGrinderTileEntity;
import ic2.core.block.machines.tiles.mv.VacuumCannerTileEntity;
import ic2.core.utils.helpers.Formatters;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BaseMachineComponent implements ITileInfoComponent<BaseMachineTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseMachineTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getTier()));
      basicInfo.text("ic2.probe.eu.max_in.name", tile.getMaxInput());
      basicInfo.text("ic2.probe.eu.usage.name", tile.getEnergyPerTick());
      if (tile instanceof SlowGrinderTileEntity) {
         basicInfo.text("ic2.probe.scrap.chance.name", Formatters.XP_FORMAT.format((double)(((SlowGrinderTileEntity)tile).getChance(0.25F) * 100.0F)));
      }

      IExpandedProbeInfo bars = basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(tile));
      if (tile instanceof BaseAdvMachineTileEntity adv && adv.getSpeed() > 0) {
         bars.progress(
            adv.getSpeed(),
            adv.getMaxSpeed(),
            IC2Styles.SPEED_BAR
               .copy()
               .prefix(
                  adv.getSpeedName()
                     .copy()
                     .append(": " + ProbePluginHelper.THERMAL_GEN.format((double)adv.getSpeed() / (double)adv.getMaxSpeed() * 100.0) + "%")
               )
         );
      }

      if (tile instanceof VacuumCannerTileEntity vac && vac.getSpeed() > 0) {
         bars.progress(
            vac.getSpeed(),
            vac.getMaxSpeed(),
            IC2Styles.SPEED_BAR
               .copy()
               .prefix(
                  vac.getSpeedName()
                     .copy()
                     .append(": " + ProbePluginHelper.THERMAL_GEN.format((double)vac.getSpeed() / (double)vac.getMaxSpeed() * 100.0) + "%")
               )
         );
      }

      if (tile instanceof RareEarthExtractorTileEntity extractor) {
         ItemStack output = extractor.getCurrentOutput();
         if (!output.isEmpty() && extractor.materialProgress > 0.0F) {
            bars.progress(
               (int)extractor.materialProgress,
               1000,
               IC2Styles.MATERIAL_BAR
                  .copy()
                  .prefix("ic2.probe.progress.material.name", new Object[]{Formatters.EU_READER_FORMAT.format((double)extractor.materialProgress)})
            );
         }
      } else if (tile instanceof RareEarthCentrifugeTileEntity extractorx) {
         ItemStack output = extractorx.getCurrentOutput();
         if (!output.isEmpty() && extractorx.materialProgress > 0.0F) {
            bars.progress(
               (int)extractorx.materialProgress,
               1000,
               IC2Styles.MATERIAL_BAR
                  .copy()
                  .prefix("ic2.probe.progress.material.name", new Object[]{Formatters.EU_READER_FORMAT.format((double)extractorx.materialProgress)})
            );
         }

         if (extractorx.getSpeed() > 0) {
            bars.progress(
               extractorx.getSpeed(),
               extractorx.getMaxSpeed(),
               IC2Styles.SPEED_BAR
                  .copy()
                  .prefix(
                     extractorx.getSpeedName()
                        .copy()
                        .append(": " + ProbePluginHelper.THERMAL_GEN.format((double)extractorx.getSpeed() / (double)extractorx.getMaxSpeed() * 100.0) + "%")
                  )
            );
         }
      }

      if (tile instanceof RefineryTileEntity) {
         ProbePluginHelper.addTanks(tile, bars, true);
      }

      if (tile.isMachineWorking() || tile.getProgress() > 0.0F) {
         bars.progress(
            (int)tile.getProgress(),
            (int)tile.getMaxProgress(),
            IC2Styles.progressBar(
               (int)Math.min(6.0E7F, tile.getProgress() / tile.progressPerTick), (int)Math.min(6.0E7F, tile.getMaxProgress() / tile.progressPerTick)
            )
         );
      }

      ProbePluginHelper.generateDefaultSlots(tile, false, machine);
      this.addSecurely(info, 1, machine);
   }
}
