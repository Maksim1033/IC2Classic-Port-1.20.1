package ic2.core.fluid;

import java.util.function.Predicate;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class InsertionTank extends IC2Tank {
   public InsertionTank(int capacity) {
      super(capacity);
   }

   public InsertionTank(int capacity, Predicate<FluidStack> validator) {
      super(capacity, validator);
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      return FluidStack.EMPTY;
   }

   @Override
   public FluidStack drain(int maxDrain, FluidAction action) {
      return FluidStack.EMPTY;
   }

   public FluidStack drainInternally(int maxDrain, FluidAction action) {
      int drained = maxDrain;
      if (this.fluid.getAmount() < maxDrain) {
         drained = this.fluid.getAmount();
      }

      FluidStack stack = new FluidStack(this.fluid, drained);
      if (action.execute() && drained > 0) {
         this.fluid.shrink(drained);
      }

      if (drained > 0) {
         this.onContentsChanged();
      }

      return stack;
   }
}
