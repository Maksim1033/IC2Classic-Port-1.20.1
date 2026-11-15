package ic2.core.block.machines.logic.crop;

import java.util.Comparator;
import net.minecraft.network.chat.Component;

public class CropSorter implements Comparator<ISeedEntry> {
   Component name;
   int index;

   public CropSorter(int index) {
      this.name = Component.translatable(createName(index));
      this.index = index;
   }

   private static String createName(int index) {
      switch (index) {
         case 0:
            return "Combo Stat";
         case 1:
            return "Growth Stat";
         case 2:
            return "Gain Stat";
         case 3:
            return "Resist. Stat";
         case 4:
            return "Tier";
         case 5:
            return "Size";
         default:
            return "I AM ERROR";
      }
   }

   public Component getName() {
      return this.name;
   }

   public int getIndex() {
      return this.index;
   }

   public int compare(ISeedEntry o1, ISeedEntry o2) {
      return Integer.compare(this.getValue(o2), this.getValue(o1));
   }

   public int getValue(ISeedEntry entry) {
      switch (this.index) {
         case 0:
            return entry.getGrowth() + entry.getGain() + entry.getResistance();
         case 1:
            return entry.getGrowth();
         case 2:
            return entry.getGain();
         case 3:
            return entry.getResistance();
         case 4:
            return entry.getTier();
         case 5:
            return entry.getCount();
         default:
            return 0;
      }
   }
}
