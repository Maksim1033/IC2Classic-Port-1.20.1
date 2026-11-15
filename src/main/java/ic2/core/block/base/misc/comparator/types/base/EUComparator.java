package ic2.core.block.base.misc.comparator.types.base;

import ic2.api.tiles.readers.IEUStorage;
import ic2.core.block.base.misc.comparator.BaseComparator;
import net.minecraft.network.chat.Component;

public class EUComparator extends BaseComparator {
   IEUStorage storage;

   public EUComparator(String id, Component name, IEUStorage storage) {
      super(id, name);
      this.storage = storage;
   }

   @Override
   protected int createValue() {
      return value((float)this.storage.getStoredEU(), (float)this.storage.getMaxEU(), 15);
   }
}
