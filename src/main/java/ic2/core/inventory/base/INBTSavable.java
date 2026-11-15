package ic2.core.inventory.base;

import net.minecraft.nbt.CompoundTag;

public interface INBTSavable {
   CompoundTag save(CompoundTag var1);

   void load(CompoundTag var1);
}
