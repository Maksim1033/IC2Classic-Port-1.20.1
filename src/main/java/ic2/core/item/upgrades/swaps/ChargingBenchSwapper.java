package ic2.core.item.upgrades.swaps;

import ic2.core.block.base.tiles.impls.BaseChargingBenchTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChargingBenchSwapper implements ISwapper {
   public static final ISwapper INSTANCE = new ChargingBenchSwapper();

   @Override
   public void transfer(BlockEntity from, BlockEntity to) {
      if (from instanceof BaseChargingBenchTileEntity fromCharge && to instanceof BaseChargingBenchTileEntity toCharge) {
         toCharge.sortedMode = fromCharge.sortedMode;
         toCharge.energy = fromCharge.energy;

         for (int i = 0; i < 17; i++) {
            toCharge.setStackInSlot(i, fromCharge.getStackInSlot(i));
         }

         return;
      }
   }
}
