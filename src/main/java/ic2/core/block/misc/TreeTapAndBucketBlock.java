package ic2.core.block.misc;

import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.IC2Block;
import ic2.core.block.rendering.block.TreeTapAndBucketModel;
import ic2.core.block.resource.RubberwoodLogBlock;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.item.base.IC2TexturedBlockItem;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TreeTapAndBucketBlock extends IC2Block implements ICustomBlockModel {
   public static final DirectionProperty FACING = IC2Properties.HORIZONTAL_FACINGS;
   public static final IntegerProperty FILL_STAGE = IntegerProperty.create("fill_stage", 0, 5);
   public static final VoxelShape CENTER = Shapes.or(Block.box(4.0, 0.0, 4.0, 12.0, 6.0, 12.0), Block.box(7.0, 7.0, 7.0, 9.0, 12.0, 9.0));
   public static final VoxelShape[] SHAPE = new VoxelShape[]{
      Shapes.or(CENTER, new VoxelShape[]{Block.box(7.0, 2.0, 12.0, 9.0, 4.0, 16.0), Block.box(7.0, 10.0, 9.0, 9.0, 12.0, 16.0)}),
      Shapes.or(CENTER, new VoxelShape[]{Block.box(0.0, 2.0, 7.0, 10.0, 4.0, 9.0), Block.box(0.0, 10.0, 7.0, 7.0, 12.0, 9.0)}),
      Shapes.or(CENTER, new VoxelShape[]{Block.box(7.0, 2.0, 0.0, 9.0, 4.0, 4.0), Block.box(7.0, 10.0, 0.0, 9.0, 12.0, 7.0)}),
      Shapes.or(CENTER, new VoxelShape[]{Block.box(12.0, 2.0, 7.0, 16.0, 4.0, 9.0), Block.box(9.0, 10.0, 7.0, 16.0, 12.0, 9.0)})
   };

   public TreeTapAndBucketBlock() {
      super("treetap_and_bucket", Properties.of().strength(4.0F));
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(FILL_STAGE, 0));
      this.setHarvestTool(Tool.PICKAXE);
   }

   public IC2BlockItem createItem() {
      return new IC2TexturedBlockItem(this, "misc", "treetap_and_bucket");
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, FILL_STAGE});
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new TreeTapAndBucketModel(state, IC2Textures.getMappedEntriesBlockIC2("misc").get("treetap_and_bucket"));
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPE[((Direction)state.getValue(FACING)).get2DDataValue()];
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return this.getFacing((Level)worldIn, pos) != null;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = this.getFacing(context.getLevel(), context.getClickedPos());
      return direction == null ? null : (BlockState)this.defaultBlockState().setValue(FACING, direction);
   }

   protected Direction getFacing(Level world, BlockPos pos) {
      for (Direction dir : DirectionList.HORIZONTAL) {
         BlockState neighbor = DirectionList.getNeighborState(world, pos, dir);
         if (neighbor.getBlock() instanceof RubberwoodLogBlock
            && (Boolean)neighbor.getValue(RubberwoodLogBlock.RESIN)
            && neighbor.getValue(RubberwoodLogBlock.RESIN_FACING) == dir.getOpposite()) {
            return dir;
         }
      }

      return null;
   }

   public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(worldIn, pos, state, placer, stack);
      worldIn.scheduleTick(pos, this, 200);
   }

   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      BlockState neighbor = worldIn.getBlockState(pos.relative((Direction)state.getValue(FACING)));
      if (neighbor.getBlock() instanceof RubberwoodLogBlock
         && (Boolean)neighbor.getValue(RubberwoodLogBlock.RESIN)
         && neighbor.getValue(RubberwoodLogBlock.RESIN_FACING) == ((Direction)state.getValue(FACING)).getOpposite()) {
         worldIn.scheduleTick(pos, this, 200);
      } else {
         worldIn.destroyBlock(pos, true);
      }
   }

   public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      BlockState neighbor = worldIn.getBlockState(pos.relative((Direction)state.getValue(FACING)));
      if (!(neighbor.getBlock() instanceof RubberwoodLogBlock)
         || !(Boolean)neighbor.getValue(RubberwoodLogBlock.RESIN)
         || neighbor.getValue(RubberwoodLogBlock.RESIN_FACING) != ((Direction)state.getValue(FACING)).getOpposite()) {
         worldIn.destroyBlock(pos, true);
      } else if ((Integer)state.getValue(FILL_STAGE) < 5) {
         if ((Boolean)neighbor.getValue(RubberwoodLogBlock.COLLECTABLE)) {
            worldIn.setBlockAndUpdate(pos.relative((Direction)state.getValue(FACING)), (BlockState)neighbor.setValue(RubberwoodLogBlock.COLLECTABLE, false));
            worldIn.setBlockAndUpdate(pos, (BlockState)state.setValue(FILL_STAGE, Math.min(5, (Integer)state.getValue(FILL_STAGE) + 1 + random.nextInt(3))));
         }

         worldIn.scheduleTick(pos, this, 200);
      }
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      int stage = (Integer)state.getValue(FILL_STAGE);
      if (stage > 0) {
         if (IC2.PLATFORM.isSimulating()) {
            worldIn.setBlockAndUpdate(pos, (BlockState)state.setValue(FILL_STAGE, 0));
            Block.popResource(worldIn, pos, new ItemStack(IC2Items.STICKY_RESIN, stage));
            worldIn.scheduleTick(pos, this, 200);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public void addExtraDrops(List<ItemStack> list, BlockState state, ItemStack tool, RandomSource rand) {
      int props = (Integer)state.getValue(FILL_STAGE);
      if (props > 0) {
         list.add(new ItemStack(IC2Items.STICKY_RESIN, props));
      }
   }
}
