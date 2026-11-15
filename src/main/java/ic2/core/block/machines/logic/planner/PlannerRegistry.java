package ic2.core.block.machines.logic.planner;

import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.block.machines.logic.planner.encoder.EncoderRegistry;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortComparator;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PlannerRegistry {
   static final Map<Item, ComponentInfo> DATA = CollectionUtils.<Item, ComponentInfo>createLinkedMap();
   static final NonNullList<ItemStack> REGISTRY = NonNullList.withSize(32767, ItemStack.EMPTY);
   static ItemStack[] REACTOR_COMPONENTS;
   static ItemStack[] STEAM_COMPONENTS;
   static final Object2ObjectMap<IReactorPlannerComponent.ComponentType, ShortList>[] TYPES = CollectionUtils.createMaps(3, true);
   static final Function<IReactorPlannerComponent.ComponentType, ShortList> CREATOR = T -> new ShortArrayList();
   static final PlannerRegistry.ComponentSorter SORTER = new PlannerRegistry.ComponentSorter();
   static final PlannerRegistry.TypeSorter TYPE_SORTER = new PlannerRegistry.TypeSorter();

   public static void init() {
      List<ItemStack>[] lists = CollectionUtils.createLists(2);
      NonNullList<ItemStack> helper = NonNullList.create();

      for (Item item : ForgeRegistries.ITEMS) {
         if (item instanceof IReactorPlannerComponent planner) {
            planner.provideComponents(helper);
            if (helper.size() != 1) {
               throw new IllegalStateException("Reactor Planner Items are supposed only to have 1 ItemStack per Item");
            }

            ItemStack stack = (ItemStack)helper.get(0);
            SimulatedStack sim = planner.createSimulationComponent(stack);
            if (sim == null) {
               throw new IllegalStateException("Reactor Planner Item couldn't create a Simulated Stack");
            }

            ComponentInfo info = new ComponentInfo(sim);
            if (!((ItemStack)REGISTRY.get(info.getId())).isEmpty()) {
               throw new IllegalStateException(
                  "ID: "
                     + info.getId()
                     + ". is Already Used by "
                     + ((ItemStack)REGISTRY.get(info.getId())).getHoverName().getString()
                     + " in the Planner Registry"
               );
            }

            stack = stack.copy();
            DATA.put(stack.getItem(), info);
            REGISTRY.set(info.getId(), stack);
            IReactorPlannerComponent.ReactorType type = sim.getValidType();
            if (type == IReactorPlannerComponent.ReactorType.ELECTRIC || type == IReactorPlannerComponent.ReactorType.UNIVERSAL) {
               lists[0].add(stack);
               ((ShortList)TYPES[1].computeIfAbsent(info.getType(), CREATOR)).add(info.getId());
            }

            if (type == IReactorPlannerComponent.ReactorType.STEAM || type == IReactorPlannerComponent.ReactorType.UNIVERSAL) {
               lists[1].add(stack);
               ((ShortList)TYPES[2].computeIfAbsent(info.getType(), CREATOR)).add(info.getId());
            }

            ((ShortList)TYPES[0].computeIfAbsent(info.getType(), CREATOR)).add(info.getId());
            helper.clear();
         }
      }

      lists[0].sort(SORTER);
      lists[1].sort(SORTER);
      ObjectIterator var9 = TYPES[0].values().iterator();

      while (var9.hasNext()) {
         ShortList typex = (ShortList)var9.next();
         typex.sort(TYPE_SORTER);
      }

      var9 = TYPES[1].values().iterator();

      while (var9.hasNext()) {
         ShortList typex = (ShortList)var9.next();
         typex.sort(TYPE_SORTER);
      }

      var9 = TYPES[2].values().iterator();

      while (var9.hasNext()) {
         ShortList typex = (ShortList)var9.next();
         typex.sort(TYPE_SORTER);
      }

      REACTOR_COMPONENTS = lists[0].toArray(ItemStack[]::new);
      STEAM_COMPONENTS = lists[1].toArray(ItemStack[]::new);
      EncoderRegistry.INSTANCE.init();
   }

   public static ComponentInfo getInfo(ItemStack stack) {
      return DATA.get(stack.getItem());
   }

   public static ItemStack getByID(int id) {
      return ((ItemStack)REGISTRY.get(id)).copy();
   }

   public static short getId(ItemStack stack) {
      ComponentInfo info = getInfo(stack);
      return info == null ? -1 : info.getId();
   }

   public static IReactorPlannerComponent.ComponentType getType(ItemStack stack) {
      ComponentInfo info = getInfo(stack);
      return info == null ? null : info.getType();
   }

   public static int getStackSize(ItemStack stack) {
      return stack.getItem() instanceof IReactorPlannerComponent ? ((IReactorPlannerComponent)stack.getItem()).getStackSize(stack) : stack.getCount();
   }

   public static ItemStack setStackSize(ItemStack stack, int count) {
      if (stack.getItem() instanceof IReactorPlannerComponent) {
         return ((IReactorPlannerComponent)stack.getItem()).applyStackSize(stack, count);
      } else {
         stack.setCount(Math.min(count, stack.getMaxStackSize()));
         return stack;
      }
   }

   public static ItemStack[] getByType(IReactorPlannerComponent.ComponentType type, boolean steam) {
      if (type == null) {
         return steam ? STEAM_COMPONENTS : REACTOR_COMPONENTS;
      } else {
         ShortList list = (ShortList)TYPES[steam ? 2 : 1].get(type);
         if (list != null && !list.isEmpty()) {
            ItemStack[] items = new ItemStack[list.size()];
            int i = 0;

            for (int m = list.size(); i < m; i++) {
               items[i] = (ItemStack)REGISTRY.get(list.getShort(i));
            }

            return items;
         } else {
            return new ItemStack[0];
         }
      }
   }

   public static class ComponentSorter implements Comparator<ItemStack> {
      public int compare(ItemStack o1, ItemStack o2) {
         ComponentInfo a = PlannerRegistry.getInfo(o1);
         ComponentInfo b = PlannerRegistry.getInfo(o2);
         if (a == null && b == null) {
            return 0;
         } else if (a != null && b != null) {
            int sorted = Integer.compare(a.getType().getIndex(), b.getType().getIndex());
            return sorted != 0 ? sorted : Integer.compare(a.getId(), b.getId());
         } else {
            return a == null ? 1 : 0;
         }
      }
   }

   public static class TypeSorter implements ShortComparator {
      public int compare(short k1, short k2) {
         return PlannerRegistry.SORTER.compare((ItemStack)PlannerRegistry.REGISTRY.get(k1), (ItemStack)PlannerRegistry.REGISTRY.get(k2));
      }
   }
}
