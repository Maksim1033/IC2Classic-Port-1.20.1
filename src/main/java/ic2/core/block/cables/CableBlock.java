package ic2.core.block.cables;

import ic2.api.blocks.PainterHelper;
import ic2.api.items.ICutterItem;
import ic2.api.util.DirectionList;
import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.ICamouflageBlock;
import ic2.core.block.base.IStateController;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.misc.CFoamBlock;
import ic2.core.block.rendering.block.CamouflageModel;
import ic2.core.block.rendering.block.tubes.CableModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.item.tool.SprayerTool;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.blocks.NonTranslucentBlockModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.ColorUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public abstract class CableBlock
   extends IC2ContainerBlock
   implements ICustomBlockModel,
   IStateController<CableTileEntity>,
   IDualLogged,
   ICamouflageBlock,
   ITubeBlock,
   PainterHelper.IPaintable {
   public static final BooleanProperty ACTIVE = IC2Properties.ACTIVE;
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
   public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);
   public static final IntegerProperty FOAMED = IntegerProperty.create("foamed", 0, 2);
   public static final IntegerProperty[] INSULATION = new IntegerProperty[]{
      IntegerProperty.create("insulation", 0, 1),
      IntegerProperty.create("insulation", 0, 2),
      IntegerProperty.create("insulation", 0, 3),
      IntegerProperty.create("insulation", 0, 4),
      IntegerProperty.create("insulation", 0, 5)
   };
   final VoxelShape[][] shapes;
   CableBlock.CableInstance instance;
   BlockEntityType<? extends CableTileEntity> cableTile;

   protected CableBlock(String blockName, BlockEntityType<? extends CableTileEntity> cableTile) {
      super(blockName, Properties.of().noOcclusion().isRedstoneConductor((T, V, X) -> false).sound(SoundType.WOOL).strength(0.2F, 1.0F));
      this.setHarvestTool(Tool.AXE);
      this.shapes = this.getCable().createShapes();
      this.cableTile = cableTile;
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(WATER, false)).setValue(LAVA, false));
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
      return useContext.getItemInHand().getItem() instanceof SprayerTool && (Integer)state.getValue(FOAMED) <= 0;
   }

   public static CableBlock createBlock(String name, final CableBlock.CableInstance cable, BlockEntityType<? extends CableTileEntity> creator) {
      return new CableBlock(name, creator) {
         @Override
         public CableBlock.CableInstance createInstance() {
            return cable;
         }
      };
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(new ICamouflageBlock.CamouflageWrapper(this));
   }

   @Override
   public int getHighlightColor(BlockState state) {
      return ColorUtils.rgb(175, 175, 0);
   }

   public BlockState withInsulation(int level) {
      return this.hasInsulation() ? (BlockState)this.defaultBlockState().setValue(this.getInsulation(), level) : this.defaultBlockState();
   }

   public BlockState withActivity(boolean active) {
      return this.instance.hasActivity ? (BlockState)this.defaultBlockState().setValue(ACTIVE, active) : this.defaultBlockState();
   }

   protected CableBlock.CableInstance getCable() {
      return this.instance;
   }

   public abstract CableBlock.CableInstance createInstance();

   public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
      return true;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this).disableCreative();
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.cableTile.create(pos, state);
   }

   @Override
   public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof CableTileEntity ? ((CableTileEntity)tile).setColor(dir, color) : false;
   }

   @Override
   public DyeColor getColor(BlockState state) {
      return this.getBlockColor(state);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState state = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)this.defaultBlockState().setValue(WATER, state.getType() == Fluids.WATER)).setValue(LAVA, state.getType() == Fluids.LAVA);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
      int insulation = this.getInsulation(state);
      CableBlock.CableInstance instance = this.getCable();
      return insulation == 0 && !instance.usesUninsulatedColor
         ? IC2Textures.getMappedEntriesBlockIC2(instance.folder).get(instance.textures.get(instance.hasActivity && state.getValue(ACTIVE) ? 1 : 0))
         : IC2Textures.getMappedEntriesBlockIC2(instance.getTexture(insulation)).get(this.getCable().usesColor ? this.getTextureColor(state) : "blank");
   }

   @Override
   public List<BlockState> getCustomStates() {
      return this.getStateDefinition().getPossibleStates();
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
            return new CableModel(this, state);
      }
   }

   public String getTextureColor(BlockState state) {
      int index = (Integer)state.getValue(COLOR);
      return index == 0 ? "blank" : DyeColor.byId(index - 1).getName();
   }

   @Override
   public float getRadius(BlockState state) {
      return this.getCable().widths[this.getInsulation(state)];
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      if ((Integer)state.getValue(FOAMED) > 0) {
         return Shapes.block();
      } else {
         return worldIn.getBlockEntity(pos) instanceof CableTileEntity cable
            ? this.shapes[this.getInsulation(state)][cable.getConnectivity()]
            : this.shapes[0][0];
      }
   }

   @Override
   public VoxelShape[] getRealShapes(BlockState state, BlockGetter reader, BlockPos pos) {
      if (reader.getBlockEntity(pos) instanceof CableTileEntity cable) {
         VoxelShape[] array = this.shapes[this.getInsulation(state)];
         return new VoxelShape[]{array[cable.getConnections()], Shapes.join(array[0], array[cable.getAnchors()], BooleanOp.ONLY_SECOND)};
      } else {
         return new VoxelShape[]{this.shapes[0][0], Shapes.empty()};
      }
   }

   @Override
   public DyeColor getBlockColor(BlockState state) {
      if (!state.hasProperty(COLOR)) {
         return null;
      } else {
         int index = (Integer)state.getValue(COLOR);
         return index == 0 ? null : DyeColor.byId(index - 1);
      }
   }

   public final boolean hasInsulation() {
      return this.getCable().insulation > 0;
   }

   public final IntegerProperty getInsulation() {
      return INSULATION[this.getCable().insulation - 1];
   }

   public int getInsulation(BlockState state) {
      return this.hasInsulation() ? (Integer)state.getValue(this.getInsulation()) : 0;
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }

   @Override
   public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
      return IDualLogged.super.canPlaceLiquid(worldIn, pos, state, fluidIn) && (Integer)state.getValue(FOAMED) <= 0;
   }

   @Override
   public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluid) {
      if (IDualLogged.super.placeLiquid(world, pos, state, fluid)) {
         if (fluid.getType() == Fluids.LAVA) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof CableTileEntity && ((CableTileEntity)tile).insulation > 0) {
               ((CableTileEntity)tile).removeInsulation();
               world.playSound(
                  null,
                  pos,
                  SoundEvents.FIRE_EXTINGUISH,
                  SoundSource.BLOCKS,
                  0.5F,
                  2.6F + (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.8F
               );
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      this.instance = this.createInstance();
      if (this.getCable().hasActivity) {
         builder.add(new Property[]{ACTIVE});
      }

      if (this.getCable().usesColor) {
         builder.add(new Property[]{COLOR});
      }

      if (this.hasInsulation()) {
         builder.add(new Property[]{this.getInsulation()});
      }

      builder.add(new Property[]{WATER}).add(new Property[]{LAVA}).add(new Property[]{FOAMED});
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, CableTileEntity tile) {
      if (this.getCable().hasActivity) {
         state = (BlockState)state.setValue(ACTIVE, tile.isActive());
      }

      if (this.getCable().usesColor) {
         state = (BlockState)state.setValue(COLOR, tile.cableColor);
      }

      if (this.hasInsulation()) {
         state = (BlockState)state.setValue(this.getInsulation(), Integer.valueOf(tile.insulation));
      }

      if (tile.foamed > 0) {
         state = (BlockState)((BlockState)state.setValue(WATER, false)).setValue(LAVA, false);
      }

      tile.setState((BlockState)state.setValue(FOAMED, Integer.valueOf(tile.foamed)));
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack stack, RandomSource rand, BlockEntity tile, boolean wrench) {
      return this.instance.drops.apply(this.getInsulation(state));
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof CableTileEntity && ((CableTileEntity)tile).foamed == 2 && target instanceof BlockHitResult) {
         BlockState rayState = ((CableTileEntity)tile).getStorage().getCache(((BlockHitResult)target).getDirection()).getDisplayBlock();
         if (rayState != null) {
            try {
               return rayState.getCloneItemStack(target, world, pos, player);
            } catch (Exception var9) {
            }
         }
      }

      return this.instance.drops.apply(this.getInsulation(state));
   }

   @Override
   public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
      ItemStack stack = player.getMainHandItem();
      if (stack.getItem() instanceof ICutterItem) {
         ((ICutterItem)stack.getItem()).cutInsulation(player, stack, worldIn, pos);
      } else {
         super.attack(state, worldIn, pos, player);
      }
   }

   public static class CableInstance {
      boolean usesColor;
      boolean usesUninsulatedColor = false;
      boolean hasActivity;
      int insulation;
      float[] widths;
      String folder;
      List<String> textures = CollectionUtils.<String>createList();
      IntFunction<ItemStack> drops;

      public CableInstance(boolean usesColor, boolean hasActivity, int insulation, float[] widths, String folder, IntFunction<ItemStack> drops) {
         this.usesColor = usesColor;
         this.hasActivity = hasActivity;
         this.insulation = insulation;
         this.widths = widths;
         this.folder = folder;
         this.drops = drops;
      }

      public CableBlock.CableInstance setHasUninsulatedColor() {
         this.usesUninsulatedColor = true;
         return this;
      }

      public CableBlock.CableInstance addTextures(String... textures) {
         this.textures.addAll(ObjectArrayList.wrap(textures));
         return this;
      }

      public String getTexture(int insulation) {
         return this.folder + "/" + this.textures.get(insulation);
      }

      VoxelShape[][] createShapes() {
         if (this.widths != null && this.widths.length != 0) {
            VoxelShape[][] result = new VoxelShape[this.widths.length][64];

            for (int i = 0; i < this.widths.length; i++) {
               VoxelShape[] shapes = this.createShapes(i);

               for (int j = 0; j < 64; j++) {
                  DirectionList list = DirectionList.ofNumber(j);
                  VoxelShape[] entry = new VoxelShape[list.size()];
                  int index = 0;

                  for (Direction dir : list) {
                     entry[index++] = shapes[dir.get3DDataValue()];
                  }

                  result[i][j] = Shapes.or(shapes[6], entry);
               }
            }

            return result;
         } else {
            throw new RuntimeException("Need at least 1 width");
         }
      }

      VoxelShape[] createShapes(int index) {
         VoxelShape[] result = new VoxelShape[7];
         float minValue = 8.0F - this.widths[index];
         float maxValue = 8.0F + this.widths[index];
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
}
