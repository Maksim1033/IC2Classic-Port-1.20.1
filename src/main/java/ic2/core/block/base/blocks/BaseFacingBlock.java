package ic2.core.block.base.blocks;

import ic2.core.IC2;
import ic2.core.block.base.IStateController;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IRotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class BaseFacingBlock<T extends BaseTileEntity> extends BaseTexturedBlock implements IStateController<T>, IRotatableBlock, IBlockModel {
   public static final DirectionProperty FACING = IC2Properties.ALL_FACINGS;

   public BaseFacingBlock(String blockName, Properties properties, ITextureProvider provider, BlockEntityType<? extends BlockEntity> tile) {
      super(blockName, properties, provider, tile);
      this.setDefaultState();
   }

   protected void setDefaultState() {
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, this.getFacing(context));
   }

   protected Direction getFacing(BlockPlaceContext context) {
      return context.getHorizontalDirection().getOpposite();
   }

   public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (!IC2.PLATFORM.isRendering()) {
         if (worldIn.getBlockEntity(pos) instanceof BaseTileEntity base) {
            base.lock();
            base.setFacing((Direction)state.getValue(FACING));
            if (stack.hasCustomHoverName()) {
               base.setCustomName(stack.getHoverName());
            }

            this.setPlaceData(base, state, placer, stack);
            base.unlock();
         }
      }
   }

   protected void setPlaceData(BaseTileEntity tile, BlockState state, LivingEntity placer, ItemStack stack) {
   }

   @Override
   public boolean hasRotation(BlockState state) {
      return true;
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return state.hasProperty(FACING) ? (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING))) : state;
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.hasProperty(FACING) ? (BlockState)state.setValue(FACING, mirror.mirror((Direction)state.getValue(FACING))) : state;
   }

   @Override
   public Direction getRotation(BlockState state) {
      return (Direction)state.getValue(FACING);
   }

   @Override
   public void onStateUpdate(Level world, BlockPos pos, BlockState state, T tile) {
      tile.withState(FACING, tile.getFacing());
   }
}
