package ic2.core.block.transport.item;

import com.google.common.base.Objects;
import ic2.api.blocks.PainterHelper;
import ic2.api.util.DirectionList;
import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.ICamouflageBlock;
import ic2.core.block.base.IStateController;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.cables.CableBlock;
import ic2.core.block.misc.CFoamBlock;
import ic2.core.block.rendering.block.tubes.TubeModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.item.tool.SprayerTool;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.blocks.NonTranslucentBlockModel;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.ColorUtils;
import java.util.function.Consumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public class TubeBlock
   extends IC2ContainerBlock
   implements ITubeBlock,
   ICustomBlockModel,
   ICamouflageBlock,
   IDualLogged,
   IStateController<TubeTileEntity>,
   PainterHelper.IPaintable {
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
   public static final IntegerProperty FOAMED = CableBlock.FOAMED;
   BlockEntityType<? extends BlockEntity> type;
   VoxelShape[] shapes = this.createShapes();
   String texture;

   public TubeBlock(String name, BlockEntityType<? extends BlockEntity> type, String texture) {
      super(
         name,
         Properties.of()
            .noOcclusion()
            .isRedstoneConductor((T, V, X) -> false)
            .strength(1.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
      );
      this.setHarvestTool(Tool.PICKAXE);
      this.type = type;
      this.texture = texture;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(WATER, false)).setValue(LAVA, false)).setValue(FOAMED, 0)
      );
   }

   @Override
   public int getHighlightColor(BlockState state) {
      return ColorUtils.RED;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean canRenderInLayer(BlockState state, RenderType type) {
      return (Integer)state.getValue(FOAMED) > 0 && Objects.equal(RenderType.translucent(), type) || Objects.equal(RenderType.cutout(), type);
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(new ICamouflageBlock.CamouflageWrapper(this));
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
      return useContext.getItemInHand().getItem() instanceof SprayerTool && (Integer)state.getValue(FOAMED) <= 0;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{WATER}).add(new Property[]{LAVA}).add(new Property[]{FOAMED});
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.type.create(pos, state);
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }

   @Override
   public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
      return IDualLogged.super.canPlaceLiquid(worldIn, pos, state, fluidIn) && (Integer)state.getValue(FOAMED) <= 0;
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, TubeTileEntity tile) {
      tile.setState(
         (BlockState)((BlockState)((BlockState)state.setValue(FOAMED, Integer.valueOf(tile.foamed)))
               .setValue(WATER, (Boolean)state.getValue(WATER) && tile.foamed == 0))
            .setValue(LAVA, (Boolean)state.getValue(LAVA) && tile.foamed == 0)
      );
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      if ((Integer)state.getValue(FOAMED) > 0) {
         return Shapes.block();
      } else {
         return worldIn.getBlockEntity(pos) instanceof TubeTileEntity tube ? this.shapes[tube.getConnectivity()] : this.shapes[0];
      }
   }

   @Override
   public VoxelShape[] getRealShapes(BlockState state, BlockGetter reader, BlockPos pos) {
      return reader.getBlockEntity(pos) instanceof TubeTileEntity tube
         ? new VoxelShape[]{this.shapes[tube.getValidDirections()], Shapes.join(this.shapes[0], this.shapes[tube.getAnchors()], BooleanOp.ONLY_SECOND)}
         : new VoxelShape[]{this.shapes[0], this.shapes[0]};
   }

   @Override
   public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof TubeTileEntity ? ((TubeTileEntity)tile).setColor(dir, color) : false;
   }

   @Override
   public DyeColor getColor(BlockState state) {
      return null;
   }

   @Override
   public DyeColor getBlockColor(BlockState state) {
      return null;
   }

   @Override
   public float getRadius(BlockState state) {
      return 3.0F;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("transport/tubes").get(this.texture);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState state = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)this.defaultBlockState().setValue(WATER, state.getType() == Fluids.WATER);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return (BaseModel)(state.getValue(FOAMED) == 1
         ? new NonTranslucentBlockModel(
            (BlockState)IC2Blocks.CFOAM_WET.defaultBlockState().setValue(CFoamBlock.FOAM_TYPE, CFoamBlock.FoamType.CABLE), (IBlockModel)IC2Blocks.CFOAM_WET
         )
         : new TubeModel(this, state, (Integer)state.getValue(FOAMED) > 0));
   }

   VoxelShape[] createShapes() {
      VoxelShape[] shapes = this.createSubShapes();
      VoxelShape[] results = new VoxelShape[64];

      for (int j = 0; j < 64; j++) {
         DirectionList list = DirectionList.ofNumber(j);
         VoxelShape[] entry = new VoxelShape[list.size()];
         int index = 0;

         for (Direction dir : list) {
            entry[index++] = shapes[dir.get3DDataValue()];
         }

         results[j] = Shapes.or(shapes[6], entry);
      }

      return results;
   }

   VoxelShape[] createSubShapes() {
      VoxelShape[] result = new VoxelShape[7];
      float minValue = 8.0F - this.getRadius(this.defaultBlockState());
      float maxValue = 8.0F + this.getRadius(this.defaultBlockState());
      result[0] = Block.box((double)minValue, 0.0, (double)minValue, (double)maxValue, (double)minValue, (double)maxValue);
      result[1] = Block.box((double)minValue, (double)maxValue, (double)minValue, (double)maxValue, 16.0, (double)maxValue);
      result[2] = Block.box((double)minValue, (double)minValue, 0.0, (double)maxValue, (double)maxValue, (double)minValue);
      result[3] = Block.box((double)minValue, (double)minValue, (double)maxValue, (double)maxValue, (double)maxValue, 16.0);
      result[4] = Block.box(0.0, (double)minValue, (double)minValue, (double)minValue, (double)maxValue, (double)maxValue);
      result[5] = Block.box((double)maxValue, (double)minValue, (double)minValue, 16.0, (double)maxValue, (double)maxValue);
      result[6] = Block.box((double)minValue, (double)minValue, (double)minValue, (double)maxValue, (double)maxValue, (double)maxValue);
      return result;
   }
}
