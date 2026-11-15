package ic2.core.item.upgrades.swaps;

import ic2.core.block.base.tiles.impls.BaseBatteryStationTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BatteryStationSwapper implements ISwapper {
   public static final ISwapper INSTANCE = new BatteryStationSwapper();

   @Override
   public void transfer(BlockEntity from, BlockEntity to) {
      if (from instanceof BaseBatteryStationTileEntity fromCharge && to instanceof BaseBatteryStationTileEntity toCharge) {
         toCharge.energy = fromCharge.energy;

         for (int i = 0; i < 17; i++) {
            toCharge.setStackInSlot(i, fromCharge.getStackInSlot(i));
         }

         return;
      }
   }
}
