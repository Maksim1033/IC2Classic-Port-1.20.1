package ic2.core.block.machines.logic.planner.newLogic;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.inventory.base.INBTSavable;
import net.minecraft.nbt.CompoundTag;

public class ReactorPrediction implements INetworkDataBuffer, INBTSavable {
   public long totalHeatProduced;
   public int heatPerTick;
   public int heatPackHeatPerTick;
   public int coolingPerTick;
   public int reactorCoolingPerTick;
   public float efficiency;
   public float totalEfficiency;
   public boolean breeder;
   public int totalFuelRodPulses;
   public int totalInternalFuelPulses;
   public int totalCellCount;
   public int totalBreedingPulses;
   public float totalExplosionPower;
   float explosionPower = 10.0F;
   float explosionMod = 1.0F;
   public long totalEnergyProduced;
   public float energyPerTick;
   public long totalWaterConsumed;
   public long totalSteamProduced;
   public float steamPerTick;
   public float waterPerTick;

   public void clear() {
      this.totalHeatProduced = 0L;
      this.heatPerTick = 0;
      this.heatPackHeatPerTick = 0;
      this.coolingPerTick = 0;
      this.reactorCoolingPerTick = 0;
      this.efficiency = 0.0F;
      this.totalEfficiency = 0.0F;
      this.breeder = false;
      this.totalFuelRodPulses = 0;
      this.totalInternalFuelPulses = 0;
      this.totalCellCount = 0;
      this.totalBreedingPulses = 0;
      this.totalExplosionPower = 0.0F;
      this.explosionPower = 10.0F;
      this.explosionMod = 1.0F;
      this.totalEnergyProduced = 0L;
      this.energyPerTick = 0.0F;
      this.totalWaterConsumed = 0L;
      this.totalSteamProduced = 0L;
      this.steamPerTick = 0.0F;
      this.waterPerTick = 0.0F;
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      nbt.putLong("total_heat", this.totalHeatProduced);
      nbt.putInt("heat_production", this.heatPerTick);
      nbt.putInt("heat_pack", this.heatPackHeatPerTick);
      nbt.putInt("cooling", this.coolingPerTick);
      nbt.putInt("reactor_cooling", this.reactorCoolingPerTick);
      nbt.putFloat("explosion_power", this.totalExplosionPower);
      nbt.putFloat("efficiency", this.efficiency);
      nbt.putFloat("total_efficiency", this.totalEfficiency);
      nbt.putBoolean("breeder", this.breeder);
      nbt.putLong("total_energy", this.totalEnergyProduced);
      nbt.putFloat("energy_production", this.energyPerTick);
      nbt.putLong("total_water", this.totalWaterConsumed);
      nbt.putLong("total_steam", this.totalSteamProduced);
      nbt.putFloat("steam", this.steamPerTick);
      nbt.putFloat("water", this.waterPerTick);
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.totalHeatProduced = nbt.getLong("total_heat");
      this.heatPerTick = nbt.getInt("heat_production");
      this.heatPackHeatPerTick = nbt.getInt("heat_pack");
      this.coolingPerTick = nbt.getInt("cooling");
      this.reactorCoolingPerTick = nbt.getInt("reactor_cooling");
      this.totalExplosionPower = nbt.getFloat("explosion_power");
      this.efficiency = nbt.getFloat("efficiency");
      this.totalEfficiency = nbt.getFloat("total_efficiency");
      this.breeder = nbt.getBoolean("breeder");
      this.energyPerTick = nbt.getFloat("energy_production");
      this.totalEnergyProduced = nbt.getLong("total_energy");
      this.totalWaterConsumed = nbt.getLong("total_water");
      this.totalSteamProduced = nbt.getLong("total_steam");
      this.steamPerTick = nbt.getFloat("steam");
      this.waterPerTick = nbt.getFloat("water");
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeLong(this.totalHeatProduced);
      buffer.writeInt(this.heatPerTick);
      buffer.writeInt(this.heatPackHeatPerTick);
      buffer.writeInt(this.coolingPerTick);
      buffer.writeInt(this.reactorCoolingPerTick);
      buffer.writeFloat(this.totalExplosionPower);
      buffer.writeFloat(this.efficiency);
      buffer.writeFloat(this.totalEfficiency);
      buffer.writeBoolean(this.breeder);
      buffer.writeLong(this.totalEnergyProduced);
      buffer.writeFloat(this.energyPerTick);
      buffer.writeLong(this.totalWaterConsumed);
      buffer.writeLong(this.totalSteamProduced);
      buffer.writeFloat(this.steamPerTick);
      buffer.writeFloat(this.waterPerTick);
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.totalHeatProduced = buffer.readLong();
      this.heatPerTick = buffer.readInt();
      this.heatPackHeatPerTick = buffer.readInt();
      this.coolingPerTick = buffer.readInt();
      this.reactorCoolingPerTick = buffer.readInt();
      this.totalExplosionPower = buffer.readFloat();
      this.efficiency = buffer.readFloat();
      this.totalEfficiency = buffer.readFloat();
      this.breeder = buffer.readBoolean();
      this.totalEnergyProduced = buffer.readLong();
      this.energyPerTick = buffer.readFloat();
      this.totalWaterConsumed = buffer.readLong();
      this.totalSteamProduced = buffer.readLong();
      this.steamPerTick = buffer.readFloat();
      this.waterPerTick = buffer.readFloat();
   }
}
