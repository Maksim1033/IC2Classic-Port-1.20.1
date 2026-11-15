package ic2.core.block.transport.fluid.tiles;

import ic2.core.platform.registries.IC2Tiles;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class VoidPipeTileEntity extends PipeTileEntity implements IFluidHandler {
   public VoidPipeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.VOID_PIPE;
   }

   @Override
   public boolean canPushFluid(Direction dir) {
      return false;
   }

   @Override
   public boolean isFluidAcceptor() {
      return true;
   }

   public int getTanks() {
      return 1;
   }

   public FluidStack getFluidInTank(int tank) {
      return FluidStack.EMPTY;
   }

   public int getTankCapacity(int tank) {
      return 2147483647;
   }

   public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
      return true;
   }

   public int fill(FluidStack resource, FluidAction action) {
      return resource.getAmount();
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      return FluidStack.EMPTY;
   }

   public FluidStack drain(int maxDrain, FluidAction action) {
      return FluidStack.EMPTY;
   }
}
