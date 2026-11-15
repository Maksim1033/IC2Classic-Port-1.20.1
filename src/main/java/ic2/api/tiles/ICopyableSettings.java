package ic2.api.tiles;

import net.minecraft.nbt.CompoundTag;

public interface ICopyableSettings {
   void saveSettings(CompoundTag var1);

   void loadSettings(CompoundTag var1);
}
