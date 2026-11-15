package ic2.core.item.reactor.planner;

import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.BaseHeatSimulatedStack;
import ic2.api.reactor.planner.ISimulatedReactor;
import ic2.core.IC2;
import ic2.core.item.reactor.ReactorHeatVentItem;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public class SimulatedHeatVent extends BaseHeatSimulatedStack {
   protected ReactorHeatVentItem.VentProperty props;
   protected double water = 0.0;
   protected double heatStorage = 0.0;

   public SimulatedHeatVent(ReactorHeatVentItem.VentProperty props) {
      super(props.getComponentID(), props.getMaxHeat());
      this.props = props;
   }

   @Override
   public ItemStack syncStack(ItemStack original) {
      original.setDamageValue(this.heat);
      return original;
   }

   @Override
   public void reset() {
      super.reset();
      this.heatStorage = 0.0;
      this.water = 0.0;
   }

   @Override
   public CompoundTag save() {
      CompoundTag data = super.save();
      data.putDouble("water", this.water);
      data.putDouble("heatStorage", this.heatStorage);
      return data;
   }

   @Override
   public void load(CompoundTag data) {
      super.load(data);
      this.water = data.getDouble("water");
      this.heatStorage = data.getDouble("heatStorage");
   }

   @Override
   public void simulate(ISimulatedReactor reactor, int x, int y, boolean heatTick, boolean damageTick) {
      if (reactor.isSteamReactor()) {
         if (this.props.getType() == ReactorHeatVentItem.VentType.ELECTRIC) {
            return;
         }

         if (this.props.getType() == ReactorHeatVentItem.VentType.HEAT) {
            this.processNormalReactor(reactor, x, y, heatTick);
            return;
         }

         if (heatTick) {
            int reactorVent = this.props.getReactor();
            int selfVent = this.props.getSelf();
            if (reactorVent > 0) {
               int reactorDrain;
               int rHeat = reactorDrain = reactor.getHeat();
               if (reactorDrain > reactorVent) {
                  reactorDrain = reactorVent;
               }

               rHeat -= reactorDrain;
               if (this.storeHeat(reactor, x, y, reactorDrain) > 0) {
                  return;
               }

               reactor.setHeat(rHeat);
            }

            if (this.heat < 100) {
               if (reactor.getGameTime() % 40L == 0L) {
                  this.storeHeat(reactor, x, y, -1);
               }

               return;
            }

            if (this.water <= 0.0) {
               if (reactor.getGameTime() % 40L == 0L) {
                  this.storeHeat(reactor, x, y, -1);
               }

               return;
            }

            double heatLevel = (double)this.heat / ((double)this.props.getMaxHeat() - 100.0);
            double lvl = (double)selfVent * heatLevel / 40.0 * IC2.CONFIG.steamReactorMultiplier.get();
            double consume = Math.min(this.water, lvl);
            if (consume <= 0.0) {
               return;
            }

            int steam = (int)(160.0 * consume);
            if (steam <= 0) {
               return;
            }

            reactor.addSteam(steam);
            consume = (double)steam / 160.0;
            this.water -= consume;
            this.heatStorage = this.heatStorage + consume * 40.0 / IC2.CONFIG.steamReactorMultiplier.get();
            int copy = -((int)this.heatStorage);
            copy -= this.storeHeat(reactor, x, y, copy);
            this.heatStorage += (double)copy;
         } else if (this.water <= (double)(this.props.getSelf() - 1)) {
            this.water = this.water + (double)reactor.consumeWater(this.props.getSelf() - (int)this.water);
         }
      } else {
         this.processNormalReactor(reactor, x, y, heatTick);
      }
   }

   private void processNormalReactor(ISimulatedReactor reactor, int x, int y, boolean heatCalculation) {
      if (heatCalculation) {
         boolean producing = reactor.isProducingEnergy();
         boolean electric = this.props.getType() == ReactorHeatVentItem.VentType.ELECTRIC;
         int reactorVent = electric && !producing ? this.props.getReactor() / 2 : this.props.getReactor();
         int selfVent = this.props.getSelf();
         if (reactorVent > 0) {
            int reactorDrain;
            int rHeat = reactorDrain = reactor.getHeat();
            if (reactorDrain > reactorVent) {
               reactorDrain = reactorVent;
            }

            rHeat -= reactorDrain;
            if (this.storeHeat(reactor, x, y, reactorDrain) > 0) {
               return;
            }

            reactor.setHeat(rHeat);
         }

         if (electric) {
            if (producing) {
               reactor.addOutput(-((float)selfVent * 0.005F));
            }

            this.storeHeat(reactor, x, y, producing ? -this.props.getSelf() : -(this.props.getSelf() / 2));
            return;
         }

         this.storeHeat(reactor, x, y, -selfVent);
      }
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats() {
      switch (this.props.getType()) {
         case ELECTRIC:
            return CollectionUtils.<IReactorPlannerComponent.ReactorStat>asList(
               IReactorPlannerComponent.ReactorStat.SELF_COOLING,
               IReactorPlannerComponent.ReactorStat.REACTOR_COOLING,
               IReactorPlannerComponent.ReactorStat.ENERGY_USAGE
            );
         case STEAM:
            return CollectionUtils.<IReactorPlannerComponent.ReactorStat>asList(
               IReactorPlannerComponent.ReactorStat.SELF_COOLING,
               IReactorPlannerComponent.ReactorStat.REACTOR_COOLING,
               IReactorPlannerComponent.ReactorStat.WATER_CONSUMPTION,
               IReactorPlannerComponent.ReactorStat.STEAM_PRODUCTION,
               IReactorPlannerComponent.ReactorStat.WATER_STORAGE
            );
         default:
            return CollectionUtils.<IReactorPlannerComponent.ReactorStat>asList(
               IReactorPlannerComponent.ReactorStat.SELF_COOLING, IReactorPlannerComponent.ReactorStat.REACTOR_COOLING
            );
      }
   }

   @Override
   public IReactorPlannerComponent.ReactorType getValidType() {
      if (this.props.getType() == ReactorHeatVentItem.VentType.STEAM) {
         return IReactorPlannerComponent.ReactorType.STEAM;
      } else {
         return this.props.getType() == ReactorHeatVentItem.VentType.ELECTRIC
            ? IReactorPlannerComponent.ReactorType.ELECTRIC
            : IReactorPlannerComponent.ReactorType.UNIVERSAL;
      }
   }

   @Override
   public IReactorPlannerComponent.ComponentType getComponentType() {
      return IReactorPlannerComponent.ComponentType.HEAT_VENT;
   }

   @Override
   public NumericTag getStat(IReactorPlannerComponent.ReactorStat stat) {
      switch (stat) {
         case SELF_COOLING:
            return IntTag.valueOf(this.props.getSelf());
         case REACTOR_COOLING:
            return IntTag.valueOf(this.props.getReactor());
         case ENERGY_USAGE:
            return FloatTag.valueOf((float)this.props.getSelf() * 0.005F);
         case STEAM_PRODUCTION:
            return FloatTag.valueOf((float)(this.props.getSelf() * 4) * (float)IC2.CONFIG.steamReactorMultiplier.get());
         case WATER_CONSUMPTION:
            return FloatTag.valueOf((float)this.props.getSelf() / 40.0F * (float)IC2.CONFIG.steamReactorMultiplier.get());
         case WATER_STORAGE:
            return FloatTag.valueOf((float)this.props.getSelf());
         default:
            return NULL_VALUE;
      }
   }
}
