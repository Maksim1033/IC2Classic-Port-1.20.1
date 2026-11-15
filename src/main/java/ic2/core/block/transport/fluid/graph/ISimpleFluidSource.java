package ic2.core.block.transport.fluid.graph;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.core.Direction;

public interface ISimpleFluidSource extends IFluidPipe {
   @Override
   default boolean hasAnchor(Direction side) {
      return false;
   }

   @Override
   default boolean addAnchor(Direction side) {
      return false;
   }

   @Override
   default boolean removeAnchor(Direction side) {
      return false;
   }

   @Override
   default boolean canPushFluid(Direction dir) {
      return true;
   }

   @Override
   default boolean canReceiveFluid(Direction dir) {
      return true;
   }

   @Override
   default List<IFluidPipe.FluidOutput> getOutputs() {
      return ObjectLists.emptyList();
   }

   @Override
   default Iterable<Direction> getEmitterSources() {
      return ObjectLists.singleton(null);
   }
}
