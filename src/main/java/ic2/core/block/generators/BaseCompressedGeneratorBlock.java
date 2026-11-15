package ic2.core.block.generators;

import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseCompressedGeneratorBlock extends BaseGeneratorBlock implements SimpleWaterloggedBlock {
   public static final AABB BOX = new AABB(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
   public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;

   public BaseCompressedGeneratorBlock(String blockName, IBlockDropProvider drop, String textureFolder, BlockEntityType<? extends BlockEntity> tile) {
      super(blockName, drop, ITextureProvider.toggleIC2(textureFolder), tile);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(WATER, false));
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return BOX;
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATER) ? Fluids.WATER.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState state = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)this.defaultBlockState().setValue(WATER, state.getType() == Fluids.WATER);
   }

   @Override
   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      super.neighborChanged(state, world, pos, block, fromPos, isMoving);
      if ((Boolean)state.getValue(WATER)) {
         world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      }
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{WATER});
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
      return SHAPE;
   }
}
