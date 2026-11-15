package ic2.core.block.personal.trade;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.fluid.IC2Tank;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidTrade extends Trade {
   public IC2Tank offered = new IC2Tank(128000);

   @Override
   public void write(IOutputBuffer buffer) {
      super.write(buffer);
      buffer.writeFluidStack(this.offered.getFluid());
   }

   @Override
   public void read(IInputBuffer buffer) {
      super.read(buffer);
      this.offered.setFluid(buffer.readFluidStack());
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      super.save(nbt);
      NBTUtils.put(nbt, "fluid", this.offered.writeToNBT(new CompoundTag()));
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt);
      this.offered.readFromNBT(nbt.getCompound("fluid"));
   }

   public int getCount(IFluidHandler handler) {
      return this.offered.isEmpty()
         ? 0
         : handler.drain(new FluidStack(this.offered.getFluid(), 2147483647), FluidAction.SIMULATE).getAmount() / this.offered.getFluidAmount();
   }

   public boolean hasStock(IFluidHandler extract, IFluidHandler insert, FluidAction action) {
      if (this.offered.isEmpty()) {
         return false;
      } else {
         FluidStack stack = this.infinite ? this.offered.getFluid() : extract.drain(this.offered.getFluid(), action);
         return stack.getAmount() < this.offered.getFluidAmount() ? false : insert.fill(stack, action) >= this.offered.getFluidAmount();
      }
   }
}
