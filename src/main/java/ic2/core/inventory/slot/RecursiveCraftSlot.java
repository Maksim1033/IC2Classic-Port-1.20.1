package ic2.core.inventory.slot;

import ic2.core.inventory.base.IHasInventory;

public class RecursiveCraftSlot extends LockedSlot {
   public RecursiveCraftSlot(IHasInventory inv, int index, int xPosition, int yPosition) {
      super(inv, index, xPosition, yPosition);
   }
}
