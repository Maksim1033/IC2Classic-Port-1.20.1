package ic2.core.block.storage;

import ic2.core.block.base.blocks.BaseActivityBlock;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IRotatableBlock;
import ic2.core.utils.helpers.Tool;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TransformerBlock extends BaseActivityBlock<BaseTileEntity> implements IBlockModel, IRotatableBlock {
   public TransformerBlock(String blockName, IBlockDropProvider drop, ITextureProvider provider, BlockEntityType<? extends BlockEntity> type) {
      super(blockName, EnergyStorageBlock.BASE_ENERGY_STORAGE, provider, type);
      this.setDropProvider(drop);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   protected Direction getFacing(BlockPlaceContext context) {
      if (context.getPlayer() == null) {
         return Direction.NORTH;
      } else {
         int pitch = Math.round(context.getPlayer().getXRot());
         return pitch >= 65 ? Direction.UP : (pitch <= -65 ? Direction.DOWN : context.getHorizontalDirection().getOpposite());
      }
   }
}
