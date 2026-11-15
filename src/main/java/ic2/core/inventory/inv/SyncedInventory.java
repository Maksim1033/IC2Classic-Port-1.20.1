package ic2.core.inventory.inv;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;

public class SyncedInventory extends SimpleInventory implements INetworkDataBuffer {
   public SyncedInventory(int size) {
      super(size);
   }

   @Override
   public void write(IOutputBuffer buffer) {
      for (int i = 0; i < this.size; i++) {
         buffer.writeItemStack(this.getStackInSlot(i));
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      for (int i = 0; i < this.size; i++) {
         this.setStackInSlot(i, buffer.readItemStack());
      }
   }
}
