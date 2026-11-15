package ic2.core.fluid;

import javax.annotation.Nonnull;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class WrapperFluidHandler extends FluidTank {
   public WrapperFluidHandler(IFluidHandler handler) {
      super(handler.getTankCapacity(0));
      this.setFluid(handler.drain(2147483647, FluidAction.SIMULATE));
   }

   public static class DrainHandlerWrapper implements IFluidHandler {
      IFluidHandler handler;

      public DrainHandlerWrapper(IFluidHandler handler) {
         this.handler = handler;
      }

      public int getTanks() {
         return this.handler.getTanks();
      }

      @Nonnull
      public FluidStack getFluidInTank(int tank) {
         return this.handler.getFluidInTank(tank);
      }

      public int getTankCapacity(int tank) {
         return this.handler.getTankCapacity(tank);
      }

      public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
         return this.handler.isFluidValid(tank, stack);
      }

      public int fill(FluidStack resource, FluidAction action) {
         return 0;
      }

      @Nonnull
      public FluidStack drain(FluidStack resource, FluidAction action) {
         return this.handler.drain(resource, action);
      }

      @Nonnull
      public FluidStack drain(int maxDrain, FluidAction action) {
         return this.handler.drain(maxDrain, action);
      }
   }

   public static class FillHandlerWrapper implements IFluidHandler {
      IFluidHandler handler;

      public FillHandlerWrapper(IFluidHandler handler) {
         this.handler = handler;
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
         return this.handler.isFluidValid(tank, stack);
      }

      public int fill(FluidStack resource, FluidAction action) {
         return this.handler.fill(resource, FluidAction.SIMULATE);
      }

      public FluidStack drain(FluidStack resource, FluidAction action) {
         return this.handler.drain(resource, FluidAction.SIMULATE);
      }

      public FluidStack drain(int maxDrain, FluidAction action) {
         return this.handler.drain(maxDrain, FluidAction.SIMULATE);
      }
   }
}
