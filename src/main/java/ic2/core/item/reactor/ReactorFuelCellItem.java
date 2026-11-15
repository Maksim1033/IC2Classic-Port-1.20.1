package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.ISteamReactor;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.reactor.base.ReactorComponentBase;
import ic2.core.item.reactor.planner.SimulatedIsotopicCell;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public class ReactorFuelCellItem extends ReactorComponentBase {
   public ReactorFuelCellItem() {
      super("reactor_fuel_cell", "cells", "inactive_fuel", new PropertiesBuilder().setRepair().maxDamage(2500));
   }

   @Override
   public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatCalculation, boolean damageTick) {
   }

   @Override
   public boolean acceptUraniumPulse(
      ItemStack stack, IReactor reactor, ItemStack source, int myX, int myY, int sourceX, int sourceY, boolean heatRun, boolean damageTick
   ) {
      if (reactor instanceof ISteamReactor) {
         if (damageTick) {
            int level = this.getDamage(stack) - 1 - reactor.getHeat() / 1000;
            if (level <= 0) {
               reactor.setStackInReactor(myX, myY, new ItemStack(IC2Items.CELL_FUEL));
            } else {
               this.setDamage(stack, level);
            }
         }

         return true;
      } else {
         int level = this.getDamage(stack) - 1 - reactor.getHeat() / 1000;
         if (level <= 0) {
            reactor.setStackInReactor(myX, myY, new ItemStack(IC2Items.CELL_FUEL));
         } else {
            this.setDamage(stack, level);
         }

         return true;
      }
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
      return 0;
   }

   @Override
   public float getExplosionInfluence(ItemStack stack, IReactor reactor) {
      return 0.0F;
   }

   @Override
   public SimulatedStack createSimulationComponent(ItemStack self) {
      return new SimulatedIsotopicCell((short)52, this.getMaxDamage(self), 1000);
   }

   @Override
   public short getComponentID(ItemStack stack) {
      return 52;
   }

   @Override
   public IReactorPlannerComponent.ReactorType getSupportedReactor(ItemStack stack) {
      return IReactorPlannerComponent.ReactorType.ELECTRIC;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getType(ItemStack stack) {
      return IReactorPlannerComponent.ComponentType.ISOTOPE_CELL;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack stack) {
      List<IReactorPlannerComponent.ReactorStat> list = CollectionUtils.<IReactorPlannerComponent.ReactorStat>createList();
      list.add(IReactorPlannerComponent.ReactorStat.MAX_COMPONENT_DURABILITY);
      return list;
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack) {
      return stat == IReactorPlannerComponent.ReactorStat.MAX_COMPONENT_DURABILITY ? stat.createStat(this.getMaxDamage(stack)) : NULL_VALUE;
   }
}
