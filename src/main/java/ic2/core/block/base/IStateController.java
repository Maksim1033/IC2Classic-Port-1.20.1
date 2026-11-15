package ic2.core.block.base;

import ic2.core.block.base.tiles.BaseTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IStateController<T extends BaseTileEntity> extends EntityBlock {
   void onStateUpdate(Level var1, BlockPos var2, BlockState var3, T var4);

   BlockEntity newBlockEntity(BlockPos var1, BlockState var2);
}
