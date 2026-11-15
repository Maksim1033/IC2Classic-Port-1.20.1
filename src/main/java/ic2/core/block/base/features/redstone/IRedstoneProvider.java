package ic2.core.block.base.features.redstone;

import net.minecraft.core.Direction;

public interface IRedstoneProvider {
   int getCommonSignalStrength(Direction var1);

   default int getStrongSignalStrength(Direction side) {
      return this.getCommonSignalStrength(side);
   }

   default int getWeakSignalStrength(Direction side) {
      return this.getCommonSignalStrength(side);
   }
}
