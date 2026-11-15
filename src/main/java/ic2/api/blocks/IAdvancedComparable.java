package ic2.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IAdvancedComparable {
   int getComparatorInputOverride(BlockState var1, Level var2, BlockPos var3, Direction var4);
}
