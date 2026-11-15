package ic2.core.block.base.features.multiblock;

import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.tiles.BaseMultiBlockTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface IMultiBlockClickable extends IClickable {
   default boolean shouldAbsorbClick(BlockPos blockClicked, BlockState state) {
      return ((BaseMultiBlockTileEntity)this).isValid;
   }

   default boolean shouldLinkToBlock(BlockPos blockClicked, BlockState state) {
      return ((BaseMultiBlockTileEntity)this).isValid;
   }

   default BlockPos getOrigin() {
      return ((BlockEntity)this).getBlockPos();
   }

   @Override
   default boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      return false;
   }
}
