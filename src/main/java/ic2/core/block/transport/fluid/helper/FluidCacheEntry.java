package ic2.core.block.transport.fluid.helper;

import ic2.core.IC2;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.transport.fluid.graph.IFluidPipe;
import ic2.core.inventory.base.INBTSavable;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidCacheEntry implements IntSupplier, Predicate<Fluid>, INBTSavable {
   final IFluidPipe pipe;
   final Direction dir;
   final ICache<IFluidHandler> cache;
   NonNullList<Fluid> inputs = NonNullList.withSize(9, Fluids.EMPTY);
   NonNullList<Fluid> outputs = NonNullList.withSize(9, Fluids.EMPTY);
   IFluidPipe.FluidOutput output;
   public Set<Fluid> filter = new ObjectOpenHashSet();
   public Set<Fluid> outputFilter = new ObjectLinkedOpenHashSet();
   public int priority = 0;
   public boolean whiteList = true;

   public FluidCacheEntry(IFluidPipe pipe, Direction dir, ICache<IFluidHandler> cache) {
      this.pipe = pipe;
      this.dir = dir;
      this.cache = cache;
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      NBTUtils.putBoolean(nbt, "white", this.whiteList, true);
      NBTUtils.putInt(nbt, "priority", this.priority, 0);
      ListTag list = new ListTag();

      for (int i = 0; i < 9; i++) {
         Fluid fluid = (Fluid)this.inputs.get(i);
         if (fluid != Fluids.EMPTY) {
            CompoundTag data = new CompoundTag();
            data.putString("id", ForgeRegistryWrapper.getFluidKey(fluid).toString());
            data.putByte("index", (byte)i);
            list.add(data);
         }
      }

      NBTUtils.put(nbt, "filters", list);
      list = new ListTag();

      for (int ix = 0; ix < 9; ix++) {
         Fluid fluid = (Fluid)this.outputs.get(ix);
         if (fluid != Fluids.EMPTY) {
            CompoundTag data = new CompoundTag();
            data.putString("id", ForgeRegistryWrapper.getFluidKey(fluid).toString());
            data.putByte("index", (byte)ix);
            list.add(data);
         }
      }

      NBTUtils.put(nbt, "outputs", list);
      return nbt;
   }

   public void clear() {
      this.whiteList = true;
      this.inputs.clear();
      this.filter.clear();
      this.priority = 0;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.whiteList = NBTUtils.getBoolean(nbt, "white", true);
      this.priority = nbt.getInt("priority");

      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("filters", 10), CompoundTag.class)) {
         int slot = data.getInt("index");
         if (slot >= 0 && slot < 9) {
            Fluid fluid = ForgeRegistryWrapper.getFluidValue(new ResourceLocation(data.getString("id")));
            this.inputs.set(slot, fluid);
            if (fluid != Fluids.EMPTY) {
               this.filter.add(fluid);
            }
         }
      }

      for (CompoundTag datax : NBTListWrapper.wrap(nbt.getList("outputs", 10), CompoundTag.class)) {
         int slot = datax.getInt("index");
         if (slot >= 0 && slot < 9) {
            Fluid fluid = ForgeRegistryWrapper.getFluidValue(new ResourceLocation(datax.getString("id")));
            this.outputs.set(slot, fluid);
            if (fluid != Fluids.EMPTY) {
               this.outputFilter.add(fluid);
            }
         }
      }
   }

   public boolean updateOutput() {
      IFluidHandler handler = this.cache.getHandler(this.dir);
      if (handler != null && !this.pipe.hasAnchor(this.dir)) {
         if (this.output != null && this.output.getHandler() == handler) {
            return true;
         } else {
            this.output = new IFluidPipe.FluidOutput(this.pipe, this.pipe.getPosition().relative(this.dir), this.dir, handler, this, this);
            return true;
         }
      } else {
         this.output = null;
         return false;
      }
   }

   public IFluidPipe.FluidOutput getOutput() {
      return this.output;
   }

   public boolean hasOutput() {
      return this.output != null;
   }

   public boolean test(Fluid t) {
      return this.filter.isEmpty() && this.whiteList || this.filter.contains(t) == this.whiteList;
   }

   @Override
   public int getAsInt() {
      return this.priority;
   }

   public void setFluid(int slot, Fluid fluid, boolean input) {
      if (input) {
         this.inputs.set(slot, fluid);
         if (IC2.PLATFORM.isSimulating()) {
            this.filter.clear();

            for (int i = 0; i < 9; i++) {
               fluid = (Fluid)this.inputs.get(i);
               if (fluid != Fluids.EMPTY) {
                  this.filter.add(fluid);
               }
            }
         }
      } else {
         this.outputs.set(slot, fluid);
         if (IC2.PLATFORM.isSimulating()) {
            this.outputFilter.clear();

            for (int ix = 0; ix < 9; ix++) {
               fluid = (Fluid)this.outputs.get(ix);
               if (fluid != Fluids.EMPTY) {
                  this.outputFilter.add(fluid);
               }
            }
         }
      }
   }

   public Fluid getFluid(int slot, boolean input) {
      return (Fluid)(input ? this.inputs : this.outputs).get(slot);
   }

   public void addPriority(int value) {
      this.priority += value;
   }

   public void setWhitelist(boolean value) {
      this.whiteList = value;
   }
}
