package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.api.util.DirectionList;
import ic2.core.item.reactor.base.ReactorComponentBase;
import ic2.core.item.reactor.planner.SimulatedSpreader;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ReactorHeatSpreaderItem extends ReactorComponentBase {
   public ReactorHeatSpreaderItem(String itemName, String textureFolder, String textureName) {
      super(itemName, textureFolder, textureName, null);
   }

   @Override
   public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatCalculation, boolean damageTick) {
      if (heatCalculation) {
         this.cool(reactor, x - 1, y);
         this.cool(reactor, x + 1, y);
         this.cool(reactor, x, y - 1);
         this.cool(reactor, x, y + 1);
      }
   }

   @Override
   public void addAffectedSlots(int x, int y, BiPredicate<Integer, Integer> slots) {
      slots.test(x, y);
      slots.test(x + 1, y);
      slots.test(x - 1, y);
      slots.test(x, y + 1);
      slots.test(x, y - 1);
   }

   @Override
   public boolean acceptUraniumPulse(
      ItemStack stack, IReactor reactor, ItemStack source, int myX, int myY, int sourceX, int sourceY, boolean heatRun, boolean damageTick
   ) {
      return false;
   }

   @Override
   public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return false;
   }

   @Override
   public int getStoredHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return 0;
   }

   @Override
   public int getMaxStoredHeat(ItemStack stack, IReactor reactor, int x, int y) {
      return 0;
   }

   @Override
   public int storeHeat(ItemStack stack, IReactor reactor, int x, int y, int heatChange) {
      return heatChange;
   }

   @Override
   public float getExplosionInfluence(ItemStack stack, IReactor reactor) {
      return 0.0F;
   }

   private void cool(IReactor reactor, int x, int y) {
      ItemStack stack = reactor.getStackInReactor(x, y);
      if (stack.getItem() instanceof IReactorComponent comp && comp.canStoreHeat(stack, reactor, x, y)) {
         comp.storeHeat(stack, reactor, x, y, -4);
      }
   }

   @Override
   public SimulatedStack createSimulationComponent(ItemStack self) {
      return new SimulatedSpreader((short)26, 4);
   }

   @Override
   public short getComponentID(ItemStack stack) {
      return 26;
   }

   @Override
   public IReactorPlannerComponent.ReactorType getSupportedReactor(ItemStack stack) {
      return IReactorPlannerComponent.ReactorType.UNIVERSAL;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getType(ItemStack stack) {
      return IReactorPlannerComponent.ComponentType.HEAT_SPREAD;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack stack) {
      List<IReactorPlannerComponent.ReactorStat> list = CollectionUtils.<IReactorPlannerComponent.ReactorStat>createList();
      list.add(IReactorPlannerComponent.ReactorStat.PART_COOLING);
      return list;
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack) {
      return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.PART_COOLING ? IntTag.valueOf(4) : NULL_VALUE);
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack, IReactor planner, int x, int y) {
      if (stat == IReactorPlannerComponent.ReactorStat.PART_COOLING) {
         int sides = 0;

         for (Direction direction : DirectionList.HORIZONTAL) {
            int newX = x + direction.getStepX();
            int newY = y + direction.getStepZ();
            ItemStack other = planner.getStackInReactor(newX, newY);
            Item var13 = other.getItem();
            if (var13 instanceof IReactorComponent) {
               IReactorComponent comp = (IReactorComponent)var13;
               if (comp.canStoreHeat(other, planner, x, y)) {
                  sides++;
               }
            }
         }

         return IntTag.valueOf(4 * sides);
      } else {
         return NULL_VALUE;
      }
   }
}
