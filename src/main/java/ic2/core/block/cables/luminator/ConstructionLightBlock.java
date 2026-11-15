package ic2.core.block.cables.luminator;

import ic2.core.IC2;
import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.IStateController;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.rendering.block.ConstructionLightModel;
import ic2.core.item.block.ConstructionLightItemBlock;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConstructionLightBlock extends IC2ContainerBlock implements ICustomBlockModel, IStateController<ConstructionLightTileEntity>, IDualLogged {
   public static final DirectionProperty FACING = IC2Properties.HORIZONTAL_FACINGS;
   public static final BooleanProperty TOP = BooleanProperty.create("top");
   public static final BooleanProperty ACTIVE = IC2Properties.ACTIVE;
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
   public static final VoxelShape BOTTOM_MAIN = Shapes.or(
         Block.box(7.0, 0.0, 0.0, 9.0, 1.0, 16.0), new VoxelShape[]{Block.box(0.0, 0.0, 7.0, 16.0, 1.0, 9.0), Block.box(7.0, 0.0, 7.0, 9.0, 27.0, 9.0)}
      )
      .optimize();
   public static final VoxelShape[] BOTTOM_SHAPES = new VoxelShape[]{
      Shapes.or(BOTTOM_MAIN, new VoxelShape[]{Block.box(7.0, 25.0, 5.0, 9.0, 27.0, 7.0), Block.box(5.0, 24.0, 2.0, 11.0, 28.0, 5.0)}).optimize(),
      Shapes.or(BOTTOM_MAIN, new VoxelShape[]{Block.box(8.0, 25.0, 7.0, 12.0, 27.0, 9.0), Block.box(11.0, 24.0, 5.0, 14.0, 28.0, 11.0)}).optimize(),
      Shapes.or(BOTTOM_MAIN, new VoxelShape[]{Block.box(7.0, 25.0, 8.0, 9.0, 27.0, 12.0), Block.box(5.0, 24.0, 11.0, 11.0, 28.0, 14.0)}).optimize(),
      Shapes.or(BOTTOM_MAIN, new VoxelShape[]{Block.box(5.0, 25.0, 7.0, 7.0, 27.0, 9.0), Block.box(2.0, 24.0, 5.0, 5.0, 28.0, 11.0)}).optimize()
   };
   public static final VoxelShape TOP_MAIN = Shapes.or(
         Block.box(7.0, -16.0, 0.0, 9.0, -15.0, 16.0),
         new VoxelShape[]{Block.box(0.0, -16.0, 7.0, 0.0, -15.0, 9.0), Block.box(7.0, -16.0, 7.0, 9.0, 11.0, 9.0)}
      )
      .optimize();
   public static final VoxelShape[] TOP_SHAPES = new VoxelShape[]{
      Shapes.or(TOP_MAIN, new VoxelShape[]{Block.box(7.0, 9.0, 5.0, 9.0, 11.0, 7.0), Block.box(5.0, 8.0, 2.0, 11.0, 12.0, 5.0)}).optimize(),
      Shapes.or(TOP_MAIN, new VoxelShape[]{Block.box(8.0, 9.0, 7.0, 12.0, 11.0, 9.0), Block.box(11.0, 8.0, 5.0, 14.0, 12.0, 11.0)}).optimize(),
      Shapes.or(TOP_MAIN, new VoxelShape[]{Block.box(7.0, 9.0, 8.0, 9.0, 11.0, 12.0), Block.box(5.0, 8.0, 11.0, 11.0, 12.0, 14.0)}).optimize(),
      Shapes.or(TOP_MAIN, new VoxelShape[]{Block.box(5.0, 9.0, 7.0, 7.0, 11.0, 9.0), Block.box(2.0, 8.0, 5.0, 5.0, 12.0, 11.0)}).optimize()
   };

   public ConstructionLightBlock() {
      super("construction_light", Properties.of().lightLevel(T -> T.getValue(ACTIVE) ? 15 : 0));
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(TOP, false)).setValue(ACTIVE, false)).setValue(WATER, false))
            .setValue(LAVA, false)
      );
      this.setHarvestTool(Tool.PICKAXE);
   }

   @Override
   public BlockItem createItem() {
      return new ConstructionLightItemBlock(this);
   }

   public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         worldIn.removeBlock(state.getValue(TOP) ? pos.below() : pos.above(), isMoving);
         if ((Boolean)state.getValue(TOP)) {
            worldIn.removeBlockEntity(pos);
         }
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING}).add(new Property[]{TOP}).add(new Property[]{ACTIVE}).add(new Property[]{WATER}).add(new Property[]{LAVA});
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return (state.getValue(TOP) ? TOP_SHAPES : BOTTOM_SHAPES)[((Direction)state.getValue(FACING)).get2DDataValue()];
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, ConstructionLightTileEntity tile) {
      if (tile.setState((BlockState)((BlockState)state.setValue(ACTIVE, tile.isActive())).setValue(FACING, tile.getFacing()))) {
         world.setBlockAndUpdate(pos.below(), (BlockState)world.getBlockState(pos.below()).setValue(FACING, tile.getFacing()));
      }
   }

   public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (!IC2.PLATFORM.isRendering()) {
         if (worldIn.getBlockEntity(pos.above()) instanceof BaseTileEntity base) {
            base.lock();
            base.setFacing((Direction)state.getValue(FACING));
            if (stack.hasCustomHoverName()) {
               base.setCustomName(stack.getHoverName());
            }

            base.unlock();
         }
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new ConstructionLightModel(state);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return state.getValue(TOP) ? IC2Tiles.CONSTRUCTION_LIGHT.create(pos, state) : null;
   }
}
