package ic2.core.block.base.features;

import ic2.api.tiles.readers.IActivityProvider;
import ic2.core.block.base.tiles.BaseTileEntity;

public interface ITileActivityProvider extends IActivityProvider {
   @Override
   default boolean isActivated() {
      return ((BaseTileEntity)this).isActive();
   }
}
