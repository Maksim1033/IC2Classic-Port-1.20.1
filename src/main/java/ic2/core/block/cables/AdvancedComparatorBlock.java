package ic2.core.block.cables;

import ic2.api.blocks.IAdvancedComparable;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.IAutoCreator;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.block.ITileModel;
import ic2.core.platform.rendering.features.block.ITileParticleTexture;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.ForgeEventFactory;

public class AdvancedComparatorBlock extends DiodeBlock implements ITileModel, IAutoCreator, ITileParticleTexture, IRegistryProvider, EntityBlock {
   public static final DirectionProperty FACING = IC2Properties.ALL_FACINGS;
   public static final DirectionProperty ROTATION = DirectionProperty.create("rotation", DirectionList.ALL);
   public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;
   public static final VoxelShape[] SHAPES = new VoxelShape[]{
      Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
      Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0),
      Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0),
      Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0)
   };

   public AdvancedComparatorBlock() {
      super(Block.Properties.of().noOcclusion());
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.DOWN)).setValue(ROTATION, Direction.SOUTH))
               .setValue(POWERED, false))
            .setValue(MODE, ComparatorMode.COMPARE)
      );
   }

   @Override
   public ResourceLocation getRegistryName() {
      return new ResourceLocation("ic2", "advanced_comparator");
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties()) {
         public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(IC2.PLATFORM.getTileRenderer());
         }
      };
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getParticleTexture(BlockState state) {
      return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("block/comparator"));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, ROTATION, MODE, POWERED});
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return IC2Tiles.ADVANCED_COMPARATOR.create(pos, state);
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = CollectionUtils.<ItemStack>createList();
         drops.add(new ItemStack(this));
         return drops;
      }
   }

   protected int getDelay(BlockState state) {
      return 2;
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      Direction dir = (Direction)state.getValue(FACING);
      pos = pos.relative(dir);
      return worldIn.getBlockState(pos).isFaceSturdy(worldIn, pos, dir.getOpposite(), SupportType.RIGID);
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPES[((Direction)state.getValue(FACING)).get3DDataValue()];
   }

   protected int getOutputSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
      BlockEntity tileEntity = worldIn.getBlockEntity(pos);
      return tileEntity instanceof AdvancedComparatorTileEntity ? ((AdvancedComparatorTileEntity)tileEntity).getOutputSignal() : 0;
   }

   private int calculateOutput(Level worldIn, BlockPos pos, BlockState state) {
      return state.getValue(MODE) == ComparatorMode.SUBTRACT
         ? Math.max(this.getInputSignal(worldIn, pos, state) - this.getAlternateSignal(worldIn, pos, state), 0)
         : this.getInputSignal(worldIn, pos, state);
   }

   protected boolean shouldTurnOn(Level worldIn, BlockPos pos, BlockState state) {
      int i = this.getInputSignal(worldIn, pos, state);
      if (i >= 15) {
         return true;
      } else {
         return i == 0 ? false : i >= this.getAlternateSignal(worldIn, pos, state);
      }
   }

   protected int getBaseStrength(Level worldIn, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.getValue(ROTATION);
      BlockPos offset = pos.relative(direction);
      int i = worldIn.getSignal(offset, direction);
      if (i >= 15) {
         return i;
      } else {
         BlockState offsetState = worldIn.getBlockState(offset);
         return Math.max(i, offsetState.getBlock() == Blocks.REDSTONE_WIRE ? (Integer)offsetState.getValue(RedStoneWireBlock.POWER) : 0);
      }
   }

   protected int getInputSignal(Level worldIn, BlockPos pos, BlockState state) {
      return getComparatorOutput(worldIn, pos, (Direction)state.getValue(ROTATION), true, this.getBaseStrength(worldIn, pos, state));
   }

   public static int getComparatorOutput(Level level, BlockPos pos, Direction dir, boolean throughBlocks, int defaultValue) {
      BlockPos offset = pos.relative(dir);
      BlockState state = level.getBlockState(offset);
      if (state.hasAnalogOutputSignal()) {
         return getComparatorOutput(state, level, offset, dir.getOpposite());
      } else {
         return defaultValue < 15 && throughBlocks && state.isRedstoneConductor(level, offset)
            ? getComparatorOutput(level, offset, dir, false, defaultValue)
            : defaultValue;
      }
   }

   public static int getComparatorOutput(BlockState state, Level world, BlockPos pos, Direction side) {
      return state.getBlock() instanceof IAdvancedComparable comp
         ? comp.getComparatorInputOverride(state, world, pos, side)
         : state.getAnalogOutputSignal(world, pos);
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      if (!player.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else {
         if (player.isShiftKeyDown()) {
            state = (BlockState)state.setValue(
               ROTATION, DirectionList.rotateAround((Direction)state.getValue(ROTATION), ((Direction)state.getValue(FACING)).getAxis())
            );
         } else {
            state = (BlockState)state.cycle(MODE);
         }

         worldIn.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, state.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F);
         worldIn.setBlock(pos, state, 2);
         this.onStateChange(worldIn, pos, state);
         return InteractionResult.SUCCESS;
      }
   }

   protected void checkTickOnNeighbor(Level worldIn, BlockPos pos, BlockState state) {
      if (!worldIn.getBlockTicks().willTickThisTick(pos, this)) {
         int i = this.calculateOutput(worldIn, pos, state);
         BlockEntity tileEntity = worldIn.getBlockEntity(pos);
         int j = tileEntity instanceof AdvancedComparatorTileEntity ? ((AdvancedComparatorTileEntity)tileEntity).getOutputSignal() : 0;
         if (i != j || (Boolean)state.getValue(POWERED) != this.shouldTurnOn(worldIn, pos, state)) {
            worldIn.scheduleTick(pos, this, 2, this.shouldPrioritize(worldIn, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL);
         }
      }
   }

   private void onStateChange(Level worldIn, BlockPos pos, BlockState state) {
      int i = this.calculateOutput(worldIn, pos, state);
      BlockEntity tile = worldIn.getBlockEntity(pos);
      int j = 0;
      if (tile instanceof AdvancedComparatorTileEntity comp) {
         j = comp.getOutputSignal();
         comp.setOutputSignal(i);
      }

      if (j != i || state.getValue(MODE) == ComparatorMode.COMPARE) {
         boolean flag1 = this.shouldTurnOn(worldIn, pos, state);
         boolean flag = (Boolean)state.getValue(POWERED);
         if (flag && !flag1) {
            worldIn.setBlock(pos, (BlockState)state.setValue(POWERED, false), 2);
         } else if (!flag && flag1) {
            worldIn.setBlock(pos, (BlockState)state.setValue(POWERED, true), 2);
         }

         this.updateNeighborsInFront(worldIn, pos, state);
      }
   }

   public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.getCommonPower(state, world, pos, side);
   }

   public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.getCommonPower(state, world, pos, side);
   }

   public int getCommonPower(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      if (side != state.getValue(ROTATION)) {
         return 0;
      } else {
         BlockEntity tile = world.getBlockEntity(pos);
         return tile instanceof AdvancedComparatorTileEntity ? ((AdvancedComparatorTileEntity)tile).getOutputSignal() : 0;
      }
   }

   @Override
   protected int getAlternateSignal(SignalGetter worldIn, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.getValue(FACING);
      Direction rotation = (Direction)state.getValue(ROTATION);
      Direction rotation1 = DirectionList.rotateAround(rotation, direction.getAxis());
      Direction rotation2 = DirectionList.rotateAround(rotation.getOpposite(), direction.getAxis());
      return Math.max(
         worldIn.getControlInputSignal(pos.relative(rotation1), rotation1, this.sideInputDiodesOnly()),
         worldIn.getControlInputSignal(pos.relative(rotation2), rotation2, this.sideInputDiodesOnly())
      );
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Vec3 hit = context.getClickLocation()
         .subtract((double)context.getClickedPos().getX(), (double)context.getClickedPos().getY(), (double)context.getClickedPos().getZ());
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite()))
         .setValue(ROTATION, getDefaultForAxis(context.getClickedFace().getOpposite().getAxis(), (float)hit.x(), (float)hit.y(), (float)hit.z()));
   }

   protected void updateNeighborsInFront(Level worldIn, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.getValue(ROTATION);
      BlockPos blockpos = pos.relative(direction.getOpposite());
      if (!ForgeEventFactory.onNeighborNotify(worldIn, pos, worldIn.getBlockState(pos), EnumSet.of(direction.getOpposite()), false).isCanceled()) {
         worldIn.neighborChanged(blockpos, this, pos);
         worldIn.updateNeighborsAtExceptFromFacing(blockpos, this, direction);
      }
   }

   public boolean shouldPrioritize(BlockGetter worldIn, BlockPos pos, BlockState state) {
      Direction direction = ((Direction)state.getValue(ROTATION)).getOpposite();
      BlockState blockstate = worldIn.getBlockState(pos.relative(direction));
      if (!isDiode(blockstate)) {
         return false;
      } else if (blockstate.hasProperty(ROTATION)) {
         return blockstate.getValue(ROTATION) != direction;
      } else {
         return blockstate.hasProperty(DiodeBlock.FACING) ? blockstate.getValue(DiodeBlock.FACING) != direction : false;
      }
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      Direction dir = (Direction)state.getValue(FACING);
      Direction rotatedDir = rot.rotate((Direction)state.getValue(FACING));
      Direction rotation = (Direction)state.getValue(ROTATION);
      return (BlockState)((BlockState)state.setValue(FACING, rotatedDir))
         .setValue(ROTATION, rotate(getDefaultForAxis(rotatedDir.getAxis()), getRotation(rotation, dir.getAxis()), rotatedDir.getAxis()));
   }

   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.getRotation((Direction)state.getValue(FACING)));
   }

   public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
      this.onStateChange(worldIn, pos, state);
   }

   public boolean getWeakChanges(BlockState state, LevelReader world, BlockPos pos) {
      return true;
   }

   public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
      if (pos.getY() == neighbor.getY() && world instanceof Level && !world.isClientSide()) {
         state.neighborChanged((Level)world, pos, world.getBlockState(neighbor).getBlock(), neighbor, false);
      }
   }

   public static Direction getDefaultForAxis(Axis side, float x, float y, float z) {
      switch (side) {
         case X:
            return !getDifference(y, z) ? (y > 0.5F ? Direction.DOWN : Direction.UP) : (z > 0.5F ? Direction.NORTH : Direction.SOUTH);
         case Y:
            return !getDifference(x, z) ? (x > 0.5F ? Direction.WEST : Direction.EAST) : (z > 0.5F ? Direction.NORTH : Direction.SOUTH);
         case Z:
            return !getDifference(y, x) ? (y > 0.5F ? Direction.DOWN : Direction.UP) : (x > 0.5F ? Direction.WEST : Direction.EAST);
         default:
            return Direction.NORTH;
      }
   }

   public static boolean getDifference(float x, float y) {
      return x < 0.5F ? (y >= 0.5F ? 1.0F - y < x : y < x) : (y < 0.5F ? 1.0F - y > x : y > x);
   }

   public static Direction getDefaultForAxis(Axis side) {
      switch (side) {
         case X:
            return Direction.UP;
         case Y:
            return Direction.NORTH;
         case Z:
            return Direction.UP;
         default:
            return Direction.NORTH;
      }
   }

   public static Direction rotate(Direction base, int count, Axis axis) {
      for (int i = 0; i < count; i++) {
         base = DirectionList.rotateAround(base, axis);
      }

      return base;
   }

   public static Direction getFacingForRotation(Axis axis, int rotation) {
      switch (axis) {
         case X:
            switch (rotation) {
               case 0:
                  return Direction.UP;
               case 1:
                  return Direction.NORTH;
               case 2:
                  return Direction.DOWN;
               case 3:
                  return Direction.SOUTH;
               default:
                  throw new IllegalArgumentException("InvalidRotationForAxis " + axis + ": " + rotation);
            }
         case Y:
            if (rotation == -1) {
               throw new IllegalArgumentException("InvalidRotationForAxis " + axis + ": " + rotation);
            }

            return Direction.from2DDataValue(rotation);
         case Z:
            switch (rotation) {
               case 0:
                  return Direction.UP;
               case 1:
                  return Direction.EAST;
               case 2:
                  return Direction.DOWN;
               case 3:
                  return Direction.WEST;
               default:
                  throw new IllegalArgumentException("InvalidRotationForAxis " + axis + ": " + rotation);
            }
         default:
            throw new IllegalArgumentException("InvalidAxis " + axis);
      }
   }

   public static int getRotation(Direction dir, Axis axis) {
      switch (axis) {
         case X:
            switch (dir) {
               case UP:
                  return 0;
               case NORTH:
                  return 1;
               case DOWN:
                  return 2;
               case SOUTH:
                  return 3;
               default:
                  return -1;
            }
         case Y:
            return dir.get2DDataValue();
         case Z:
            switch (dir) {
               case UP:
                  return 0;
               case NORTH:
               case SOUTH:
               default:
                  return -1;
               case DOWN:
                  return 2;
               case EAST:
                  return 1;
               case WEST:
                  return 3;
            }
         default:
            return -1;
      }
   }
}
