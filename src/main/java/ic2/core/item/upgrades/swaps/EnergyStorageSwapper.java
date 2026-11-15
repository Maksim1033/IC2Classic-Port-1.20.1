package ic2.core.item.upgrades.swaps;

import ic2.core.block.base.tiles.impls.BaseEnergyStorageTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnergyStorageSwapper implements ISwapper {
   public static final ISwapper INSTANCE = new EnergyStorageSwapper();

   @Override
   public void transfer(BlockEntity from, BlockEntity to) {
      if (from instanceof BaseEnergyStorageTileEntity fromStorage && to instanceof BaseEnergyStorageTileEntity toStorage) {
         toStorage.energy = fromStorage.energy;
         toStorage.setFacing(fromStorage.getFacing());
         toStorage.redstoneMode = fromStorage.redstoneMode;
         int i = 0;

         for (int m = Math.min(toStorage.inventorySize, fromStorage.inventorySize); i < m; i++) {
            toStorage.setStackInSlot(i, fromStorage.getStackInSlot(i));
         }

         return;
      }
   }
}
