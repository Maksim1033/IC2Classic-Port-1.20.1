package ic2.core.item.reactor.planner;

import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.BaseDurabilitySimulatedStack;
import ic2.api.reactor.planner.ISimulatedReactor;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public class SimulatedIsotopicCell extends BaseDurabilitySimulatedStack {
   protected int finished = 0;
   protected int heatRequired;

   public SimulatedIsotopicCell(short id, int maxDamage, int heatRequired) {
      super(id, maxDamage);
      this.damage = maxDamage;
      this.heatRequired = heatRequired;
   }

   @Override
   public ItemStack syncStack(ItemStack original) {
      original.setDamageValue(this.damage);
      if (this.finished > 0) {
         original.addTagElement("breed", IntTag.valueOf(this.finished));
      } else {
         original.removeTagKey("breed");
      }

      return original;
   }

   @Override
   public void reset() {
      this.damage = this.maxDamage;
      this.finished = 0;
   }

   @Override
   public void simulate(ISimulatedReactor reactor, int x, int y, boolean heatTick, boolean damageTick) {
   }

   @Override
   public boolean acceptUraniumPulse(
      ISimulatedReactor reactor, int x, int y, SimulatedStack source, int sourceX, int sourceY, boolean heatRun, boolean damageTick
   ) {
      if (reactor.isSimulatingPulses()) {
         reactor.addBreedingPulse();
      }

      if (reactor.isSteamReactor()) {
         if (damageTick) {
            int level = this.damage - 1 - reactor.getHeat() / this.heatRequired;
            if (level <= 0) {
               this.damage = this.maxDamage;
               this.finished++;
            } else {
               this.damage = level;
            }
         }

         return true;
      } else {
         int level = this.damage - 1 - reactor.getHeat() / this.heatRequired;
         if (level <= 0) {
            this.damage = this.maxDamage;
            this.finished++;
         } else {
            this.damage = level;
         }

         return true;
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
      return IReactorPlannerComponent.ComponentType.ISOTOPE_CELL;
   }

   @Override
   public NumericTag getStat(IReactorPlannerComponent.ReactorStat stat) {
      return (NumericTag)(stat == IReactorPlannerComponent.ReactorStat.MAX_COMPONENT_DURABILITY ? IntTag.valueOf(this.maxDamage) : NULL_VALUE);
   }
}
