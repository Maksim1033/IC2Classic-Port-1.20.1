package ic2.core.block.base.misc;

import ic2.core.platform.registries.IC2Properties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface ILavaLoggable extends BucketPickup, LiquidBlockContainer {
   default boolean canPlaceLiquid(BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
      return !(Boolean)state.getValue(IC2Properties.LAVA_LOGGED) && fluid == Fluids.LAVA;
   }

   default boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluid) {
      if (!(Boolean)state.getValue(IC2Properties.LAVA_LOGGED) && fluid.getType() == Fluids.LAVA) {
         if (!world.isClientSide()) {
            world.setBlock(pos, (BlockState)state.setValue(IC2Properties.LAVA_LOGGED, Boolean.TRUE), 3);
            world.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(world));
         }

         return true;
      } else {
         return false;
      }
   }

   default Fluid takeLiquid(LevelAccessor world, BlockPos pos, BlockState state) {
      if ((Boolean)state.getValue(IC2Properties.LAVA_LOGGED)) {
         world.setBlock(pos, (BlockState)state.setValue(IC2Properties.LAVA_LOGGED, Boolean.FALSE), 3);
         return Fluids.LAVA;
      } else {
         return Fluids.EMPTY;
      }
   }
}
