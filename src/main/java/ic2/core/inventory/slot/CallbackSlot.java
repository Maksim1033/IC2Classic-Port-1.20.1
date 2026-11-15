package ic2.core.inventory.slot;

import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.filter.IFilter;
import java.util.function.IntConsumer;

public class CallbackSlot extends FilterSlot {
   IntConsumer listener;

   public CallbackSlot(IHasInventory inv, int index, int xPosition, int yPosition, IFilter filter, IntConsumer listener) {
      super(inv, index, xPosition, yPosition, filter);
      this.listener = listener;
   }

   @Override
   public void setChanged() {
      this.listener.accept(this.getSlotIndex());
   }
}
