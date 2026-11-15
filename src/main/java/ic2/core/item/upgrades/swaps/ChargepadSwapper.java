package ic2.core.item.upgrades.swaps;

import ic2.core.block.base.tiles.impls.BaseChargePadTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChargepadSwapper implements ISwapper {
   public static final ISwapper INSTANCE = new ChargepadSwapper();

   @Override
   public void transfer(BlockEntity from, BlockEntity to) {
      if (from instanceof BaseChargePadTileEntity fromPad && to instanceof BaseChargePadTileEntity toPad) {
         toPad.energy = fromPad.energy;
         toPad.setFacingSilent(fromPad.getFacing());
         toPad.setStackInSlot(0, fromPad.getStackInSlot(0));
         if (fromPad.tier > 1) {
            toPad.setStackInSlot(1, fromPad.getStackInSlot(1));
            toPad.setStackInSlot(2, fromPad.getStackInSlot(2));
         }

         int fromStart = fromPad.tier > 1 ? 3 : 1;
         int toStart = toPad.tier > 1 ? 3 : 1;
         int fromEnd = fromPad.getSlotCount();
         int toEnd = toPad.getSlotCount();

         for (int i = 0; i + fromStart < fromEnd && i + toStart < toEnd; i++) {
            toPad.setStackInSlot(i + toStart, fromPad.getStackInSlot(i + fromStart));
         }

         return;
      }
   }
}
