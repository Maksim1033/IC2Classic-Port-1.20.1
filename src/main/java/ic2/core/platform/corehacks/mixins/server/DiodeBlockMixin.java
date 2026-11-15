package ic2.core.platform.corehacks.mixins.server;

import ic2.core.block.cables.AdvancedComparatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin({DiodeBlock.class})
public class DiodeBlockMixin {
   @Overwrite
   public boolean shouldPrioritize(BlockGetter world, BlockPos pos, BlockState state) {
      Direction direction = ((Direction)state.getValue(DiodeBlock.FACING)).getOpposite();
      BlockState blockstate = world.getBlockState(pos.relative(direction));
      if (DiodeBlock.isDiode(blockstate)) {
         return blockstate.getBlock() instanceof AdvancedComparatorBlock
            ? blockstate.getValue(AdvancedComparatorBlock.ROTATION) != direction
            : blockstate.getValue(DiodeBlock.FACING) != direction;
      } else {
         return false;
      }
   }
}
