package ic2.core.platform.recipes.helpers;

import ic2.core.inventory.filter.FluidFilter;
import ic2.core.inventory.filter.IFilter;
import ic2.core.item.base.IItemCategoryFiller;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemStackCache {
   static final ObjectArrayList<ItemStack> EMPTY = ObjectArrayList.wrap(new ItemStack[0]);
   public static final ItemStackCache INSTANCE = new ItemStackCache();
   ObjectArrayList<ItemStack> cache = ObjectArrayList.wrap(new ItemStack[0]);
   Map<ResourceLocation, ObjectArrayList<ItemStack>> requestedFilter = CollectionUtils.<ResourceLocation, ObjectArrayList<ItemStack>>createLinkedMap();
   Map<Fluid, ObjectArrayList<ItemStack>> fluidFilter = CollectionUtils.<Fluid, ObjectArrayList<ItemStack>>createLinkedMap();

   public void init() {
      this.cache.clear();
      NonNullList<ItemStack> helper = NonNullList.create();

      for (Item item : ForgeRegistries.ITEMS) {
         if (item != Items.AIR) {
            helper.clear();
            if (item instanceof IItemCategoryFiller filler) {
               filler.m_6787_(null, helper);
            } else {
               helper.add(new ItemStack(item));
            }

            for (ItemStack stack : helper) {
               this.cache.add(stack.copy());
            }
         }
      }

      this.fluidFilter.clear();
      this.requestedFilter.clear();
      ItemStack[] elements = (ItemStack[])this.cache.elements();
      List<Tuple<ResourceLocation, IFilter>> filterList = CollectionUtils.<Tuple<ResourceLocation, IFilter>>createList();
      MinecraftForge.EVENT_BUS.post(new ItemStackCache.StackCacheGeneratorEvent(filterList));

      for (Tuple<ResourceLocation, IFilter> entry : filterList) {
         ObjectArrayList<ItemStack> list = ObjectArrayList.wrap(new ItemStack[50], 0);
         IFilter filter = (IFilter)entry.getB();
         int i = 0;

         for (int m = this.cache.size(); i < m; i++) {
            if (filter.matches(elements[i])) {
               list.add(elements[i]);
            }
         }

         this.requestedFilter.put((ResourceLocation)entry.getA(), list);
      }

      for (Fluid fluid : ForgeRegistries.FLUIDS) {
         if (fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
            ObjectArrayList<ItemStack> list = ObjectArrayList.wrap(new ItemStack[10], 0);
            IFilter filter = new FluidFilter(fluid);
            int i = 0;

            for (int mx = this.cache.size(); i < mx; i++) {
               if (filter.matches(elements[i])) {
                  list.add(elements[i]);
               }
            }

            this.fluidFilter.put(fluid, list);
         }
      }
   }

   public ObjectArrayList<ItemStack> getCustomCache(ResourceLocation location) {
      return this.requestedFilter.getOrDefault(location, EMPTY);
   }

   public ObjectArrayList<ItemStack> getFluidContainers(Fluid fluid) {
      return this.fluidFilter.getOrDefault(fluid, EMPTY);
   }

   public int size() {
      return this.cache.size();
   }

   public ObjectArrayList<ItemStack> getCache() {
      return this.cache;
   }

   public ItemStack[] getRawCache() {
      return (ItemStack[])this.cache.elements();
   }

   public static class StackCacheGeneratorEvent extends Event {
      final List<Tuple<ResourceLocation, IFilter>> list;

      public StackCacheGeneratorEvent(List<Tuple<ResourceLocation, IFilter>> list) {
         this.list = list;
      }

      public void addSearchFilter(String mod, String name, IFilter filter) {
         this.addSearchFilter(new ResourceLocation(mod, name), filter);
      }

      public void addSearchFilter(ResourceLocation id, IFilter filter) {
         if (filter != null && id != null) {
            this.list.add(new Tuple(id, filter));
         } else {
            throw new RuntimeException("Nulls are not allowed. " + id + " : " + filter);
         }
      }
   }
}
