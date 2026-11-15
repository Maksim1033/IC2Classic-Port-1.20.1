package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.reactor.base.ReactorComponentBase;
import ic2.core.item.reactor.planner.SimulatedReactorPlating;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public class ReactorPlatingItem extends ReactorComponentBase {
   private int heatModifier;
   private float effectModifier;
   private short componentId;

   public ReactorPlatingItem(String itemName, String textureFolder, String textureName, int heatModifier, float effectModifier, int componentId) {
      super(itemName, textureFolder, textureName, null);
      this.heatModifier = heatModifier;
      this.effectModifier = effectModifier;
      this.componentId = (short)componentId;
   }

   public ReactorPlatingItem(
      String itemName, String textureFolder, String textureName, PropertiesBuilder builder, int heatModifier, float effectModifier, int componentId
   ) {
      super(itemName, textureFolder, textureName, builder);
      this.heatModifier = heatModifier;
      this.effectModifier = effectModifier;
      this.componentId = (short)componentId;
   }

   @Override
   public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatCalculation, boolean damageTick) {
      if (heatCalculation) {
         reactor.setMaxHeat(reactor.getMaxHeat() + this.heatModifier);
         reactor.setHeatEffectModifier(reactor.getHeatEffectModifier() * this.effectModifier);
      }
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
      return this.effectModifier >= 1.0F ? 0.0F : this.effectModifier;
   }

   @Override
   public SimulatedStack createSimulationComponent(ItemStack self) {
      return new SimulatedReactorPlating(this.componentId, this.heatModifier, this.effectModifier);
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
      return IReactorPlannerComponent.ComponentType.PLATING;
   }

   @Override
   public List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack stack) {
      List<IReactorPlannerComponent.ReactorStat> list = CollectionUtils.<IReactorPlannerComponent.ReactorStat>createList();
      list.add(IReactorPlannerComponent.ReactorStat.MAX_HEAT_STORAGE);
      list.add(IReactorPlannerComponent.ReactorStat.REACTOR_HEAT_EFFECT_MULTIPLIER);
      return list;
   }

   @Override
   public NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack) {
      if (stat == IReactorPlannerComponent.ReactorStat.MAX_HEAT_STORAGE) {
         return IntTag.valueOf(this.heatModifier);
      } else {
         return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.REACTOR_HEAT_EFFECT_MULTIPLIER ? FloatTag.valueOf(this.effectModifier) : NULL_VALUE);
      }
   }
}
