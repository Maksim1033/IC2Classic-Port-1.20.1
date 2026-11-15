package ic2.api.reactor;

import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public interface IReactorPlannerComponent extends IReactorComponent {
   DecimalFormat FORMAT = new DecimalFormat("###,###.##");
   NumericTag NULL_VALUE = IntTag.valueOf(0);

   default ItemStack applyStackSize(ItemStack newStack, int size) {
      newStack.setCount(Mth.clamp(size, 0, newStack.getMaxStackSize()));
      return newStack;
   }

   default int getStackSize(ItemStack input) {
      return input.getCount();
   }

   void provideComponents(NonNullList<ItemStack> var1);

   short getComponentID(ItemStack var1);

   SimulatedStack createSimulationComponent(ItemStack var1);

   default void addToolTip(ItemStack stack, Consumer<Component> tooltips) {
   }

   void addAffectedSlots(int var1, int var2, BiPredicate<Integer, Integer> var3);

   IReactorPlannerComponent.ReactorType getSupportedReactor(ItemStack var1);

   IReactorPlannerComponent.ComponentType getType(ItemStack var1);

   List<IReactorPlannerComponent.ReactorStat> getStats(ItemStack var1);

   NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat var1, ItemStack var2);

   default NumericTag getReactorStat(IReactorPlannerComponent.ReactorStat stat, ItemStack stack, IReactor planner, int x, int y) {
      return this.getReactorStat(stat, stack);
   }

   public static class ComponentType {
      static final List<IReactorPlannerComponent.ComponentType> TYPES = ObjectLists.synchronize(CollectionUtils.createList());
      public static final IReactorPlannerComponent.ComponentType FUEL_ROD = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.fuel_rod"
      );
      public static final IReactorPlannerComponent.ComponentType COOLANT_CELL = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.coolant_cell"
      );
      public static final IReactorPlannerComponent.ComponentType CONDENSATOR = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.condensator"
      );
      public static final IReactorPlannerComponent.ComponentType HEAT_PACK = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.heat_pack"
      );
      public static final IReactorPlannerComponent.ComponentType HEAT_VENT = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.heat_vent"
      );
      public static final IReactorPlannerComponent.ComponentType HEAT_SPREAD = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.heat_spread"
      );
      public static final IReactorPlannerComponent.ComponentType HEAT_EXCHANGER = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.heat_exchanger"
      );
      public static final IReactorPlannerComponent.ComponentType HEAT_PUMP = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.heat_pump"
      );
      public static final IReactorPlannerComponent.ComponentType PLATING = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.plating"
      );
      public static final IReactorPlannerComponent.ComponentType REFLECTORS = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.reflectors"
      );
      public static final IReactorPlannerComponent.ComponentType ISOTOPE_CELL = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.isotope_cell"
      );
      public static final IReactorPlannerComponent.ComponentType UNDEFINED = new IReactorPlannerComponent.ComponentType(
         "gui.ic2.reactor_planner.component.undefined"
      );
      int index;
      Component name;

      public ComponentType(String name) {
         this.name = Component.translatable(name);
         this.index = TYPES.size();
         TYPES.add(this);
      }

      public Component getName() {
         return this.name;
      }

      public int getIndex() {
         return this.index;
      }

      public static int size() {
         return TYPES.size();
      }

      public static IReactorPlannerComponent.ComponentType byID(int index) {
         return TYPES.get(index % TYPES.size());
      }
   }

   public static enum ReactorStat {
      HEAT_PRODUCTION(false, "gui.ic2.reactor_planner.stat.heat_production"),
      ENERGY_PRODUCTION(true, "gui.ic2.reactor_planner.stat.energy_production"),
      STEAM_PRODUCTION(true, "gui.ic2.reactor_planner.stat.steam_production"),
      ROD_COUNT(false, "gui.ic2.reactor_planner.stat.rod_count"),
      PULSE_COUNT(false, "gui.ic2.reactor_planner.stat.pulse_count"),
      SELF_COOLING(false, "gui.ic2.reactor_planner.stat.self_cooling"),
      REACTOR_COOLING(false, "gui.ic2.reactor_planner.stat.reactor_cooling"),
      PART_COOLING(false, "gui.ic2.reactor_planner.stat.part_cooling"),
      PART_BALANCING(false, "gui.ic2.reactor_planner.stat.part_balance"),
      REACTOR_BALANCING(false, "gui.ic2.reactor_planner.stat.reactor_balance"),
      HEAT_STORAGE(false, "gui.ic2.reactor_planner.stat.heat_storage"),
      MAX_HEAT_STORAGE(false, "gui.ic2.reactor_planner.stat.max_heat_storage"),
      REACTOR_HEAT_EFFECT_MODIFIER(true, "gui.ic2.reactor_planner.stat.reactor_hem"),
      REACTOR_HEAT_EFFECT_MULTIPLIER(true, "gui.ic2.reactor_planner.stat.reactor_hem_mul"),
      MAX_COMPONENT_DURABILITY(false, "gui.ic2.reactor_planner.stat.max_durability"),
      RECHARGEABLE(false, "gui.ic2.reactor_planner.stat.rechargeable"),
      ENERGY_USAGE(true, "gui.ic2.reactor_planner.stat.energy_usage"),
      WATER_CONSUMPTION(true, "gui.ic2.reactor_planner.stat.water_consumption"),
      WATER_STORAGE(true, "gui.ic2.reactor_planner.stat.water_storage");

      final boolean isFloat;
      final String name;

      private ReactorStat(boolean isFloat, String name) {
         this.isFloat = isFloat;
         this.name = name;
      }

      public NumericTag createStat(Number nr) {
         return (NumericTag)(this.isFloat ? FloatTag.valueOf(nr.floatValue()) : IntTag.valueOf(nr.intValue()));
      }

      public boolean isFloat() {
         return this.isFloat;
      }

      public Component getName(NumericTag nbt) {
         return Component.translatable(
            this.name,
            new Object[]{this.isFloat ? IReactorPlannerComponent.FORMAT.format(nbt.getAsDouble()) : IReactorPlannerComponent.FORMAT.format(nbt.getAsLong())}
         );
      }
   }

   public static enum ReactorType {
      ELECTRIC,
      STEAM,
      UNIVERSAL;
   }
}
