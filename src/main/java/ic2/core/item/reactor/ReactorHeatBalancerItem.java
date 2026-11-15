package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.reactor.base.ExchangerProperty;
import ic2.core.item.reactor.base.ItemStackCoord;
import ic2.core.item.reactor.base.ReactorHeatStorageBase;
import ic2.core.item.reactor.planner.SimulatedHeatBalancer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Vec2i;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ReactorHeatBalancerItem extends ReactorHeatStorageBase {
   ExchangerProperty prop;

   public ReactorHeatBalancerItem(String name, ExchangerProperty prop) {
      super(name, prop.getTextureFolder(), prop.getTextureName(), new PropertiesBuilder().maxDamage(prop.getHeatStorage()));
      this.prop = prop;
   }

   public ReactorHeatBalancerItem(String name, PropertiesBuilder properties, ExchangerProperty prop) {
      super(name, prop.getTextureFolder(), prop.getTextureName(), (properties == null ? new PropertiesBuilder() : properties).maxDamage(prop.getHeatStorage()));
      this.prop = prop;
   }

   protected int processStep(ItemStack stack, IReactor reactor, int x, int y, int myHeat, boolean absorb, List<ItemStackCoord.ItemStackDirCoord> process) {
      int side = this.prop.getSelf();
      int react = this.prop.getReactor();
      double med = (double)this.getStoredHeat(stack, reactor, x, y) / (double)this.getMaxStoredHeat(stack, reactor, x, y);
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
         for (ItemStackCoord coord : process) {
            IReactorComponent heatable = (IReactorComponent)coord.stack.getItem();
            int add = Mth.clamp(coord.getTransferRate(heatable, reactor, med), -side, side);
            myHeat -= add;
            myHeat += coord.storeHeat(heatable, reactor, add);
         }
      }

      if (react > 0 && didReactor) {
         int add = Mth.clamp((int)(med * (double)reactor.getMaxHeat() - (double)reactor.getHeat()), -react, react);
         myHeat -= add;
         reactor.setHeat(reactor.getHeat() + add);
      }

      return myHeat;
   }

   @Override
   public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatCalculation, boolean damageTick) {
      List<ItemStackCoord.ItemStackDirCoord> insert = new ObjectArrayList();
      List<ItemStackCoord.ItemStackDirCoord> extract = new ObjectArrayList();
      List<Vec2i> offsets = this.prop.getOffsets();
      int i = 0;

      for (int m = offsets.size(); i < m; i++) {
         Vec2i pos = offsets.get(i);
         this.fetchInfo(reactor, x + pos.getX(), y + pos.getY(), insert, extract);
      }

      i = 0;
      i = this.processStep(stack, reactor, x, y, i, false, insert);
      i = this.processStep(stack, reactor, x, y, i, true, extract);
      this.storeHeat(stack, reactor, x, y, i);
   }

   private void fetchInfo(IReactor reactor, int x, int y, List<ItemStackCoord.ItemStackDirCoord> insert, List<ItemStackCoord.ItemStackDirCoord> extract) {
      ItemStack thing = reactor.getStackInReactor(x, y);
      if (thing.getItem() instanceof IReactorComponent comp && comp.canStoreHeat(thing, reactor, x, y)) {
         double max = (double)comp.getMaxStoredHeat(thing, reactor, x, y);
         if (max <= 0.0) {
            return;
         }

         double average = (double)comp.getStoredHeat(thing, reactor, x, y) / max;
         if (average <= 0.25) {
            insert.add(new ItemStackCoord.ItemStackDirCoord(thing, x, y, average));
         } else if (average >= 0.75) {
            extract.add(new ItemStackCoord.ItemStackDirCoord(thing, x, y, average));
         }
      }
   }

   @Override
   public void addAffectedSlots(int x, int y, BiPredicate<Integer, Integer> slots) {
      slots.test(x, y);
      if (this.prop.getSelf() > 0) {
         slots.test(x + 1, y);
         slots.test(x - 1, y);
         slots.test(x, y + 1);
         slots.test(x, y - 1);
      }
   }

   @Override
   public SimulatedStack createSimulationComponent(ItemStack self) {
      return new SimulatedHeatBalancer(this.prop);
   }

   @Override
   public short getComponentID(ItemStack stack) {
      return this.prop.getComponentID();
   }

   @Override
   public IReactorPlannerComponent.ReactorType getSupportedReactor(ItemStack stack) {
      return IReactorPlannerComponent.ReactorType.UNIVERSAL;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getType(ItemStack stack) {
      return IReactorPlannerComponent.ComponentType.HEAT_EXCHANGER;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack stack) {
      List<IReactorPlannerComponent.ReactorStat> list = CollectionUtils.<IReactorPlannerComponent.ReactorStat>createList();
      list.add(IReactorPlannerComponent.ReactorStat.REACTOR_BALANCING);
      list.add(IReactorPlannerComponent.ReactorStat.PART_BALANCING);
      return list;
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack) {
      if (stat == IReactorPlannerComponent.ReactorStat.REACTOR_BALANCING) {
         return IntTag.valueOf(this.prop.getReactor());
      } else {
         return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.PART_BALANCING ? IntTag.valueOf(this.prop.getSelf()) : NULL_VALUE);
      }
   }
}
