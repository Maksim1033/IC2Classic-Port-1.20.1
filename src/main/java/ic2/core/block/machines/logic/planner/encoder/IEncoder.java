package ic2.core.block.machines.logic.planner.encoder;

import ic2.core.block.machines.logic.planner.newLogic.ReactorPrediction;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import net.minecraft.nbt.CompoundTag;

public interface IEncoder {
   CompoundTag createDecodedData(String var1);

   void processData(CompoundTag var1, ReactorPlannerTileEntity var2);

   String createEncodedData(ReactorPlannerTileEntity var1);

   ReactorSetup createSetup(String var1, boolean var2);

   String getName();

   boolean hasBitLimit();

   int getBitLimit();

   default ReactorSetup finishSetup(ReactorPrediction prediction, boolean isSteam, int chambers, int startingHeat) {
      ReactorSetup setup = new ReactorSetup();
      setup.breeder = prediction.breeder;
      setup.isStable = prediction.heatPerTick <= prediction.coolingPerTick;
      setup.chambersRequired = chambers;
      setup.efficiency = prediction.efficiency;
      setup.totalEfficiency = prediction.totalEfficiency;
      setup.isSteam = isSteam;
      setup.startingHeat = startingHeat;
      if (isSteam) {
         setup.duration = (int)Math.max(1.0F, (float)prediction.totalSteamProduced / Math.max(1.0F, prediction.steamPerTick));
         setup.output = prediction.steamPerTick;
         setup.totalOutput = prediction.totalSteamProduced;
         setup.input = (float)prediction.totalWaterConsumed / (float)setup.duration;
         setup.totalInput = prediction.totalWaterConsumed;
      } else {
         setup.efficiency = prediction.efficiency;
         setup.totalEfficiency = prediction.totalEfficiency;
         setup.duration = (int)Math.max(1.0F, (float)prediction.totalEnergyProduced / Math.max(1.0F, prediction.energyPerTick));
         setup.output = prediction.energyPerTick;
         setup.totalOutput = prediction.totalEnergyProduced;
      }

      return setup;
   }
}
