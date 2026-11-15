package ic2.core.block.machines.logic.crafter;

import ic2.core.inventory.base.INBTSavable;
import net.minecraft.nbt.CompoundTag;

public class WrappedInteger implements INBTSavable {
   int value;

   public WrappedInteger() {
   }

   public WrappedInteger(int value) {
      this.value = value;
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      nbt.putInt("value", this.value);
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.value = nbt.getInt("value");
   }

   public int getValue() {
      return this.value;
   }
}
