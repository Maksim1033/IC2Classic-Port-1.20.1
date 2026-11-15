package ic2.core.block.machines.logic.planner.newLogic;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.ISimulatedReactor;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.api.reactor.planner.Tracker;
import ic2.core.IC2;
import ic2.core.block.machines.logic.planner.IReactorProvider;
import ic2.core.block.machines.logic.planner.UserSettings;
import ic2.core.utils.collection.NBTListWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableDouble;

public class ReactorLogic implements ISimulatedReactor, INetworkDataBuffer {
   public IReactorProvider provider;
   public ReactorPrediction prediction = new ReactorPrediction();
   public SimulatedStack[] inventory = new SimulatedStack[54];
   public int totalTicks;
   public int heatCapacity;
   public int startingHeat;
   public int tick;
   public int ticksLeft;
   public int lastMaxHeat;
   public int maxHeat;
   public int heat;
   public float heatEffectModifier = 1.0F;
   public float lastProduction = 0.0F;
   public float production = 0.0F;
   public int lastSteamProduction = 0;
   public int steamProduction = 0;
   public boolean isProducing = false;
   public long totalProducedPower = 0L;
   public long totalWaterConsumed = 0L;
   public long totalSteamProduced = 0L;
   public boolean isExploded;
   public boolean didComponentBreak;
   public int brokenSlot = -1;
   public boolean isValid;
   public long slotsFilled = 0L;
   public boolean isSimulating = false;
   public boolean isBreeding = false;
   public int fuelPulse = 0;
   public int breedingPulse = 0;
   public Tracker averageHeat = new Tracker();

   public ReactorLogic(IReactorProvider provider) {
      this.provider = provider;
   }

   public void tick() {
      this.ticksLeft = this.ticksLeft - (!this.isProducing && !this.isSimulating ? 0 : 1);
      this.updateTick();
      if (this.heat >= this.maxHeat) {
         this.isExploded = true;
      }
   }

   public void simulate(SimulationResult result) {
      this.isProducing = true;
      int highestHeat = 0;
      int heatDuration = 0;

      while (!this.isFinished() && !this.isBroken()) {
         this.tick();
         int heat = this.getTotalHeat(null);
         if (heat > highestHeat) {
            highestHeat = heat;
            heatDuration = 0;
         } else {
            heatDuration++;
         }
      }

      result.isStable = heatDuration >= 200;
      result.ticksSimulated = this.tick;
      result.totalSimTicks = this.totalTicks;
      this.tick = 0;
      this.ticksLeft = 15000;
      this.isProducing = false;
      this.isSimulating = true;
      int prevHeat = 2147483647;
      int strikes = 0;
      int startHeat = this.heat;
      MutableDouble average = new MutableDouble();

      for (boolean shouldCool = !result.isStable; !this.isFinished() && shouldCool; this.tick()) {
         boolean isCoolEnough = this.heat <= this.startingHeat;
         int totalHeat = this.getTotalHeat(average);
         if (startHeat > 0 ? this.heat <= 0 || isCoolEnough : average.getValue() <= (this.isSteamReactor() ? 0.11 : 0.0)) {
            break;
         }

         if (totalHeat > prevHeat) {
            if (strikes++ > 5) {
               break;
            }
         } else {
            prevHeat = totalHeat;
            strikes = 0;
         }
      }

      this.isSimulating = false;
      result.ticksCooled = this.tick;
      result.isStable &= strikes <= 5;
      result.tickRate = this.isSteamReactor() ? 1 : 20;
      result.calculateEff();
   }

   protected int getTotalHeat(MutableDouble average) {
      int total = this.getHeat();
      int size = 0;

      for (int y = 0; y < 6; y++) {
         for (int x = 0; x < 9; x++) {
            SimulatedStack stack = this.inventory[y * 9 + x];
            if (stack != null) {
               stack.commitState();
               if (stack.canViewHeat(this, x, y)) {
                  int heat = stack.getStoredHeat(this, x, y);
                  total += heat;
                  if (average != null) {
                     average.add((double)heat / (double)stack.getMaxStoredHeat(this, x, y));
                  }

                  size++;
               }
            }
         }
      }

      if (average != null) {
         average.setValue(average.getValue() / (double)size);
      }

      this.commitState();
      return total;
   }

