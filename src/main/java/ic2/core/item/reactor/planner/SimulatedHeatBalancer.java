package ic2.core.item.reactor.planner;

import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.BaseHeatSimulatedStack;
import ic2.api.reactor.planner.ISimulatedReactor;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.item.reactor.base.ExchangerProperty;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Vec2i;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class SimulatedHeatBalancer extends BaseHeatSimulatedStack {
   ExchangerProperty property;

   public SimulatedHeatBalancer(ExchangerProperty property) {
      super(property.getComponentID(), property.getHeatStorage());
      this.property = property;
   }

   @Override
   public ItemStack syncStack(ItemStack original) {
      original.setDamageValue(this.heat);
      return original;
   }

   @Override
   public void simulate(ISimulatedReactor reactor, int x, int y, boolean heatTick, boolean damageTick) {
      List<SimulatedCoord.SimulatedDirCoord> insert = new ObjectArrayList();
      List<SimulatedCoord.SimulatedDirCoord> extract = new ObjectArrayList();
      List<Vec2i> offsets = this.property.getOffsets();
      int i = 0;

      for (int m = offsets.size(); i < m; i++) {
         Vec2i pos = offsets.get(i);
         this.fetchInfo(reactor, x + pos.getX(), y + pos.getY(), insert, extract);
      }

      i = 0;
      i = this.processStep(reactor, x, y, i, false, insert);
      i = this.processStep(reactor, x, y, i, true, extract);
      this.storeHeat(reactor, x, y, i);
   }

   protected int processStep(ISimulatedReactor reactor, int x, int y, int myHeat, boolean absorb, List<SimulatedCoord.SimulatedDirCoord> process) {
      int side = this.property.getSelf();
      int react = this.property.getReactor();
      double med = (double)this.getStoredHeat(reactor, x, y) / (double)this.getMaxStoredHeat(reactor, x, y);
      int c = 1;
      boolean didReactor = false;
      if (react > 0) {
         double avg = (double)reactor.getHeat() / (double)reactor.getMaxHeat();
         if (absorb ? avg >= 0.75 : avg <= 0.25) {
            c++;
            med += avg;
            didReactor = true;
         }
      }

      if (side > 0) {
         int i = 0;

         for (int m = process.size(); i < m; i++) {
            med += process.get(i).average();
         }
      }

      med /= (double)(c + process.size());
      if (side > 0) {
         for (SimulatedCoord.SimulatedDirCoord coord : process) {
            int add = Mth.clamp(coord.getTransferRate(reactor, med), -side, side);
            myHeat -= add;
            myHeat += coord.storeHeat(reactor, add);
         }
      }

      if (react > 0 && didReactor) {
         int add = Mth.clamp((int)(med * (double)reactor.getMaxHeat() - (double)reactor.getHeat()), -react, react);
         myHeat -= add;
         reactor.setHeat(reactor.getHeat() + add);
      }

      return myHeat;
   }

   private void fetchInfo(
      ISimulatedReactor reactor, int x, int y, List<SimulatedCoord.SimulatedDirCoord> insert, List<SimulatedCoord.SimulatedDirCoord> extract
   ) {
      SimulatedStack comp = reactor.getItem(x, y);
      if (comp != null && comp.canStoreHeat(reactor, x, y)) {
         double max = (double)comp.getMaxStoredHeat(reactor, x, y);
         if (max <= 0.0) {
            return;
         }

         double average = (double)comp.getStoredHeat(reactor, x, y) / max;
         if (average <= 0.25) {
            insert.add(new SimulatedCoord.SimulatedDirCoord(comp, x, y, average));
         } else if (average >= 0.75) {
            extract.add(new SimulatedCoord.SimulatedDirCoord(comp, x, y, average));
         }
      }
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats() {
      return CollectionUtils.<IReactorPlannerComponent.ReactorStat>asList(
         IReactorPlannerComponent.ReactorStat.REACTOR_BALANCING, IReactorPlannerComponent.ReactorStat.PART_BALANCING
      );
   }

   @Override
   public IReactorPlannerComponent.ReactorType getValidType() {
      return IReactorPlannerComponent.ReactorType.UNIVERSAL;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getComponentType() {
      return IReactorPlannerComponent.ComponentType.HEAT_EXCHANGER;
   }

   @Override
   public NumericTag getStat(IReactorPlannerComponent.ReactorStat stat) {
      if (stat == IReactorPlannerComponent.ReactorStat.REACTOR_BALANCING) {
         return IntTag.valueOf(this.property.getReactor());
      } else {
         return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.PART_BALANCING ? IntTag.valueOf(this.property.getSelf()) : NULL_VALUE);
      }
   }
}
