package ic2.core.block.base.features.redstone;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IRedstoneListener {
   default boolean allowWeakSignal(Direction dir) {
      BlockEntity tile = (BlockEntity)this;
      return tile.getBlockState().isRedstoneConductor(tile.getLevel(), tile.getBlockPos());
   }

   boolean canConnectToRedstone(Direction var1);
}
