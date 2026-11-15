package ic2.core.fluid;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class IC2Tank extends FluidTank implements INetworkDataBuffer {
   List<Consumer<IC2Tank>> listeners;

   public IC2Tank(int capacity) {
      this(capacity, e -> true);
   }

   public IC2Tank(int capacity, Predicate<FluidStack> validator) {
      super(capacity, validator);
   }

   public IC2Tank addListener(Consumer<IC2Tank> listener) {
      if (this.listeners == null) {
         this.listeners = CollectionUtils.<Consumer<IC2Tank>>createList();
      }

      this.listeners.add(listener);
      return this;
   }

   public IC2Tank removeListener(Consumer<IC2Tank> listener) {
      if (this.listeners.remove(listener) && this.listeners.isEmpty()) {
         this.listeners = null;
      }

      return this;
   }

   protected void onContentsChanged() {
      if (this.listeners != null) {
         int i = 0;

         for (int m = this.listeners.size(); i < m; i++) {
            this.listeners.get(i).accept(this);
         }
      }
   }

   @Nonnull
   public FluidStack drain(int maxDrain, FluidAction action) {
      int drained = maxDrain;
      if (this.fluid.getAmount() < maxDrain) {
         drained = this.fluid.getAmount();
      }

      FluidStack stack = new FluidStack(this.fluid, drained);
      if (action.execute() && drained > 0) {
         this.fluid.shrink(drained);
         this.onContentsChanged();
      }

      return stack;
   }

   public FluidTank readFromNBT(CompoundTag nbt) {
      this.setFluid(FluidStack.loadFluidStackFromNBT(nbt));
      return this;
   }

   public CompoundTag writeToNBT(CompoundTag nbt) {
      if (this.getFluidAmount() <= 0) {
         return nbt;
      } else {
         NBTUtils.putString(
            nbt, "FluidName", ForgeRegistryWrapper.getFluidKey(this.fluid.getFluid()).toString(), ForgeRegistryWrapper.getFluidKey(Fluids.EMPTY).toString()
         );
         NBTUtils.putInt(nbt, "Amount", this.fluid.getAmount(), 0);
         if (this.fluid.getTag() != null) {
            NBTUtils.put(nbt, "Tag", this.fluid.getTag());
         }

         return nbt;
      }
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeBoolean(this.fluid.isEmpty());
      buffer.writeInt(this.capacity);
      if (!this.fluid.isEmpty()) {
         buffer.writeFluidStack(this.fluid);
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      if (buffer.readBoolean()) {
         this.setFluid(FluidStack.EMPTY);
         this.capacity = buffer.readInt();
      } else {
         this.capacity = buffer.readInt();
         this.setFluid(buffer.readFluidStack());
      }
   }
}
