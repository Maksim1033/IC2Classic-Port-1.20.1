package ic2.core.item.reactor.planner;

import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.BaseHeatSimulatedStack;
import ic2.api.reactor.planner.ISimulatedReactor;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public class SimulatedCondensator extends BaseHeatSimulatedStack {
   protected int reset = 0;

   public SimulatedCondensator(short id, int maxHeat) {
      super(id, maxHeat);
   }

   @Override
   public ItemStack syncStack(ItemStack original) {
      original.setDamageValue(this.heat);
      CompoundTag tag = original.getOrCreateTag();
      if (this.heatChanges.getCount() > 0) {
         tag.putInt("reset", this.reset);
         tag.putInt("total", this.heatChanges.getTotal());
         tag.putFloat("average", this.heatChanges.getAverage());
      } else {
         tag.remove("reset");
         tag.remove("total");
         tag.remove("average");
      }

      return original;
   }

   @Override
   public void reset() {
      super.reset();
      this.reset = 0;
   }

   @Override
   public void simulate(ISimulatedReactor planner, int x, int y, boolean heatTick, boolean damageTick) {
   }

   @Override
   public boolean canViewHeat(ISimulatedReactor reactor, int x, int y) {
      return false;
   }

   @Override
   public int getStoredHeat(ISimulatedReactor reactor, int x, int y) {
      return 0;
   }

   @Override
   public int storeHeat(ISimulatedReactor reactor, int x, int y, int heatChange) {
      int can = Math.min(this.maxHeat - (this.heat + 1), heatChange);
      if (can < heatChange) {
         this.reset++;
         this.heat = 0;
         can = Math.min(this.maxHeat - (this.heat + 1), heatChange);
      }

      this.heat += can;
      this.heatChanges.addChange(can);
      return heatChange - can;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats() {
      return CollectionUtils.<IReactorPlannerComponent.ReactorStat>asList(IReactorPlannerComponent.ReactorStat.HEAT_STORAGE);
   }

   @Override
   public IReactorPlannerComponent.ReactorType getValidType() {
      return IReactorPlannerComponent.ReactorType.UNIVERSAL;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getComponentType() {
      return IReactorPlannerComponent.ComponentType.CONDENSATOR;
   }

   @Override
   public NumericTag getStat(IReactorPlannerComponent.ReactorStat stat) {
      return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.HEAT_STORAGE ? IntTag.valueOf(this.maxHeat) : NULL_VALUE);
   }
}
