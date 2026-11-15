package ic2.core.fluid;

import ic2.core.utils.helpers.FluidHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TransformHandler implements IFluidHandler {
   IFluidHandler handler;
   TagKey<Fluid> filter;
   Fluid targetFluid;

   public TransformHandler(IFluidHandler handler, TagKey<Fluid> filter, Fluid targetFluid) {
      this.handler = handler;
      this.filter = filter;
      this.targetFluid = targetFluid;
   }

   public int getTanks() {
      return this.handler.getTanks();
   }

   public FluidStack getFluidInTank(int tank) {
      return this.handler.getFluidInTank(tank);
   }

   public int getTankCapacity(int tank) {
      return this.handler.getTankCapacity(tank);
   }

   public boolean isFluidValid(int tank, FluidStack stack) {
      return stack.getFluid().is(this.filter);
   }

   public int fill(FluidStack resource, FluidAction action) {
      return this.handler.fill(FluidHelper.transform(resource, this.filter, this.targetFluid), action);
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      return this.handler.drain(FluidHelper.transform(resource, this.filter, this.targetFluid), action);
   }

   public FluidStack drain(int maxDrain, FluidAction action) {
      return this.handler.drain(maxDrain, action);
   }
}
