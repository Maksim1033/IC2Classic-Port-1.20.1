package ic2.core.inventory.filter;

import ic2.core.fluid.WrapperFluidHandler;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidFilter implements IFilter {
   public static final IFilter EMPTY_CONTAINER = T -> T.getItem() == Items.MILK_BUCKET
         ? false
         : T.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent() && FluidUtil.getFluidContained(T).isEmpty();
   public static final IFilter ANY_FLUID = T -> FluidUtil.getFluidContained(T).isPresent();
   Set<Fluid> fluids;

   public static IFilter createFillFilter(Fluid fluid) {
      return new FluidFilter.FluidFillFilter(fluid);
   }

   public FluidFilter(Fluid... fluids) {
      this.fluids = new ObjectOpenHashSet(fluids);
   }

   @Override
   public boolean matches(ItemStack input) {
      return this.fluids.contains(FluidUtil.getFluidContained(input).orElse(FluidStack.EMPTY).getFluid());
   }

   public static class FluidFillFilter implements IFilter {
      FluidTank tank = new FluidTank(1000);

      public FluidFillFilter(Fluid fluid) {
         this.tank.setFluid(new FluidStack(fluid, 1000));
      }

      @Override
      public boolean matches(ItemStack input) {
         FluidActionResult result = FluidUtil.tryFillContainer(input, new WrapperFluidHandler(this.tank), 1000, null, true);
         return result.isSuccess() && !result.getResult().isEmpty();
      }
   }

   public static class FluidTankFillFilter implements IFilter {
      IFluidHandler handler;

      public FluidTankFillFilter(IFluidHandler handler) {
         this.handler = handler;
      }

      @Override
      public boolean matches(ItemStack input) {
         FluidActionResult result = FluidUtil.tryFillContainer(input, new WrapperFluidHandler(this.handler), 1000, null, true);
         return result.isSuccess() && !result.getResult().isEmpty();
      }
   }

   public static class FluidTankFilter implements IFilter {
      IFluidHandler handler;

      public FluidTankFilter(IFluidHandler handler) {
         this.handler = new WrapperFluidHandler.FillHandlerWrapper(handler);
      }

      @Override
      public boolean matches(ItemStack input) {
         FluidActionResult result = FluidUtil.tryEmptyContainer(input, this.handler, 1000, null, true);
         return result.isSuccess() && !result.getResult().isEmpty();
      }
   }
}
