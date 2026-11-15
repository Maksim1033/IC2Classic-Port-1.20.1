package ic2.core.block.base.features.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface IStructureListener {
   void markStructureDirty(BlockPos var1, BlockState var2);
}
