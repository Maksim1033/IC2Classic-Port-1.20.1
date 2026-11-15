package ic2.core.block.base.misc.comparator.types.special;

import ic2.api.tiles.IEnergyStorage;
import ic2.core.block.base.misc.comparator.BaseComparator;
import ic2.core.block.base.misc.readers.ILoader;
import net.minecraft.network.chat.Component;

public class MinecartComparator extends BaseComparator {
   ILoader loader;
   MinecartComparator.InfoType type;

   public MinecartComparator(String id, Component name, ILoader loader, MinecartComparator.InfoType type) {
      super(id, name);
      this.loader = loader;
      this.type = type;
   }

   @Override
   protected int createValue() {
      switch (this.type) {
         case HAS_MINECART:
            return this.loader.hasMinecart() ? 15 : 0;
         case HAS_TRANSFER:
            return this.loader.getTransferred() > 0 ? 15 : 0;
         case MINECART_ENERGY:
            return this.getMineCartEnergy();
         default:
            return 0;
      }
   }

   private int getMineCartEnergy() {
      IEnergyStorage storage = this.loader.getMinecart();
      return storage == null ? 0 : value((float)storage.getStoredEU(), (float)storage.getMaxEU(), 15);
   }

   public static enum InfoType {
      HAS_MINECART,
      HAS_TRANSFER,
      MINECART_ENERGY;
   }
}
