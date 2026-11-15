package ic2.core.block.misc.base;

import ic2.api.blocks.DyeableMap;
import ic2.core.block.base.IC2Block;
import ic2.core.block.base.IStateController;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.misc.tiles.PanelCornerTileEntity;
import ic2.core.block.rendering.block.base.CornerRailingModel;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2CornerRailingBlock extends IC2Block implements ICustomBlockModel, IStateController<PanelCornerTileEntity>, IDualLogged {
   public static final VoxelShape[] SHAPES = createShapes();
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
   public static final IntegerProperty COLLISION = IntegerProperty.create("collision", 0, 15);
   protected BlockEntityType<? extends PanelCornerTileEntity> type;
   protected DyeableMap colorMap;
   protected String path;

   public IC2CornerRailingBlock(
      String blockName, String path, BlockEntityType<? extends PanelCornerTileEntity> type, Properties properties, DyeableMap colorMap
   ) {
      super(blockName, properties);
      this.colorMap = colorMap;
      this.path = path;
      this.type = type;
      ObjectIterator var6 = colorMap.getBlocks().iterator();

      while (var6.hasNext()) {
         Block block = (Block)var6.next();
         if (block instanceof IC2PanelBlock) {
            ((IC2PanelBlock)block).setCorner(this);
         }
      }

      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(WATER, false)).setValue(LAVA, false));
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      return Block.byItem(context.getItemInHand().getItem()) instanceof IC2PanelBlock && context.getPlayer() != null & context.getPlayer().isShiftKeyDown()
         ? (Integer)state.getValue(COLLISION) != 15
         : false;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> state) {
      state.add(new Property[]{COLLISION}).add(new Property[]{WATER}).add(new Property[]{LAVA});
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }

   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if ((Boolean)state.getValue(WATER)) {
         world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      } else if ((Boolean)state.getValue(LAVA)) {
         world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
      }
   }

   public DyeableMap getColorMap() {
      return this.colorMap;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(COLLISION)];
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new CornerRailingModel(this.path);
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, PanelCornerTileEntity tile) {
      tile.withState(COLLISION, Integer.valueOf(tile.getBoundId()));
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile) {
      return ItemStack.EMPTY;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.type.create(pos, state);
   }

   public void m_49811_(CreativeModeTab p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      if (target.getType() != Type.BLOCK) {
         return ItemStack.EMPTY;
      } else {
         BlockHitResult block = (BlockHitResult)target;
         Vec3 place = block.getLocation().subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         int index = place.x() < 0.5 ? (place.z() < 0.5 ? 0 : 3) : (place.z() < 0.5 ? 1 : 2);
         if (((Integer)state.getValue(COLLISION) & 1 << index) != 0) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof PanelCornerTileEntity) {
               DyeColor color = ((PanelCornerTileEntity)tile).getColor(index);
               if (color != null) {
                  ItemStack stack = new ItemStack(this.colorMap.getBlock(color));
                  if (((Nameable)tile).hasCustomName()) {
                     stack.setHoverName(((Nameable)tile).getCustomName());
                  }

                  return stack;
               }
            }
         }

         return ItemStack.EMPTY;
      }
   }

   @Override
   public BlockItem createItem() {
      return null;
   }

   private static VoxelShape[] createShapes() {
      VoxelShape[] baseShape = new VoxelShape[]{
         Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0),
         Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 2.0),
         Block.box(14.0, 0.0, 14.0, 16.0, 16.0, 16.0),
         Block.box(0.0, 0.0, 14.0, 2.0, 16.0, 16.0)
      };
      VoxelShape[] result = new VoxelShape[16];
      Arrays.fill(result, Shapes.empty());

      for (int i = 0; i < 16; i++) {
         if ((i & 1) != 0) {
            result[i] = Shapes.or(result[i], baseShape[0]);
         }

         if ((i & 2) != 0) {
            result[i] = Shapes.or(result[i], baseShape[1]);
         }

         if ((i & 4) != 0) {
            result[i] = Shapes.or(result[i], baseShape[2]);
         }

         if ((i & 8) != 0) {
            result[i] = Shapes.or(result[i], baseShape[3]);
         }

         if (i == 0) {
            result[i] = Shapes.block();
         }
      }

      return result;
   }
}
