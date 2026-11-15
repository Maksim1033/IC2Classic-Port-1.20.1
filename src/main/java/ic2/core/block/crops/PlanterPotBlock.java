package ic2.core.block.crops;

import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.rendering.block.PlanterPotModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.helpers.Tool;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlanterPotBlock extends IC2ContainerBlock implements ICustomBlockModel, IDualLogged {
   static final VoxelShape SHAPE = Shapes.or(
      Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0),
      new VoxelShape[]{
         Block.box(1.0, 4.0, 1.0, 15.0, 9.0, 15.0),
         Block.box(0.0, 9.0, 0.0, 16.0, 15.0, 16.0),
         Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 1.0),
         Block.box(0.0, 15.0, 15.0, 16.0, 16.0, 16.0),
         Block.box(0.0, 15.0, 1.0, 1.0, 16.0, 15.0),
         Block.box(15.0, 15.0, 1.0, 16.0, 16.0, 15.0)
      }
   );
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;

   public PlanterPotBlock() {
      super("planter_pot", Properties.of());
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(WATER, false)).setValue(LAVA, false));
      this.setHarvestTool(Tool.PICKAXE);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return IC2Tiles.PLANTER_POT.create(pos, state);
   }

   public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
      Block block = getId(stack);
      if (block != null && block != Blocks.AIR) {
         BlockEntity tile = world.getBlockEntity(pos);
         if (tile instanceof PlanterTileEntity) {
            ((PlanterTileEntity)tile).setBlock(block);
         }
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> p_206840_1_) {
      super.createBlockStateDefinition(p_206840_1_);
      p_206840_1_.add(new Property[]{WATER}).add(new Property[]{LAVA});
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }

   @Override
   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if ((Boolean)state.getValue(WATER)) {
         world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      } else if ((Boolean)state.getValue(LAVA)) {
         world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState state = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)this.defaultBlockState().setValue(WATER, state.getType() == Fluids.WATER)).setValue(LAVA, state.getType() == Fluids.LAVA);
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack stack, RandomSource rand, BlockEntity tile, boolean wrench) {
      ItemStack result = new ItemStack(this);
      if (tile instanceof PlanterTileEntity) {
         setId(result, ((PlanterTileEntity)tile).block);
      }

      return result;
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof PlanterTileEntity) {
         ItemStack result = new ItemStack(this);
         setId(result, ((PlanterTileEntity)tile).block);
         return result;
      } else {
         return super.getCloneItemStack(state, target, world, pos, player);
      }
   }

   public void m_49811_(CreativeModeTab p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
      for (Block block : CropRegistry.REGISTRY.getAllFarmlands()) {
         ItemStack stack = new ItemStack(this);
         setId(stack, block);
         p_149666_2_.add(stack);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      TextureAtlasSprite[] sprite = new TextureAtlasSprite[6];
      Map<String, TextureAtlasSprite> sprites = IC2Textures.getMappedEntriesBlockIC2("crops/planter_pot");

      for (Direction dir : Direction.values()) {
         sprite[dir.get3DDataValue()] = sprites.get(dir.getName());
      }

      return new PlanterPotModel(sprite);
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public static Block getId(ItemStack stack) {
      CompoundTag data = StackUtil.getNbtData(stack);
      return data.contains("id") ? ForgeRegistryWrapper.getBlockValue(ResourceLocation.tryParse(data.getString("id"))) : Blocks.AIR;
   }

   public static void setId(ItemStack stack, Block block) {
      stack.getOrCreateTag().putString("id", ForgeRegistryWrapper.getBlockKey(block).toString());
   }
}