   protected void commitState() {
      this.averageHeat.commit();
   }

   protected void updateTick() {
      this.tick++;
      this.lastMaxHeat = this.maxHeat;
      this.lastProduction = this.production;
      this.lastSteamProduction = this.steamProduction;
      this.production = 0.0F;
      this.steamProduction = 0;
      this.maxHeat = 10000;
      this.heatEffectModifier = 1.0F;

      for (int pass = 0; pass < 2; pass++) {
         boolean heat = pass == 0;
         boolean damage = (!this.provider.isSteamReactor() || this.tick % 20 == 0) && pass == 1;

         for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
               SimulatedStack stack = this.inventory[y * 9 + x];
               if (stack != null) {
                  stack.commitState();
                  stack.simulate(this, x, y, heat, damage);
               }
            }
         }
      }

      this.totalProducedPower = this.totalProducedPower + (long)(this.production * (float)this.getTickRate());
   }

   protected void resetData() {
      this.ticksLeft = 0;
      this.tick = 0;
      this.maxHeat = 10000;
      this.heat = 0;
      this.heatEffectModifier = 1.0F;
      this.lastProduction = 0.0F;
      this.production = 0.0F;
      this.lastSteamProduction = 0;
      this.steamProduction = 0;
      this.totalProducedPower = 0L;
      this.totalWaterConsumed = 0L;
      this.totalSteamProduced = 0L;
      this.breedingPulse = 0;
      this.fuelPulse = 0;
      this.isBreeding = false;
   }

   public void resetSimulation() {
      this.isProducing = true;
      this.isExploded = false;
      this.didComponentBreak = false;
      this.brokenSlot = -1;
      this.isValid = false;
      this.totalProducedPower = 0L;
      this.totalSteamProduced = 0L;
      this.totalWaterConsumed = 0L;
      this.resetData();
      this.resetComponents();
      this.totalTicks = 0;
      this.heatCapacity = 0;
      this.startingHeat = 0;
   }

   public boolean isFinished() {
      return this.ticksLeft == 0;
   }

   public boolean isBroken() {
      return this.didComponentBreak || this.isExploded;
   }

   public void validateLogic() {
      this.resetSimulation();
      this.isValid = true;
      this.isProducing = true;
      this.prediction.clear();
      int maxTick = 0;
      int maxCapacity = 0;

      for (int i = 0; i < 54; i++) {
         SimulatedStack stack = this.inventory[i];
         if (stack != null) {
            IReactorPlannerComponent.ComponentType type = stack.getComponentType();
            float value = stack.getExplosionInfluence(this);
            if (value >= 0.0F && value < 1.0F) {
               this.prediction.explosionMod *= value;
            } else {
               this.prediction.explosionPower += value;
            }

            int x = i % 9;
            int y = i / 9;
            if (type == IReactorPlannerComponent.ComponentType.FUEL_ROD) {
               maxTick = Math.max(
                  maxTick, stack.getStat(IReactorPlannerComponent.ReactorStat.MAX_COMPONENT_DURABILITY, this, x, y).getAsInt() * (21 - this.getTickRate())
               );
               this.prediction.heatPerTick = this.prediction.heatPerTick
                  + stack.getStat(IReactorPlannerComponent.ReactorStat.HEAT_PRODUCTION, this, x, y).getAsInt();
               this.isBreeding = true;
               this.prediction.energyPerTick = this.prediction.energyPerTick
                  + stack.getStat(IReactorPlannerComponent.ReactorStat.ENERGY_PRODUCTION, this, x, y).getAsFloat();
               this.isBreeding = false;
               this.prediction.totalFuelRodPulses = this.prediction.totalFuelRodPulses + this.fuelPulse;
               this.prediction.totalBreedingPulses = this.prediction.totalBreedingPulses + this.breedingPulse;
               this.resetData();
               this.prediction.totalCellCount = this.prediction.totalCellCount
                  + stack.getStat(IReactorPlannerComponent.ReactorStat.ROD_COUNT, this, x, y).getAsInt();
               this.prediction.totalInternalFuelPulses = this.prediction.totalInternalFuelPulses
                  + stack.getStat(IReactorPlannerComponent.ReactorStat.PULSE_COUNT, this, x, y).getAsInt();
               this.resetData();
            } else if (type == IReactorPlannerComponent.ComponentType.HEAT_PACK) {
               int heat = stack.getStat(IReactorPlannerComponent.ReactorStat.HEAT_PRODUCTION, this, x, y).getAsInt();
               this.prediction.heatPerTick += heat;
               this.prediction.heatPackHeatPerTick += heat;
            } else if (type == IReactorPlannerComponent.ComponentType.HEAT_VENT) {
               this.prediction.coolingPerTick = this.prediction.coolingPerTick
                  + stack.getStat(IReactorPlannerComponent.ReactorStat.SELF_COOLING, this, x, y).getAsInt();
               this.prediction.reactorCoolingPerTick = this.prediction.reactorCoolingPerTick
                  + stack.getStat(IReactorPlannerComponent.ReactorStat.REACTOR_COOLING, this, x, y).getAsInt();
            } else if (type == IReactorPlannerComponent.ComponentType.HEAT_SPREAD) {
               this.prediction.coolingPerTick = this.prediction.coolingPerTick
                  + stack.getStat(IReactorPlannerComponent.ReactorStat.PART_COOLING, this, x, y).getAsInt();
               this.prediction.reactorCoolingPerTick = this.prediction.reactorCoolingPerTick
                  + stack.getStat(IReactorPlannerComponent.ReactorStat.REACTOR_COOLING, this, x, y).getAsInt();
            } else if (type == IReactorPlannerComponent.ComponentType.PLATING) {
               maxCapacity += stack.getStat(IReactorPlannerComponent.ReactorStat.MAX_HEAT_STORAGE, this, x, y).getAsInt();
            }
         }
      }

      this.heatCapacity = this.maxHeat = 10000 + maxCapacity;
      this.resetComponents();
      UserSettings settings = this.provider.getSettings();
      if (settings.maxTicks > 0) {
         maxTick = settings.maxTicks;
      }

      this.totalTicks = this.ticksLeft = maxTick;
      this.heat = this.startingHeat = settings.startingHeat;
      this.updatePrediction(maxTick);
   }

   public void resetComponents() {
      if (this.provider.getComponents() == null) {
         for (int i = 0; i < 54; i++) {
            SimulatedStack stack = this.inventory[i];
            if (stack != null) {
               stack.reset();
            }
         }
      } else {
         for (int ix = 0; ix < 54; ix++) {
            SimulatedStack stack = this.inventory[ix];
            if (stack != null) {
               stack.reset();
               stack.syncStack(this.provider.getComponents().getStackInSlot(ix));
            }
         }
      }
   }

   public void syncComponents() {
      for (int i = 0; i < 54; i++) {
         SimulatedStack stack = this.inventory[i];
         if (stack != null) {
            stack.syncStack(this.provider.getComponents().getStackInSlot(i));
         }
      }
   }

   public void setStackInSlot(int slot, ItemStack stack) {
      this.inventory[slot] = stack.getItem() instanceof IReactorPlannerComponent
         ? ((IReactorPlannerComponent)stack.getItem()).createSimulationComponent(stack)
         : null;
      if (this.inventory[slot] == null) {
         this.slotsFilled &= ~(1L << slot);
      } else {
         this.slotsFilled |= 1L << slot;
      }
   }

   public void updatePrediction(int maxTick) {
      this.prediction.totalHeatProduced = (long)this.prediction.heatPerTick * (long)maxTick;
      this.prediction.totalExplosionPower = this.prediction.explosionPower * this.heatEffectModifier * this.prediction.explosionMod;
      this.prediction.efficiency = this.prediction.energyPerTick != 0.0F
         ? (float)this.prediction.totalFuelRodPulses / (float)this.prediction.totalCellCount
         : 0.0F;
      this.prediction.totalEfficiency = this.prediction.energyPerTick != 0.0F
         ? (float)(this.prediction.totalFuelRodPulses + this.prediction.totalBreedingPulses) / (float)this.prediction.totalCellCount
         : 0.0F;
      this.prediction.breeder = this.prediction.totalBreedingPulses > 0;
      this.prediction.totalEnergyProduced = (long)(this.prediction.energyPerTick * (float)maxTick) * (long)this.getTickRate();
      this.prediction.waterPerTick = Math.min((float)this.prediction.heatPerTick / 40.0F, (float)this.prediction.coolingPerTick)
         * (float)IC2.CONFIG.steamReactorMultiplier.get();
      this.prediction.steamPerTick = this.prediction.waterPerTick * 160.0F;
      this.prediction.totalSteamProduced = (long)(this.prediction.steamPerTick * (float)maxTick);
      this.prediction.totalWaterConsumed = this.prediction.totalSteamProduced / 160L;
   }

   protected int getTickRate() {
      return this.provider.isSteamReactor() ? 1 : 20;
   }

   protected int getReactorSize() {
      return this.provider.getReactorSize() + 3;
   }

   @Override
   public SimulatedStack getItem(int x, int y) {
      return x >= 0 && y >= 0 && y < 6 && x < this.getReactorSize() ? this.inventory[y * 9 + x] : null;
   }

   @Override
   public void markBroken(int x, int y) {
      this.didComponentBreak = true;
      this.brokenSlot = y * 9 + x;
   }

   @Override
   public int getHeat() {
      return this.heat;
   }

   @Override
   public void setHeat(int newHeat) {
      this.heat = newHeat;
   }

   @Override
   public void addHeat(int heat) {
      this.heat += heat;
      this.averageHeat.addChange(heat);
   }

   @Override
   public int getMaxHeat() {
      return this.maxHeat;
   }

   @Override
   public void setMaxHeat(int heat) {
      this.maxHeat = heat;
   }

   @Override
   public float getHeatEffectModifier() {
      return this.heatEffectModifier;
   }

   @Override
   public void setHeatEffectModifier(float hem) {
      this.heatEffectModifier = hem;
   }

   @Override
   public void addOutput(float output) {
      this.production += output;
   }

   @Override
   public void addSteam(int amount) {
      this.steamProduction += amount;
      this.totalSteamProduced += (long)amount;
   }

   @Override
   public int consumeWater(int amount) {
      this.totalWaterConsumed += (long)amount;
      return amount;
   }

   @Override
   public boolean isProducingEnergy() {
      return this.isProducing;
   }

   @Override
   public boolean isSteamReactor() {
      return this.provider.isSteamReactor();
   }

   @Override
   public void addBreedingPulse() {
      this.breedingPulse++;
   }

   @Override
   public void addFuelPulse() {
      this.fuelPulse++;
   }

   @Override
   public float getEnergyOutput() {
      return this.production * (float)IC2.CONFIG.reactorOutput.get();
   }

   @Override
   public boolean isSimulatingPulses() {
      return this.isBreeding;
   }

   @Override
   public long getGameTime() {
      return this.provider.getGameTime((long)this.tick);
   }

   public CompoundTag save(CompoundTag data) {
      data.putInt("total_ticks", this.totalTicks);
      data.putInt("heat_capacity", this.heatCapacity);
      data.putInt("start_heat", this.startingHeat);
      data.putInt("tick", this.tick);
      data.putInt("ticks_left", this.ticksLeft);
      data.putInt("max_heat", this.maxHeat);
      data.putInt("heat", this.heat);
      data.putFloat("hef", this.heatEffectModifier);
      data.putFloat("production", this.production);
      data.putInt("steam_production", this.steamProduction);
      data.putBoolean("is_producing", this.isProducing);
      data.putLong("total_produced_power", this.totalProducedPower);
      data.putLong("total_produced_steam", this.totalProducedPower);
      data.putLong("total_consumed_water", this.totalWaterConsumed);
      data.putBoolean("is_exploded", this.isExploded);
      data.putBoolean("did_break", this.didComponentBreak);
      data.putInt("broken_slot", this.brokenSlot);
      data.putBoolean("valid", this.isValid);
      data.putBoolean("simulating", this.isSimulating);
      data.put("prediction", this.prediction.save(new CompoundTag()));
      ListTag list = new ListTag();

      for (int i = 0; i < 54; i++) {
         SimulatedStack stack = this.inventory[i];
         if (stack != null) {
            CompoundTag nbt = stack.save();
            if (!nbt.isEmpty()) {
               nbt.putInt("slot", i);
               list.add(nbt);
            }
         }
      }

      if (!list.isEmpty()) {
         data.put("items", list);
      }

      return data;
   }

   public void load(CompoundTag data) {
      this.totalTicks = data.getInt("total_ticks");
      this.heatCapacity = data.getInt("heat_capacity");
      this.startingHeat = data.getInt("start_heat");
      this.tick = data.getInt("tick");
      this.ticksLeft = data.getInt("ticks_left");
      this.lastMaxHeat = this.maxHeat = data.getInt("max_heat");
      this.heat = data.getInt("heat");
      this.heatEffectModifier = data.getFloat("hef");
      this.lastProduction = this.production = data.getFloat("production");
      this.lastSteamProduction = this.steamProduction = data.getInt("steam_production");
      this.isProducing = data.getBoolean("is_producing");
      this.totalProducedPower = data.getLong("total_produced_power");
      this.totalProducedPower = data.getLong("total_produced_steam");
      this.totalWaterConsumed = data.getLong("total_consumed_water");
      this.isExploded = data.getBoolean("is_exploded");
      this.didComponentBreak = data.getBoolean("did_break");
      this.brokenSlot = data.getInt("broken_slot");
      this.isValid = data.getBoolean("valid");
      this.isSimulating = data.getBoolean("simulating");
      this.prediction.load(data.getCompound("prediction"));

      for (CompoundTag nbt : NBTListWrapper.wrap(data.getList("items", 10), CompoundTag.class)) {
         SimulatedStack stack = this.inventory[nbt.getInt("slot")];
         if (stack != null) {
            stack.load(nbt);
         }
      }
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeInt(this.totalTicks);
      buffer.writeInt(this.heatCapacity);
      buffer.writeInt(this.startingHeat);
      buffer.writeInt(this.tick);
      buffer.writeInt(this.ticksLeft);
      buffer.writeInt(this.lastMaxHeat);
      buffer.writeInt(this.heat);
      buffer.writeFloat(this.heatEffectModifier);
      buffer.writeFloat(this.lastProduction);
      buffer.writeInt(this.lastSteamProduction);
      buffer.writeBoolean(this.isProducing);
      buffer.writeLong(this.totalProducedPower);
      buffer.writeLong(this.totalWaterConsumed);
      buffer.writeLong(this.totalSteamProduced);
      buffer.writeBoolean(this.isExploded);
      buffer.writeBoolean(this.didComponentBreak);
      buffer.writeByte((byte)this.brokenSlot);
      buffer.writeBoolean(this.isValid);
      buffer.writeLong(this.slotsFilled);
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.totalTicks = buffer.readInt();
      this.heatCapacity = buffer.readInt();
      this.startingHeat = buffer.readInt();
      this.tick = buffer.readInt();
      this.ticksLeft = buffer.readInt();
      this.maxHeat = buffer.readInt();
      this.heat = buffer.readInt();
      this.heatEffectModifier = buffer.readFloat();
      this.production = buffer.readFloat();
      this.steamProduction = buffer.readInt();
      this.isProducing = buffer.readBoolean();
      this.totalProducedPower = buffer.readLong();
      this.totalWaterConsumed = buffer.readLong();
      this.totalSteamProduced = buffer.readLong();
      this.isExploded = buffer.readBoolean();
      this.didComponentBreak = buffer.readBoolean();
      this.brokenSlot = buffer.readByte();
      this.isValid = buffer.readBoolean();
      this.slotsFilled = buffer.readLong();
   }
}
