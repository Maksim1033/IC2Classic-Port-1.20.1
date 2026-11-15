package ic2.core.block.cables.luminator;

import ic2.core.block.base.IStateController;
import ic2.core.block.base.blocks.BaseActivityBlock;
import ic2.core.block.cables.CableBlock;
import ic2.core.block.rendering.block.LuminatorModel;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.ICamouflageRenderType;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.features.providers.EmptyProvider;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags.Blocks;

public class LuminatorBlock
   extends BaseActivityBlock<LuminatorTileEntity>
   implements IStateController<LuminatorTileEntity>,
   ICustomBlockModel,
   ICamouflageRenderType,
   SimpleWaterloggedBlock {
   public static final VoxelShape[] FULL_SHAPES = new VoxelShape[]{
      Block.box(0.16, 15.0, 0.16, 15.84, 16.0, 15.84),
      Block.box(0.16, 0.0, 0.16, 15.84, 1.0, 15.84),
      Block.box(0.16, 0.16, 15.0, 15.84, 15.84, 16.0),
      Block.box(0.16, 0.16, 0.0, 15.84, 15.84, 1.0),
      Block.box(15.0, 0.16, 0.16, 16.0, 15.84, 15.84),
      Block.box(0.0, 0.16, 0.16, 1.0, 15.84, 15.84)
   };
   public static final VoxelShape[] EMPTY_SHAPE = new VoxelShape[]{
      Block.box(4.0, 13.0, 4.0, 12.0, 16.0, 12.0),
      Block.box(0.16, 0.0, 0.16, 15.84, 1.0, 15.84),
      Block.box(6.0, 3.0, 14.0, 10.0, 13.0, 16.0),
      Block.box(6.0, 3.0, 0.0, 10.0, 13.0, 2.0),
      Block.box(14.0, 3.0, 6.0, 16.0, 13.0, 10.0),
      Block.box(0.0, 3.0, 6.0, 2.0, 13.0, 10.0)
   };
   public static final IntegerProperty LIGHT = IC2Properties.LIGHT_0_15;
   public static final BooleanProperty BIG = BooleanProperty.create("big");
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final Properties BASE_LUMINATOR = Properties.of()
      .sound(SoundType.GLASS)
      .lightLevel(T -> (Integer)T.getValue(LIGHT))
      .isRedstoneConductor((x, y, z) -> false)
      .strength(0.3F, 0.5F)
      .dynamicShape()
      .noOcclusion();

   public LuminatorBlock(String blockName, BlockEntityType<?> type) {
      super(blockName, BASE_LUMINATOR, EmptyProvider.INSTANCE, (BlockEntityType<? extends BlockEntity>)type);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(ACTIVE, true)).setValue(LIGHT, 0))
                  .setValue(BIG, false))
               .setValue(FACING, Direction.NORTH))
            .setValue(WATER, false)
      );
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState state = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getClickedFace()))
            .setValue(BIG, isSpecialSupporter(context.getLevel(), context.getClickedPos().relative(context.getClickedFace().getOpposite()))))
         .setValue(WATER, state.getType() == Fluids.WATER);
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return isGlass(worldIn, pos) ? Shapes.block() : (state.getValue(BIG) ? FULL_SHAPES : EMPTY_SHAPE)[((Direction)state.getValue(FACING)).get3DDataValue()];
   }

   @Override
   public List<BlockState> getModelStates() {
      return ObjectLists.emptyList();
   }

   public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof LuminatorTileEntity) {
         BlockState glass = ((LuminatorTileEntity)tile).getGlassState();
         if (glass != null) {
            try {
               return glass.getExplosionResistance(world, pos, explosion);
            } catch (Exception var8) {
            }
         }
      }

      return super.getExplosionResistance(state, world, pos, explosion);
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, LuminatorTileEntity tile) {
      tile.setState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(ACTIVE, tile.isActive())).setValue(FACING, tile.getFacing()))
                  .setValue(BIG, isSpecialSupporter(world, pos.relative(tile.getFacing().getOpposite())) || tile.getGlassState() != null))
               .setValue(LIGHT, tile.getLightLevel()))
            .setValue(WATER, (Boolean)state.getValue(WATER) && tile.getGlassState() == null)
      );
      world.getChunkSource().getLightEngine().checkBlock(pos);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ACTIVE}).add(new Property[]{BIG}).add(new Property[]{FACING}).add(new Property[]{LIGHT}).add(new Property[]{WATER});
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATER) ? Fluids.WATER.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new LuminatorModel(
         state, IC2Textures.getMappedEntriesBlockIC2("electric/luminator").get(state.getValue(ACTIVE) ? "luminator_on" : "luminator_off")
      );
   }

   @Override
   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, worldIn, pos) && !isGlass(worldIn, pos)) {
         worldIn.removeBlock(pos, isMoving);
         Block.popResource(worldIn, pos, new ItemStack(this));
      }
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return isSupporterBlock(worldIn, pos.relative(((Direction)state.getValue(FACING)).getOpposite()));
   }

   public static boolean isGlass(BlockGetter world, BlockPos pos) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof LuminatorTileEntity && ((LuminatorTileEntity)tile).getGlassState() != null;
   }

   public static boolean isSupporterBlock(LevelReader world, BlockPos pos) {
      return !world.isEmptyBlock(pos) && (world.getBlockState(pos).isRedstoneConductor(world, pos) || isSpecialSupporter(world, pos));
   }

   public static boolean isSpecialSupporter(LevelReader world, BlockPos pos) {
      if (world.isEmptyBlock(pos)) {
         return false;
      } else {
         BlockState state = world.getBlockState(pos);
         Block block = state.getBlock();
         return state.is(Blocks.GLASS)
            || state.is(BlockTags.FENCES)
            || block instanceof CableBlock
            || block == IC2Blocks.REINFORCED_CLEAR_GLASS
            || block == IC2Blocks.REINFORCED_GLASS
            || block instanceof LuminatorBlock && isGlass(world, pos);
      }
   }
}
