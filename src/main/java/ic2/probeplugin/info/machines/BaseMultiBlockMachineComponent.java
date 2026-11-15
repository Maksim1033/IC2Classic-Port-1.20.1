package ic2.probeplugin.info.machines;

import ic2.api.energy.EnergyNet;
import ic2.core.block.base.tiles.impls.machine.multi.BaseAdvMultiMachineTileEntity;
import ic2.core.block.base.tiles.impls.machine.multi.BaseColossalMachineTileEntity;
import ic2.core.block.base.tiles.impls.machine.multi.BaseMultiMachineTileEntity;
import ic2.core.block.base.tiles.impls.machine.multi.BasicMultiMachineTileEntity;
import ic2.probeplugin.base.ProbePluginHelper;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class BaseMultiBlockMachineComponent implements ITileInfoComponent<BaseMultiMachineTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseMultiMachineTileEntity tile) {
      Panel machine = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = machine.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("ic2.probe.eu.tier.name", EnergyNet.INSTANCE.getDisplayTier(tile.getTier()));
      basicInfo.text("ic2.probe.eu.max_in.name", tile.getMaxInput());
      basicInfo.text("ic2.probe.eu.usage.name", tile.getEnergyPerTick());
      IExpandedProbeInfo bars = basicInfo.vertical().element(ProbePluginHelper.generateHiddenBar(tile));
      if (tile instanceof BaseAdvMultiMachineTileEntity adv && adv.getSpeed() > 0) {
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

      if (tile instanceof BasicMultiMachineTileEntity machineTile && (tile.isMachineWorking() || machineTile.getProgress() > 0.0F)) {
         bars.progress(
            (int)machineTile.getProgress(),
            (int)machineTile.getMaxProgress(),
            IC2Styles.progressBar(
               (int)Math.min(6.0E7F, machineTile.getProgress() / tile.progressPerTick),
               (int)Math.min(6.0E7F, machineTile.getMaxProgress() / tile.progressPerTick)
            )
         );
      }

      if (tile instanceof BaseColossalMachineTileEntity colossal) {
         IntList maxProgressFilter = new IntArrayList();
         IntList progressFilter = new IntArrayList();
         IntIterator iter = colossal.getActiveSlots();

         while (iter.hasNext()) {
            int slot = iter.nextInt();
            int progress = (int)colossal.getProgress(slot);
            int maxProgress = (int)colossal.getMaxProgress(slot);
            int maxIndex = maxProgressFilter.indexOf(maxProgress);
            int index = progressFilter.indexOf(progress);
            if (maxIndex != index || index == -1) {
               maxProgressFilter.add(maxProgress);
               progressFilter.add(progress);
            }
         }

         for (int i = 0; i < progressFilter.size(); i++) {
            bars.progress(
               progressFilter.getInt(i),
               maxProgressFilter.getInt(i),
               IC2Styles.progressBar(
                  (int)Math.min(6.0E7F, (float)progressFilter.getInt(i) / tile.progressPerTick),
                  (int)Math.min(6.0E7F, (float)maxProgressFilter.getInt(i) / tile.progressPerTick)
               )
            );
         }
      }

      ProbePluginHelper.addTanks(tile, bars, true);
      ProbePluginHelper.generateDefaultSlots(tile, false, machine);
      this.addSecurely(info, 1, machine);
   }
}
