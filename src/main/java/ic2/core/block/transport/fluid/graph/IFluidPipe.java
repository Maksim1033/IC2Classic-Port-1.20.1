package ic2.core.block.transport.fluid.graph;

import ic2.api.tiles.IAnchorTile;
import ic2.api.util.ILocation;
import java.util.List;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IFluidPipe extends ILocation, IAnchorTile {
   boolean canPushFluid(Direction var1);

   boolean canReceiveFluid(Direction var1);

   default boolean isFluidAcceptor() {
      return false;
   }

   List<IFluidPipe.FluidOutput> getOutputs();

   Iterable<Direction> getEmitterSources();

   public static final class FluidOutput {
      final IFluidPipe source;
      final long position;
      final Direction dir;
      final IFluidHandler handler;
      final IntSupplier priority;
      final Predicate<Fluid> filter;

      public FluidOutput(IFluidPipe source, BlockPos position, Direction dir, IFluidHandler handler) {
         this(source, position, dir, handler, () -> 0, T -> true);
      }

      public FluidOutput(IFluidPipe source, BlockPos position, Direction dir, IFluidHandler handler, IntSupplier priority, Predicate<Fluid> filter) {
         this.source = source;
         this.position = position.asLong();
         this.dir = dir;
         this.handler = handler;
         this.priority = priority;
         this.filter = filter;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.dir, this.position);
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof IFluidPipe.FluidOutput output) ? false : output.dir == this.dir && output.position == this.position;
      }

      public int getPriority() {
         return this.priority.getAsInt();
      }

      public IFluidHandler getHandler() {
         return this.handler;
      }

      public boolean isFluidAllowed(Fluid fluid) {
         return this.filter.test(fluid);
      }
   }
}
