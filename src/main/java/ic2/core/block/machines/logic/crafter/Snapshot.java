package ic2.core.block.machines.logic.crafter;

import ic2.core.inventory.base.INBTSavable;
import ic2.core.utils.collection.NBTListWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class Snapshot implements INBTSavable {
   INBTSavable[] array;

   public Snapshot(int size) {
      this.array = new INBTSavable[size];
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.array.length; i < m; i++) {
         if (this.array[i] != null) {
            CompoundTag data = new CompoundTag();
            data.putByte("index", (byte)i);
            data.put("data", this.array[i].save(new CompoundTag()));
            list.add(data);
         }
      }

      nbt.put("data", list);
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("data", 10), CompoundTag.class)) {
         int index = data.getInt("index");
         if (this.array[index] != null) {
            this.array[index].load(data.getCompound("data"));
         }
      }
   }

   public void set(int index, INBTSavable savable) {
      this.array[index] = savable;
   }

   public <T> T get(int index) {
      return (T)this.array[index];
   }

   public <T> T get(int index, Class<T> clz) {
      return (T)this.array[index];
   }
}
