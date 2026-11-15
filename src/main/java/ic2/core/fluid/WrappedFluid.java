package ic2.core.fluid;

import ic2.core.inventory.base.INBTSavable;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;

public class WrappedFluid implements INBTSavable {
   FluidStack fluid;

   public WrappedFluid(FluidStack fluid) {
      this.fluid = fluid;
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      nbt.put("fluid", this.fluid.writeToNBT(new CompoundTag()));
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluid"));
   }

   public FluidStack getFluid() {
      return this.fluid;
   }
}
