package ic2.core.block.base.features.redstone;

import ic2.core.block.base.misc.comparator.ComparatorManager;
import net.minecraft.core.Direction;

public interface IComparable extends IComparatorProvider {
   ComparatorManager getManager();

   default boolean isAllowingUI() {
      ComparatorManager manager = this.getManager();
      return manager != null && manager.hasComparators();
   }

   @Override
   default int getSignalStrength(Direction side) {
      ComparatorManager manager = this.getManager();
      return manager == null ? 0 : manager.getValue(side);
   }
}
