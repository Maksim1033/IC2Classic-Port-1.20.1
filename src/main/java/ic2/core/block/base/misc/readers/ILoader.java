package ic2.core.block.base.misc.readers;

import ic2.api.tiles.IEnergyStorage;

public interface ILoader {
   boolean hasMinecart();

   int getTransferred();

   IEnergyStorage getMinecart();
}
