package ic2.core.block.misc.base;

import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.item.tool.SprayerTool;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ScaffoldBlock extends IC2Block implements IRenderType {
   public static final VoxelShape COLLISION = Block.box(0.0625, 0.0, 0.0625, 15.9375, 16.0, 15.9375);
   protected IntegerProperty prop;
   protected int defaultStrength;
   protected int maxStrength;

   public ScaffoldBlock(String blockName, int min, int max, Properties properties) {
      super(blockName, properties.isViewBlocking((T, Y, V) -> false).isRedstoneConductor((K, V, Y) -> false).noOcclusion());
      this.defaultStrength = min;
      this.maxStrength = max;
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(this.prop, min));
   }

   public abstract IntegerProperty createProps();

   public abstract ItemStack getUpgradeItem();

   public abstract double getMaxSpeed(boolean var1);

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      this.prop = this.createProps();
      builder.add(new Property[]{this.prop});
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   public void setStrength(BlockState state, Level world, BlockPos pos, int strength) {
      world.setBlockAndUpdate(pos, (BlockState)state.setValue(this.prop, Mth.clamp(strength, 0, this.maxStrength)));
   }

   public int getStrength(BlockState state) {
      return (Integer)state.getValue(this.prop);
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
      return useContext.getItemInHand().getItem() instanceof SprayerTool;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return COLLISION;
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return worldIn instanceof Level && worldIn.getBlockState(pos).canBeReplaced() && this.getStrengthFrom((Level)worldIn, pos) >= 0;
   }

   public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
      if (entityIn instanceof Player player) {
         player.fallDistance = 0.0F;
         Vec3 motion = player.getDeltaMovement();
         double max = player.isShiftKeyDown() ? 0.08 : -0.15;
         double y = this.getMaxSpeed(IC2.KEYBOARD.isBoostKeyDown(player));
         if (IC2.KEYBOARD.isForwardKeyDown(player) && motion.y < y) {
            max = y;
            if (IC2.KEYBOARD.isBoostKeyDown(player)) {
               player.causeFoodExhaustion((float)y * 0.01F);
            }
         }

         player.setDeltaMovement(Mth.clamp(motion.x, -0.15, 0.15), Math.max(max, motion.y), Mth.clamp(motion.z, -0.15, 0.15));
         player.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(player.getDeltaMovement().y * 100.0));
      }
   }

   public boolean isPillar(Level world, BlockPos pos) {
      BlockPos realPos = pos;

      while (world.getBlockState(realPos).getBlock() == this) {
         realPos = realPos.below();
      }

      return world.getBlockState(realPos).isFaceSturdy(world, realPos, Direction.UP, SupportType.FULL);
   }

   public int getConnectedStrength(Level world, BlockPos pos) {
      if (!world.isLoaded(pos)) {
         return 0;
      } else {
         BlockState state = world.getBlockState(pos);
         return state.getBlock() == this ? this.getStrength(state) : 0;
      }
   }

   public int getStrengthFrom(Level world, BlockPos pos) {
      int strength = this.isPillar(world, pos.below()) ? this.defaultStrength + 1 : 0;

      for (Direction dir : DirectionList.HORIZONTAL) {
         strength = Math.max(strength, this.getConnectedStrength(world, pos.relative(dir)));
      }

      return strength - 1;
   }

   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      worldIn.scheduleTick(pos, blockIn, 1);
   }

   public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      if (state.getBlock() == this) {
         int ownStrength = this.getStrength(state);
         if (ownStrength >= this.maxStrength) {
            if (!this.isPillar(worldIn, pos)) {
               ownStrength = this.getStrengthFrom(worldIn, pos);
               ItemStack drop = this.getUpgradeItem();
               if (!drop.isEmpty()) {
                  Block.popResource(worldIn, pos, drop);
               }
            }
         } else {
            ownStrength = this.getStrengthFrom(worldIn, pos);
         }

         if (ownStrength < 0) {
            Block.popResource(worldIn, pos, new ItemStack(this));
            worldIn.removeBlock(pos, false);
            worldIn.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
         } else if (ownStrength != this.getStrength(state)) {
            this.setStrength(state, worldIn, pos, ownStrength);
         }
      }
   }

   public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (worldIn instanceof ServerLevel) {
         this.tick(state, (ServerLevel)worldIn, pos, worldIn.random);
      }
   }

   public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
      if (worldIn instanceof ServerLevel world) {
         if (!this.handleSideScaffold(world, pos, player)) {
            this.handleStandardScaffold(world, pos, player);
         }
      }
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      if (player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      } else {
         ItemStack current = player.getItemInHand(handIn);
         ItemStack upgrade = this.getUpgradeItem();
         if (!StackUtil.isStackEqual(current, upgrade) || current.getCount() < upgrade.getCount()) {
            return InteractionResult.PASS;
         } else if (this.getStrength(state) != this.maxStrength && this.isPillar(worldIn, pos)) {
            if (!player.isCreative()) {
               current.shrink(upgrade.getCount());
            }

            this.setStrength(state, worldIn, pos, this.maxStrength);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public boolean handleSideScaffold(ServerLevel world, BlockPos pos, Player player) {
      if (player.getY() <= (double)pos.getY()) {
         return false;
      } else {
         Direction facing = Direction.fromYRot((double)player.getYRot());
         ItemStack stack = player.getMainHandItem();
         if (stack.getItem() == this.asItem()) {
            BlockPos cuPos = pos;

            while (world.getBlockState(cuPos).getBlock() == this) {
               cuPos = cuPos.relative(facing);
            }

            if (this.canSurvive(this.defaultBlockState(), world, cuPos)) {
               world.setBlockAndUpdate(cuPos, this.defaultBlockState());
               this.tick(this.defaultBlockState(), world, cuPos, world.getRandom());
               this.onScaffoldPlaced(player, world, pos, cuPos, false);
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return true;
            }
         }

         stack = player.getOffhandItem();
         if (stack.getItem() == this.asItem()) {
            BlockPos cuPos = pos;

            while (world.getBlockState(cuPos).getBlock() == this) {
               cuPos = cuPos.relative(facing);
            }

            if (this.canSurvive(this.defaultBlockState(), world, cuPos)) {
               world.setBlockAndUpdate(cuPos, this.defaultBlockState());
               this.tick(this.defaultBlockState(), world, cuPos, world.getRandom());
               this.onScaffoldPlaced(player, world, pos, cuPos, false);
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               return true;
            }
         }

         return true;
      }
   }

   public void handleStandardScaffold(ServerLevel world, BlockPos pos, Player player) {
      ItemStack stack = player.getMainHandItem();
      if (stack.getItem() == this.asItem()) {
         BlockPos cuPos = pos;

         while (world.getBlockState(cuPos).getBlock() == this) {
            cuPos = cuPos.above();
         }

         if (this.canSurvive(this.defaultBlockState(), world, cuPos) && cuPos.getY() < world.getMaxBuildHeight()) {
            world.setBlockAndUpdate(cuPos, this.defaultBlockState());
            this.tick(this.defaultBlockState(), world, cuPos, world.getRandom());
            this.onScaffoldPlaced(player, world, pos, cuPos, true);
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return;
         }
      }

      stack = player.getOffhandItem();
      if (stack.getItem() == this.asItem()) {
         BlockPos cuPos = pos;

         while (world.getBlockState(cuPos).getBlock() == this) {
            cuPos = cuPos.above();
         }

         if (this.canSurvive(this.defaultBlockState(), world, cuPos) && cuPos.getY() < world.getMaxBuildHeight()) {
            world.setBlockAndUpdate(cuPos, this.defaultBlockState());
            this.tick(this.defaultBlockState(), world, cuPos, world.getRandom());
            this.onScaffoldPlaced(player, world, pos, cuPos, true);
            if (!player.isCreative()) {
               stack.shrink(1);
            }
         }
      }
   }

   protected void onScaffoldPlaced(Player player, Level level, BlockPos clickPos, BlockPos placePos, boolean vertical) {
   }

   @Override
   public void addExtraDrops(List<ItemStack> list, BlockState state, ItemStack tool, RandomSource rand) {
      if (this.getStrength(state) == this.maxStrength) {
         list.add(this.getUpgradeItem());
      }
   }
}
