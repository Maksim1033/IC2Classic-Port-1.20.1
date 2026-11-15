package ic2.core.block.storage;

import ic2.api.util.DirectionList;
import ic2.core.block.base.blocks.BaseFacingBlock;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.utils.helpers.Tool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BaseLoaderBlock extends BaseFacingBlock<BaseInventoryTileEntity> {
   public BaseLoaderBlock(String blockName, ITextureProvider provider, BlockEntityType<? extends BlockEntity> tile) {
      super(blockName, EnergyStorageBlock.BASE_ENERGY_STORAGE, provider, tile);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   protected Direction getFacing(BlockPlaceContext context) {
      DirectionList list = this.getValidFacings(context.getLevel(), context.getClickedPos());
      if (list.size() == 1) {
         return list.getDefaultFacing();
      } else {
         Direction player = this.getPlayerFacing(context);
         return list.contains(player) ? player : list.getRandomFacing();
      }
   }

   public Direction getPlayerFacing(BlockPlaceContext context) {
      if (context.getPlayer() == null) {
         return Direction.NORTH;
      } else {
         int pitch = Math.round(context.getPlayer().getXRot());
         return pitch >= 65 ? Direction.UP : (pitch <= -65 ? Direction.DOWN : context.getHorizontalDirection().getOpposite());
      }
   }

   public DirectionList getValidFacings(Level level, BlockPos pos) {
      DirectionList result = DirectionList.EMPTY;

      for (Direction dir : DirectionList.ALL) {
         BlockState state = level.getBlockState(pos.relative(dir));
         if (state.is(BlockTags.RAILS)) {
            result = result.add(dir);
         }
      }

      return result.isEmpty() ? DirectionList.ALL : result;
   }
}
