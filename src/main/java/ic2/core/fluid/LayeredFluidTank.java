package ic2.core.fluid;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class LayeredFluidTank implements IFluidHandler, INetworkDataBuffer, IFluidTank {
   List<Consumer<LayeredFluidTank>> listeners = CollectionUtils.<Consumer<LayeredFluidTank>>createList();
   int capacity;
   int stored;
   Object2ObjectMap<Fluid, FluidStack> fluidMap = CollectionUtils.<Fluid, FluidStack>createLinkedMap();
   List<Fluid> fluids = CollectionUtils.<Fluid>createList();

   public LayeredFluidTank(int capacity) {
      this.capacity = capacity;
   }

   public void addListener(Consumer<LayeredFluidTank> listener) {
      this.listeners.add(listener);
   }

   public void notifyListeners() {
      for (Consumer<LayeredFluidTank> tanks : this.listeners) {
         tanks.accept(this);
      }
   }

   public void clear() {
      this.stored = 0;
      this.fluidMap.clear();
      this.fluids.clear();
   }

   public CompoundTag write(CompoundTag nbt) {
      ListTag list = new ListTag();
      ObjectIterator var3 = this.fluidMap.values().iterator();

      while (var3.hasNext()) {
         FluidStack fluid = (FluidStack)var3.next();
         list.add(fluid.writeToNBT(new CompoundTag()));
      }

      nbt.put("fluids", list);
      nbt.putInt("stored", this.stored);
      return nbt;
   }

   public void read(CompoundTag nbt) {
      this.stored = nbt.getInt("stored");

      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("fluids", 10), CompoundTag.class)) {
         FluidStack stack = FluidStack.loadFluidStackFromNBT(data);
         if (!stack.isEmpty()) {
            this.fluids.add(stack.getFluid());
            this.fluidMap.put(stack.getFluid(), stack);
         }
      }
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeInt(this.stored);
      buffer.writeShort((short)this.fluidMap.size());
      ObjectIterator var2 = this.fluidMap.values().iterator();

      while (var2.hasNext()) {
         FluidStack fluid = (FluidStack)var2.next();
         buffer.writeFluidStack(fluid);
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.stored = buffer.readInt();
      this.fluidMap.clear();
      this.fluids.clear();
      int limit = buffer.readShort();

      for (int i = 0; i < limit; i++) {
         FluidStack fluid = buffer.readFluidStack();
         if (!fluid.isEmpty()) {
            this.fluids.add(fluid.getFluid());
            this.fluidMap.put(fluid.getFluid(), fluid);
         }
      }
   }

   public int getTanks() {
      return Math.max(1, this.fluids.size());
   }

   public FluidStack getFluidInTank(int tank) {
      return tank >= this.fluids.size() ? FluidStack.EMPTY : (FluidStack)this.fluidMap.get(this.fluids.get(tank));
   }

   public int getTankCapacity(int tank) {
      return this.capacity;
   }

   public boolean isFluidValid(int tank, FluidStack stack) {
      return true;
   }

   public int fill(FluidStack resource, FluidAction action) {
      if (resource.isEmpty() || this.capacity - this.stored <= 0) {
         return 0;
      } else if (action.simulate()) {
         return Math.min(this.capacity - this.stored, resource.getAmount());
      } else {
         FluidStack stack = (FluidStack)this.fluidMap.getOrDefault(resource.getFluid(), FluidStack.EMPTY);
         if (stack.isEmpty()) {
            stack = resource.copy();
            this.fluidMap.put(stack.getFluid(), stack);
            this.fluids.add(stack.getFluid());
            stack.setAmount(0);
         }

         int added = Math.min(resource.getAmount(), this.capacity - this.stored);
         stack.grow(added);
         this.stored += added;
         this.notifyListeners();
         return added;
      }
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      if (this.stored > 0 && !resource.isEmpty()) {
         FluidStack found = (FluidStack)this.fluidMap.getOrDefault(resource.getFluid(), FluidStack.EMPTY);
         if (found.isEmpty()) {
            return FluidStack.EMPTY;
         } else {
            FluidStack drained = new FluidStack(found, Math.min(found.getAmount(), resource.getAmount()));
            if (action.execute()) {
               found.shrink(drained.getAmount());
               if (found.getAmount() <= 0) {
                  this.fluids.remove(resource.getFluid());
                  this.fluidMap.get(resource.getFluid());
               }

               this.stored = this.stored - drained.getAmount();
               this.notifyListeners();
            }

            return drained;
         }
      } else {
         return FluidStack.EMPTY;
      }
   }

   public FluidStack drain(int maxDrain, FluidAction action) {
      if (maxDrain > 0 && this.stored > 0) {
         FluidStack stack = (FluidStack)this.fluidMap.get(this.fluids.get(0));
         FluidStack drained = new FluidStack(stack, Math.min(stack.getAmount(), maxDrain));
         if (action.execute()) {
            stack.shrink(drained.getAmount());
            if (stack.getAmount() <= 0) {
               this.fluids.remove(drained.getFluid());
               this.fluidMap.get(drained.getFluid());
            }

            this.stored = this.stored - drained.getAmount();
            this.notifyListeners();
         }

         return drained;
      } else {
         return FluidStack.EMPTY;
      }
   }

   public FluidStack getFluid() {
      return this.fluids.isEmpty() ? FluidStack.EMPTY : (FluidStack)this.fluidMap.get(this.fluids.get(0));
   }

   public int getFluidAmount() {
      return this.stored;
   }

   public int getCapacity() {
      return this.capacity;
   }

   public boolean isFluidValid(FluidStack stack) {
      return true;
   }
}
