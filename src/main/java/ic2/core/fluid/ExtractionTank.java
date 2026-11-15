package ic2.core.fluid;

import java.util.function.Predicate;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ExtractionTank extends IC2Tank {
   public static Predicate<FluidStack> CACHE = T -> false;

   public ExtractionTank(int capacity) {
      super(capacity, CACHE);
   }

   public int fillInternal(FluidStack resource, FluidAction action) {
      if (resource.isEmpty()) {
         return 0;
      } else if (action.simulate()) {
         if (this.fluid.isEmpty()) {
            return Math.min(this.capacity, resource.getAmount());
         } else {
            return !this.fluid.isFluidEqual(resource) ? 0 : Math.min(this.capacity - this.fluid.getAmount(), resource.getAmount());
         }
      } else if (this.fluid.isEmpty()) {
         this.fluid = new FluidStack(resource, Math.min(this.capacity, resource.getAmount()));
         this.onContentsChanged();
         return this.fluid.getAmount();
      } else if (!this.fluid.isFluidEqual(resource)) {
         return 0;
      } else {
         int filled = this.capacity - this.fluid.getAmount();
         if (resource.getAmount() < filled) {
            this.fluid.grow(resource.getAmount());
            filled = resource.getAmount();
         } else {
            this.fluid.setAmount(this.capacity);
         }

         if (filled > 0) {
            this.onContentsChanged();
         }

         return filled;
      }
   }

   public FluidTank setValidator(Predicate<FluidStack> validator) {
      return this;
   }
}
