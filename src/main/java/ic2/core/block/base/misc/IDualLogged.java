package ic2.core.block.base.misc;

import ic2.core.platform.registries.IC2Properties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface IDualLogged extends ILavaLoggable, SimpleWaterloggedBlock {
   @Override
   default boolean canPlaceLiquid(BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
      return fluid == Fluids.LAVA && !this.canBeLavaLogged(state)
         ? false
         : !(Boolean)state.getValue(IC2Properties.LAVA_LOGGED)
            && !(Boolean)state.getValue(BlockStateProperties.WATERLOGGED)
            && (fluid == Fluids.LAVA || fluid == Fluids.WATER);
   }

   default boolean canBeLavaLogged(BlockState state) {
      return true;
   }

   @Override
   default boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluid) {
      if (this.canPlaceLiquid(world, pos, state, fluid.getType())) {
         if (!world.isClientSide()) {
            world.setBlock(
               pos, (BlockState)state.setValue(fluid.getType() == Fluids.LAVA ? IC2Properties.LAVA_LOGGED : BlockStateProperties.WATERLOGGED, true), 3
            );
            world.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(world));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   default Fluid takeLiquid(LevelAccessor world, BlockPos pos, BlockState state) {
      if (this.canBeLavaLogged(state) && (Boolean)state.getValue(IC2Properties.LAVA_LOGGED)) {
         world.setBlock(pos, (BlockState)state.setValue(IC2Properties.LAVA_LOGGED, false), 3);
         return Fluids.LAVA;
      } else if ((Boolean)state.getValue(BlockStateProperties.WATERLOGGED)) {
         world.setBlock(pos, (BlockState)state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
         return Fluids.WATER;
      } else {
         return Fluids.EMPTY;
      }
   }

   default ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
      if ((Boolean)state.getValue(BlockStateProperties.WATERLOGGED)) {
         level.setBlock(pos, (BlockState)state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE), 3);
         if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
         }

         return new ItemStack(Items.WATER_BUCKET);
      } else if (this.canBeLavaLogged(state) && (Boolean)state.getValue(IC2Properties.LAVA_LOGGED)) {
         level.setBlock(pos, (BlockState)state.setValue(IC2Properties.LAVA_LOGGED, Boolean.FALSE), 3);
         if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
         }

         return new ItemStack(Items.LAVA_BUCKET);
      } else {
         return ItemStack.EMPTY;
      }
   }

   static FluidState getFluidState(BlockState state) {
      return state.getValue(BlockStateProperties.WATERLOGGED)
         ? Fluids.WATER.defaultFluidState()
         : (state.getValue(IC2Properties.LAVA_LOGGED) ? Fluids.LAVA.defaultFluidState() : Fluids.EMPTY.defaultFluidState());
   }
}
