package ic2.core.fluid;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class ArrayFluidHandler implements IFluidHandler {
   IFluidHandler[] handler;
   IntList tankSlots = new IntArrayList();
   IntList tankIndexes = new IntArrayList();

   public ArrayFluidHandler(List<IFluidHandler> handler) {
      this(handler.toArray(IFluidHandler[]::new));
   }

   public ArrayFluidHandler(IFluidHandler... handler) {
      this.handler = handler;

      for (int i = 0; i < handler.length; i++) {
         IFluidHandler entry = handler[i];
         int j = 0;

         for (int m = entry.getTanks(); j < m; j++) {
            this.tankIndexes.add(i);
            this.tankSlots.add(j);
         }
      }
   }

   public int getTanks() {
      return this.tankIndexes.size();
   }

   public FluidStack getFluidInTank(int tank) {
      return this.handler[this.tankIndexes.getInt(tank)].getFluidInTank(this.tankSlots.getInt(tank));
   }

   public int getTankCapacity(int tank) {
      return this.handler[this.tankIndexes.getInt(tank)].getTankCapacity(this.tankSlots.getInt(tank));
   }

   public boolean isFluidValid(int tank, FluidStack stack) {
      return this.handler[this.tankIndexes.getInt(tank)].isFluidValid(this.tankSlots.getInt(tank), stack);
   }

   public int fill(FluidStack resource, FluidAction action) {
      int filled = 0;

      for (IFluidHandler iFluidHandler : this.handler) {
         filled += iFluidHandler.fill(new FluidStack(resource, resource.getAmount() - filled), action);
         if (filled >= resource.getAmount()) {
            return filled;
         }
      }

      return filled;
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      int drained = 0;

      for (IFluidHandler iFluidHandler : this.handler) {
         drained += iFluidHandler.drain(new FluidStack(resource, resource.getAmount() - drained), action).getAmount();
         if (drained >= resource.getAmount()) {
            return new FluidStack(resource, drained);
         }
      }

      return drained == 0 ? FluidStack.EMPTY : new FluidStack(resource, drained);
   }

   public FluidStack drain(int maxDrain, FluidAction action) {
      FluidStack stack = FluidStack.EMPTY;

      for (IFluidHandler iFluidHandler : this.handler) {
         if (stack.isEmpty()) {
            stack = iFluidHandler.drain(maxDrain, action);
         } else {
            stack.grow(iFluidHandler.drain(new FluidStack(stack, maxDrain - stack.getAmount()), action).getAmount());
         }

         if (stack.getAmount() >= maxDrain) {
            return stack;
         }
      }

      return stack;
   }
}
