package ic2.core.block.resource;

import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class RubberwoodBarkBlock extends IC2Block implements IBlockModel, IBlockModifiers {
   private boolean isStripped;

   public RubberwoodBarkBlock(boolean isStripped) {
      super(
         isStripped ? "rubber_log_barked_stripped" : "rubber_log_barked",
         Properties.of().sound(SoundType.WOOD).strength(1.0F, 5.0F).randomTicks()
      );
      this.setHarvestTool(Tool.AXE);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(RubberwoodLogBlock.AXIS, Axis.Y));
      this.isStripped = isStripped;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{RubberwoodLogBlock.AXIS});
   }

   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
      return toolAction == ToolActions.AXE_STRIP && !this.isStripped && context.getItemInHand().canPerformAction(toolAction)
         ? PainterHelper.copyProperties(state, IC2Blocks.RUBBER_LOG_BARKED_STRIPPED.defaultBlockState())
         : super.getToolModifiedState(state, context, toolAction, simulate);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(RubberwoodLogBlock.AXIS, context.getClickedFace().getAxis());
   }

   @Override
   public boolean hasTextureRotation(BlockState state, Direction side) {
      Axis axis = (Axis)state.getValue(RubberwoodLogBlock.AXIS);
      return axis == Axis.Y ? false : axis == Axis.X || side == Direction.EAST || side == Direction.WEST;
   }

   @Override
   public int getTextureRotation(BlockState state, Direction side) {
      Axis axis = (Axis)state.getValue(RubberwoodLogBlock.AXIS);
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
      return IC2Textures.getMappedEntriesBlockIC2("resources/rubberwood").get("log_sides" + (this.isStripped ? "_stripped" : ""));
   }

   public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return 20;
   }

   public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      return 4;
   }
}
