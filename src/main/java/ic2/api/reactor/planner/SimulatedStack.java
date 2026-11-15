package ic2.api.reactor.planner;

import ic2.api.reactor.IReactorPlannerComponent;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;

public interface SimulatedStack {
   NumericTag NULL_VALUE = IntTag.valueOf(0);

   ItemStack syncStack(ItemStack var1);

   CompoundTag save();

   void load(CompoundTag var1);

   void commitState();

   void reset();

   void simulate(ISimulatedReactor var1, int var2, int var3, boolean var4, boolean var5);

   boolean acceptUraniumPulse(ISimulatedReactor var1, int var2, int var3, SimulatedStack var4, int var5, int var6, boolean var7, boolean var8);

   boolean canStoreHeat(ISimulatedReactor var1, int var2, int var3);

   int getStoredHeat(ISimulatedReactor var1, int var2, int var3);

   int getMaxStoredHeat(ISimulatedReactor var1, int var2, int var3);

   int storeHeat(ISimulatedReactor var1, int var2, int var3, int var4);

   boolean canViewHeat(ISimulatedReactor var1, int var2, int var3);

   float getExplosionInfluence(ISimulatedReactor var1);

   short getId();

   List<IReactorPlannerComponent.ReactorStat> getStats();

   IReactorPlannerComponent.ReactorType getValidType();

   IReactorPlannerComponent.ComponentType getComponentType();

   NumericTag getStat(IReactorPlannerComponent.ReactorStat var1);

   default NumericTag getStat(IReactorPlannerComponent.ReactorStat stat, ISimulatedReactor reactor, int x, int y) {
      return this.getStat(stat);
   }
}
