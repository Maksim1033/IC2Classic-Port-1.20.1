package ic2.core.inventory.slot;

import ic2.core.block.base.tiles.impls.BaseChargePadTileEntity;
import ic2.core.inventory.filter.ClassFilter;
import ic2.core.item.upgrades.PadUpgradeItem;

public class ChargePadSlot extends FilterSlot {
   public ChargePadSlot(BaseChargePadTileEntity inv, int index, int xPosition, int yPosition) {
      super(inv, index, xPosition, yPosition, new ClassFilter(PadUpgradeItem.class));
   }

   @Override
   public void setChanged() {
      super.setChanged();
      ((BaseChargePadTileEntity)this.inventory).onPadUpgradeChanged();
   }
}
