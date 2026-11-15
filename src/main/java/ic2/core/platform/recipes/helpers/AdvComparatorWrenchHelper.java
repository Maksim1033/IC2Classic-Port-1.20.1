package ic2.core.platform.recipes.helpers;

import ic2.api.blocks.wrench.BaseWrenchHandler;
import ic2.core.block.cables.AdvancedComparatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AdvComparatorWrenchHelper extends BaseWrenchHandler {
   @Override
   public Direction getFacing(BlockState state, Level world, BlockPos pos) {
      return (Direction)state.getValue(AdvancedComparatorBlock.ROTATION);
   }

   @Override
   public boolean canSetFacing(BlockState state, Level world, BlockPos pos, Player player, Direction side) {
      int index = AdvancedComparatorBlock.getRotation(
         (Direction)state.getValue(AdvancedComparatorBlock.ROTATION), ((Direction)state.getValue(AdvancedComparatorBlock.FACING)).getAxis()
      );
      int newIndex = AdvancedComparatorBlock.getRotation(side, ((Direction)state.getValue(AdvancedComparatorBlock.FACING)).getAxis());
      return newIndex != index && newIndex != -1;
   }

   @Override
   public boolean setFacing(BlockState state, Level world, BlockPos pos, Player player, Direction side) {
      int index = AdvancedComparatorBlock.getRotation(
         (Direction)state.getValue(AdvancedComparatorBlock.ROTATION), ((Direction)state.getValue(AdvancedComparatorBlock.FACING)).getAxis()
      );
      int newIndex = AdvancedComparatorBlock.getRotation(side, ((Direction)state.getValue(AdvancedComparatorBlock.FACING)).getAxis());
      return index != newIndex && newIndex != -1
         ? world.setBlockAndUpdate(
            pos,
            (BlockState)state.setValue(
               AdvancedComparatorBlock.ROTATION,
               AdvancedComparatorBlock.getFacingForRotation(((Direction)state.getValue(AdvancedComparatorBlock.FACING)).getAxis(), newIndex)
            )
         )
         : false;
   }
}
