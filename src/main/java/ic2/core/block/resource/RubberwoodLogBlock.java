package ic2.core.block.resource;

import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class RubberwoodLogBlock extends IC2Block implements IBlockModifiers, IBlockModel {
   public static final DirectionProperty RESIN_FACING = IC2Properties.HORIZONTAL_FACINGS;
   public static final BooleanProperty RESIN = BooleanProperty.create("resin");
   public static final BooleanProperty COLLECTABLE = BooleanProperty.create("collectable");
   public static final EnumProperty<Axis> AXIS = RotatedPillarBlock.AXIS;
   private boolean isStripped;

   public RubberwoodLogBlock(boolean isStripped) {
      super(
         isStripped ? "rubber_wood_stripped" : "rubber_wood", Properties.of().sound(SoundType.WOOD).strength(1.0F, 5.0F).randomTicks()
      );
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(AXIS, Axis.Y)).setValue(RESIN, false))
               .setValue(COLLECTABLE, false))
            .setValue(RESIN_FACING, Direction.NORTH)
      );
      this.setHarvestTool(Tool.AXE);
      this.isStripped = isStripped;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
      return toolAction == ToolActions.AXE_STRIP && !this.isStripped && context.getItemInHand().canPerformAction(toolAction)
         ? PainterHelper.copyProperties(state, IC2Blocks.RUBBER_LOG_STRIPPED.defaultBlockState())
         : super.getToolModifiedState(state, context, toolAction, simulate);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{RESIN_FACING}).add(new Property[]{RESIN}).add(new Property[]{COLLECTABLE}).add(new Property[]{AXIS});
   }

   public void destroy(LevelAccessor worldIn, BlockPos pos, BlockState state) {
      if (worldIn.isAreaLoaded(pos, 5)) {
         int i = 4;

         for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-i, -i, -i), pos.offset(i, i, i))) {
            BlockState posState = worldIn.getBlockState(blockpos);
            if (posState.is(BlockTags.LEAVES)) {
               Blocks.AIR.defaultBlockState().updateNeighbourShapes(worldIn, blockpos, 2);
            }
         }
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
   }

   @Override
   public void addExtraDrops(List<ItemStack> list, BlockState state, ItemStack tool, RandomSource rand) {
      int fortune = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);

      for (int i = 0; i < 1 + fortune; i++) {
         if (rand.nextInt(12) == 0) {
            list.add(new ItemStack(IC2Items.STICKY_RESIN));
         }
      }
   }

   public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      this.update(state, worldIn, pos, random);
   }

   public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      this.update(state, worldIn, pos, random);
   }

   public void update(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      if ((Boolean)state.getValue(RESIN) && !(Boolean)state.getValue(COLLECTABLE)) {
         int range = worldIn.getBlockState(pos.relative((Direction)state.getValue(RESIN_FACING))).getBlock() == IC2Blocks.TREETAP_AND_BUCKET ? 150 : 200;
         if (random.nextInt(range) == 0) {
            worldIn.setBlockAndUpdate(pos, (BlockState)state.setValue(COLLECTABLE, true));
         } else {
            worldIn.scheduleTick(pos, this, range - 100);
         }
      }
   }

   @Override
   public boolean hasTextureRotation(BlockState state, Direction side) {
      Axis axis = (Axis)state.getValue(AXIS);
      return axis == Axis.Y ? false : axis == Axis.X || side == Direction.EAST || side == Direction.WEST;
   }

   @Override
   public int getTextureRotation(BlockState state, Direction side) {
      Axis axis = (Axis)state.getValue(AXIS);
      if (axis == Axis.Y) {
         return 0;
      } else {
         return axis == Axis.X ? 90 : (side != Direction.EAST && side != Direction.WEST ? 0 : 90);
      }
   }

   @Override
   public boolean hasCustomTextureUVs(BlockState state, Direction side) {
      return false;
   }

   @Override
   public float[] getCustomTextureUVs(BlockState state, Direction side) {
      return null;
   }

   @Override
   public List<BlockState> getModelStates() {
      return this.getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      Axis blockAxis = (Axis)state.getValue(AXIS);
      String stripped = this.isStripped ? "_stripped" : "";
      if (blockAxis != Axis.Y) {
         if (side.getAxis() == blockAxis) {
            return IC2Textures.getMappedEntriesBlockIC2("resources/rubberwood").get("log_top" + stripped);
         }
      } else {
         if (side.getAxis().isVertical()) {
            return IC2Textures.getMappedEntriesBlockIC2("resources/rubberwood").get("log_top" + stripped);
         }

         if ((Boolean)state.getValue(RESIN) && state.getValue(RESIN_FACING) == side) {
            return IC2Textures.getMappedEntriesBlockIC2("resources/rubberwood")
               .get(state.getValue(COLLECTABLE) ? "log_resin_full" + stripped : "log_resin_empty" + stripped);
         }
      }

      return IC2Textures.getMappedEntriesBlockIC2("resources/rubberwood").get("log_sides" + stripped);
   }

   public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return 20;
   }

   public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return 4;
   }

   public PushReaction getPistonPushReaction(BlockState state) {
      return state.getValue(RESIN) ? PushReaction.DESTROY : super.getPistonPushReaction(state);
   }
}
