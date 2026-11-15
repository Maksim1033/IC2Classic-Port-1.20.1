package ic2.core.block.base.features;

import net.minecraft.core.Direction;

public interface IWrenchRemovable extends IWrenchableTile {
   @Override
   default boolean canSetFacing(Direction dir) {
      return false;
   }
}
