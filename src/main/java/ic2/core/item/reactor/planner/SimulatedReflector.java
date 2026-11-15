package ic2.core.item.reactor.planner;

import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.BaseDurabilitySimulatedStack;
import ic2.api.reactor.planner.ISimulatedReactor;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public class SimulatedReflector extends BaseDurabilitySimulatedStack {
   protected boolean unbreakable;
   protected int refilled = 0;

   public SimulatedReflector(short id, int maxDamage, boolean unbreakable) {
      super(id, maxDamage);
      this.unbreakable = unbreakable;
   }

   @Override
   public ItemStack syncStack(ItemStack original) {
      original.setDamageValue(this.damage);
      if (this.refilled > 0) {
         original.getOrCreateTag().putInt("refilled", this.refilled);
      } else {
         original.getOrCreateTag().remove("refilled");
      }

      return original;
   }

   @Override
   public void reset() {
      super.reset();
      this.refilled = 0;
   }

   @Override
   public CompoundTag save() {
      CompoundTag compound = super.save();
      compound.putInt("refilled", this.refilled);
      return compound;
   }

   @Override
   public void load(CompoundTag data) {
      super.load(data);
      this.refilled = data.getInt("refilled");
   }

   @Override
   public void simulate(ISimulatedReactor reactor, int x, int y, boolean heatTick, boolean damageTick) {
   }

   @Override
   public boolean acceptUraniumPulse(
      ISimulatedReactor reactor, int x, int y, SimulatedStack source, int sourceX, int sourceY, boolean heatRun, boolean damageTick
   ) {
      if (this.unbreakable && this.damage >= this.maxDamage) {
         return false;
      } else {
         if (reactor.isSimulatingPulses()) {
            reactor.addFuelPulse();
         }

         if (reactor.isSteamReactor()) {
            if (damageTick) {
               if (this.damage >= this.maxDamage) {
                  this.damage = 0;
                  this.refilled++;
               } else {
                  this.damage++;
               }
            }

            return true;
         } else {
            if (!heatRun) {
               reactor.addOutput(1.0F);
            }

            if (damageTick) {
               if (this.damage >= this.maxDamage) {
                  this.damage = 0;
                  this.refilled++;
               } else {
                  this.damage++;
               }
            }

            return true;
         }
      }
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats() {
      return CollectionUtils.<IReactorPlannerComponent.ReactorStat>asList(IReactorPlannerComponent.ReactorStat.MAX_COMPONENT_DURABILITY);
   }

   @Override
   public IReactorPlannerComponent.ReactorType getValidType() {
      return IReactorPlannerComponent.ReactorType.UNIVERSAL;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getComponentType() {
      return IReactorPlannerComponent.ComponentType.REFLECTORS;
   }

   @Override
   public NumericTag getStat(IReactorPlannerComponent.ReactorStat stat) {
      return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.MAX_COMPONENT_DURABILITY ? IntTag.valueOf(this.maxDamage) : NULL_VALUE);
   }
}
