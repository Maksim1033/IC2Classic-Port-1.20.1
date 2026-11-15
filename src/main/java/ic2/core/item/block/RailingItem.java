package ic2.core.item.block;

import ic2.core.block.misc.base.IC2PanelBlock;
import ic2.core.block.misc.tiles.PanelCornerTileEntity;
import ic2.core.item.base.IC2BlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.Vec3;

public class RailingItem extends IC2BlockItem {
   public RailingItem(Block block, Properties properties) {
      super(block, properties);
   }

   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   protected BlockState getPlacementState(BlockPlaceContext context) {
      BlockState original = context.getLevel().getBlockState(context.getClickedPos());
      return original.getBlock() == ((IC2PanelBlock)this.getBlock()).getCorner() ? original : super.getPlacementState(context);
   }

   protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
      if (state.getBlock() == ((IC2PanelBlock)this.getBlock()).getCorner()) {
         BlockState original = context.getLevel().getBlockState(context.getClickedPos());
         if (original.getBlock() instanceof IC2PanelBlock) {
            Direction facing = (Direction)original.getValue(IC2PanelBlock.FACING);
            int oIndex = this.calcIndex(facing, original.getValue(IC2PanelBlock.SHAPE) == StairsShape.INNER_RIGHT);
            BlockPos pos = context.getClickedPos();
            Vec3 place = context.getClickLocation().subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            int index = place.x() < 0.5 ? (place.z() < 0.5 ? 0 : 3) : (place.z() < 0.5 ? 1 : 2);
            if (oIndex == index) {
               return false;
            }

            if (super.placeBlock(context, state)) {
               if (context.getLevel().getBlockEntity(pos) instanceof PanelCornerTileEntity railing) {
                  railing.setColor(
                     this.calcIndex(facing, original.getValue(IC2PanelBlock.SHAPE) == StairsShape.INNER_RIGHT), ((IC2PanelBlock)original.getBlock()).getColor()
                  );
                  railing.setColor(index, ((IC2PanelBlock)this.getBlock()).getColor());
                  return true;
               }

               return true;
            }

            return false;
         }

         if (original.getBlock() == ((IC2PanelBlock)this.getBlock()).getCorner()) {
            BlockPos posx = context.getClickedPos();
            if (context.getLevel().getBlockEntity(posx) instanceof PanelCornerTileEntity other) {
               Vec3 placex = context.getClickLocation().subtract((double)posx.getX(), (double)posx.getY(), (double)posx.getZ());
               int indexx = placex.x() < 0.5 ? (placex.z() < 0.5 ? 0 : 3) : (placex.z() < 0.5 ? 1 : 2);
               if (other.isSet(indexx)) {
                  return false;
               }

               other.setColor(indexx, ((IC2PanelBlock)this.getBlock()).getColor());
               return true;
            }

            return false;
         }
      }

      return super.placeBlock(context, state);
   }

   public int calcIndex(Direction dir, boolean right) {
      return (dir.get2DDataValue() + (right ? 1 : 0)) % 4;
   }
}
