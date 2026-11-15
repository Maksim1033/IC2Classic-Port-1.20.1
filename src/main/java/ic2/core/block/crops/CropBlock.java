package ic2.core.block.crops;

import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.IStateController;
import ic2.core.block.rendering.block.CropModel;
import ic2.core.item.block.CropItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CropBlock extends IC2ContainerBlock implements ICustomBlockModel, IStateController<CropTileEntity>, SimpleWaterloggedBlock, IRenderType {
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final IntegerProperty LIGHT = IC2Properties.LIGHT_0_15;
   public static final VoxelShape SHAPE = Block.box(3.200000047683716, 0.0, 3.200000047683716, 12.800000190734863, 11.2, 12.800000190734863);

   public CropBlock() {
      super(
         "crop_sticks",
         Properties.of().lightLevel(T -> (Integer)T.getValue(LIGHT)).noCollission().sound(SoundType.GRASS).strength(0.8F, 0.2F)
      );
      this.setHarvestTool(Tool.AXE);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(WATER, false)).setValue(LIGHT, 0));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, CropTileEntity tile) {
      tile.setState((BlockState)state.setValue(LIGHT, tile.getCropLight()));
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return CropRegistry.INSTANCE.getFarmland(worldIn.getBlockState(pos.below()).getBlock()) != null;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState state = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)this.defaultBlockState().setValue(WATER, state.getType() == Fluids.WATER);
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATER) ? Fluids.WATER.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
   }

   @Override
   public BlockItem createItem() {
      return new CropItem(this);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return IC2Tiles.CROP_STICK.create(pos, state);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{WATER}).add(new Property[]{LIGHT});
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return CropModel.INSTANCE;
   }
}
