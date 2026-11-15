package ic2.core.item.reactor;

import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.reactor.base.ReactorHeatStorageBase;
import ic2.core.item.reactor.planner.SimulatedHeatStorage;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public class ReactorHeatStorageItem extends ReactorHeatStorageBase {
   private short componentId;

   public ReactorHeatStorageItem(String name, int maxDamage, String textureFolder, String textureName, int componentId) {
      super(name, textureFolder, textureName, new PropertiesBuilder().maxDamage(maxDamage));
      this.componentId = (short)componentId;
   }

   public ReactorHeatStorageItem(String name, String textureFolder, String textureName, PropertiesBuilder properties, short componentId) {
      super(name, textureFolder, textureName, properties);
      this.componentId = componentId;
   }

   @Override
   public SimulatedStack createSimulationComponent(ItemStack self) {
      return new SimulatedHeatStorage(this.componentId, this.getMaxDamage(self));
   }

   @Override
   public short getComponentID(ItemStack stack) {
      return this.componentId;
   }

   @Override
   public IReactorPlannerComponent.ReactorType getSupportedReactor(ItemStack stack) {
      return IReactorPlannerComponent.ReactorType.UNIVERSAL;
   }

   @Override
   public IReactorPlannerComponent.ComponentType getType(ItemStack stack) {
      return IReactorPlannerComponent.ComponentType.COOLANT_CELL;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack stack) {
      List<IReactorPlannerComponent.ReactorStat> list = CollectionUtils.<IReactorPlannerComponent.ReactorStat>createList();
      list.add(IReactorPlannerComponent.ReactorStat.HEAT_STORAGE);
      return list;
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack) {
      return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.HEAT_STORAGE ? IntTag.valueOf(this.getMaxDamage(stack)) : NULL_VALUE);
   }
}
