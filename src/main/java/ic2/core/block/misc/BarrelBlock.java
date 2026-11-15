package ic2.core.block.misc;

import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.IStateController;
import ic2.core.block.misc.tiles.BarrelTileEntity;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.item.block.BarrelItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IRotatableBlock;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.tooltips.helper.ITooltipProvider;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BarrelBlock extends IC2ContainerBlock implements IBlockModel, IStateController<BarrelTileEntity>, IRotatableBlock {
   public static final DirectionProperty FACING = DirectionProperty.create("facing", DirectionList.HORIZONTAL.toFacings());
   public static final BooleanProperty TREETAP = BooleanProperty.create("treetab");

   public BarrelBlock() {
      super("barrel", Block.Properties.of().strength(1.0F, 1.0F).sound(SoundType.WOOD));
      this.setHarvestTool(Tool.AXE);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(TREETAP, false));
      this.addTooltip(ITooltipProvider.tooltip("tooltip.block.ic2.barrel.info"));
   }

   @Override
   public List<BlockState> getModelStates() {
      return this.getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return state.getValue(TREETAP) && side == Direction.SOUTH
         ? IC2Textures.getMappedEntriesBlockIC2("misc/barrel").get("treetap")
         : IC2Textures.getMappedEntriesBlockIC2("misc/barrel").get(side.getName());
   }

   public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (!IC2.PLATFORM.isRendering()) {
         BlockEntity tile = worldIn.getBlockEntity(pos);
         if (tile instanceof BarrelTileEntity) {
            ((BarrelTileEntity)tile).loadItemData(stack);
         }
      }
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return IC2Tiles.BARREL.create(pos, state);
   }

   public IC2BlockItem createItem() {
      return new BarrelItem(this);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING}).add(new Property[]{TREETAP});
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BarrelTileEntity tile) {
      tile.setState((BlockState)((BlockState)state.setValue(FACING, tile.getFacing())).setValue(TREETAP, tile.hasTreeTap()));
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return worldIn.getBlockState(pos).getBlock() == IC2Blocks.SCAFFOLD_WOOD;
   }

   @Override
   public boolean hasRotation(BlockState state) {
      return true;
   }

   @Override
   public Direction getRotation(BlockState state) {
      return (Direction)state.getValue(FACING);
   }
}
