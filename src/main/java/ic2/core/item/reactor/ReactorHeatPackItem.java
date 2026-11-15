package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.ISteamReactor;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.reactor.base.ReactorComponentBase;
import ic2.core.item.reactor.planner.SimulatedHeatPack;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public class ReactorHeatPackItem extends ReactorComponentBase {
   public ReactorHeatPackItem(String itemName, String textureFolder, String textureName) {
      super(itemName, textureFolder, textureName, new PropertiesBuilder().setRepair().maxStackSize(64));
   }

   @Override
   public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatCalculation, boolean damageTick) {
      if (!(reactor instanceof ISteamReactor)) {
         int want = 1000 * stack.getCount();
         if (reactor.getHeat() < want) {
            reactor.addHeat(stack.getCount());
         }
      }
   }

   @Override
   public boolean isValidForReactor(ItemStack stack, IReactor reactor) {
      return !(reactor instanceof ISteamReactor);
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
      return 0;
   }

   @Override
   public float getExplosionInfluence(ItemStack stack, IReactor reactor) {
      return (float)stack.getCount() / 10.0F;
   }

   @Override
   public SimulatedStack createSimulationComponent(ItemStack self) {
      return new SimulatedHeatPack((short)17, self.getCount());
   }

   @Override
   public short getComponentID(ItemStack stack) {
      return 17;
   }

   @Override
   public IReactorPlannerComponent.ReactorType getSupportedReactor(ItemStack stack) {
      return IReactorPlannerComponent.ReactorType.ELECTRIC;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getType(ItemStack stack) {
      return IReactorPlannerComponent.ComponentType.HEAT_PACK;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack stack) {
      List<IReactorPlannerComponent.ReactorStat> list = CollectionUtils.<IReactorPlannerComponent.ReactorStat>createList();
      list.add(IReactorPlannerComponent.ReactorStat.HEAT_PRODUCTION);
      return list;
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack) {
      return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.HEAT_PRODUCTION ? IntTag.valueOf(stack.getCount()) : NULL_VALUE);
   }
}
