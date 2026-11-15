package ic2.core.block.storage;

import ic2.api.util.DirectionList;
import ic2.core.block.base.blocks.BaseFacingBlock;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.storage.tiles.RedirectorMasterTileEntity;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.utils.helpers.Tool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RedirectorBlock extends BaseFacingBlock<BaseTileEntity> {
   boolean master;

   public RedirectorBlock(String blockName, boolean master, BlockEntityType<? extends BlockEntity> tile) {
      super(blockName, EnergyStorageBlock.BASE_ENERGY_STORAGE, ITextureProvider.noStateIC2("electric/cable/redirector/" + (master ? "master" : "slave")), tile);
      this.setDropProvider(IBlockDropProvider.SELF_OR_MACHINE);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
      this.master = master;
   }

   @Override
   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.master && this.getFacing(worldIn, pos, (Direction)state.getValue(FACING)) == null) {
         Block.popResource(worldIn, pos, new ItemStack(this));
         worldIn.removeBlock(pos, false);
      } else {
         super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      if (!this.master) {
         Direction facing = this.getFacing(context.getLevel(), context.getClickedPos(), null);
         return facing == null ? null : (BlockState)this.defaultBlockState().setValue(FACING, facing);
      } else {
         return super.getStateForPlacement(context);
      }
   }

   public Direction getFacing(Level world, BlockPos pos, Direction presentDir) {
      if (presentDir == null) {
         for (Direction dir : DirectionList.ALL) {
            BlockEntity tile = world.getBlockEntity(pos.relative(dir));
            if (tile instanceof RedirectorMasterTileEntity && dir != ((RedirectorMasterTileEntity)tile).getFacing().getOpposite()) {
               return dir;
            }
         }

         return null;
      } else {
         BlockEntity tile = DirectionList.getNeighborTile(world, pos, presentDir);
         return tile instanceof RedirectorMasterTileEntity && ((RedirectorMasterTileEntity)tile).getFacing().getOpposite() != presentDir ? presentDir : null;
      }
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
