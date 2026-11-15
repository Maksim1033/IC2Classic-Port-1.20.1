package ic2.core.block.machines.logic.crop;

import ic2.api.crops.ICrop;
import net.minecraft.nbt.LongTag;

public interface ISeedEntry {
   int getGain();

   int getGrowth();

   int getResistance();

   int getTier();

   ICrop getCrop(ICrop var1);

   int getCount();

   default boolean isEmpty() {
      return this.getCount() <= 0;
   }

   int grow(int var1);

   int drain(int var1, boolean var2);

   LongTag write();

   default ISeedEntry getWrapped() {
      return this;
   }
}
