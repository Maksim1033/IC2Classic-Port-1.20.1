package ic2.core.block.base.misc.comparator.types.base;

import ic2.api.tiles.readers.IPumpTile;
import ic2.core.block.base.misc.comparator.BaseComparator;
import net.minecraft.network.chat.Component;

public class PumpComparator extends BaseComparator {
   IPumpTile pump;

   public PumpComparator(String id, Component name, IPumpTile pump) {
      super(id, name);
      this.pump = pump;
   }

   @Override
   protected int createValue() {
      return value((float)this.pump.getPumpProgress(), (float)this.pump.getPumpMaxProgress(), 15);
   }
}
