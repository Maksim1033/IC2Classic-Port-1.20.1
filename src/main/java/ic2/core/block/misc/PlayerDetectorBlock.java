package ic2.core.block.misc;

import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.ICamouflageBlock;
import ic2.core.block.base.blocks.BaseActivityBlock;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.cables.CableBlock;
import ic2.core.block.misc.tiles.PlayerDetectorTileEntity;
import ic2.core.block.rendering.block.CamouflageModel;
import ic2.core.block.rendering.block.PlayerDetectorModel;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.features.providers.EmptyProvider;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.blocks.NonTranslucentBlockModel;
import ic2.core.utils.helpers.Tool;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public class PlayerDetectorBlock
   extends BaseActivityBlock<PlayerDetectorTileEntity>
   implements ICustomBlockModel,
   IDualLogged,
   ICamouflageBlock,
   PainterHelper.IPaintable {
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
   public static final IntegerProperty FOAMED = CableBlock.FOAMED;
   public static final VoxelShape[] SHAPES = new VoxelShape[]{
      Block.box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0),
      Block.box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0),
      Block.box(5.0, 5.0, 14.0, 11.0, 11.0, 16.0),
      Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 2.0),
      Block.box(14.0, 5.0, 5.0, 16.0, 11.0, 11.0),
      Block.box(0.0, 5.0, 5.0, 2.0, 11.0, 11.0)
   };

   public PlayerDetectorBlock() {
      super(
         "player_detector",
         Block.Properties.of().noOcclusion().requiresCorrectToolForDrops(),
         EmptyProvider.INSTANCE,
         IC2Tiles.PLAYER_DETECTOR
      );
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(LAVA, false)).setValue(WATER, false)).setValue(FOAMED, 0)
      );
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{WATER}).add(new Property[]{LAVA}).add(new Property[]{FOAMED});
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(new ICamouflageBlock.CamouflageWrapper(this));
   }

   @Override
   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if ((Boolean)state.getValue(WATER)) {
         world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      } else if ((Boolean)state.getValue(LAVA)) {
         world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
      }
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, PlayerDetectorTileEntity tile) {
      tile.setState(
         (BlockState)((BlockState)((BlockState)state.setValue(FACING, tile.getFacing())).setValue(ACTIVE, tile.isActive()))
            .setValue(FOAMED, Integer.valueOf(tile.foamed))
      );
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }

   @Override
   public List<BlockState> getModelStates() {
      return Collections.emptyList();
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState state = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(WATER, state.getType() == Fluids.WATER))
            .setValue(LAVA, state.getType() == Fluids.LAVA))
         .setValue(FACING, context.getClickedFace());
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return state.getValue(FOAMED) > 0 ? Shapes.block() : SHAPES[((Direction)state.getValue(FACING)).get3DDataValue()];
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
      if ((Integer)state.getValue(FOAMED) > 0) {
         return state;
      } else {
         return direction == ((Direction)state.getValue(FACING)).getOpposite() && !canSupportCenter(world, neighborPos, direction.getOpposite())
            ? Blocks.AIR.defaultBlockState()
            : state;
      }
   }

   @Override
   public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof PlayerDetectorTileEntity ? ((PlayerDetectorTileEntity)tile).setColor(dir, color) : false;
   }

   @Override
   public DyeColor getColor(BlockState state) {
      return null;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      switch (state.getValue(FOAMED)) {
         case 1:
            return new NonTranslucentBlockModel(
               (BlockState)IC2Blocks.CFOAM_WET.defaultBlockState().setValue(CFoamBlock.FOAM_TYPE, CFoamBlock.FoamType.CABLE), (IBlockModel)IC2Blocks.CFOAM_WET
            );
         case 2:
            return CamouflageModel.getInstance();
         default:
            return new PlayerDetectorModel(state, "misc", "player_detector_active", "player_detector_inactive");
      }
   }
}
