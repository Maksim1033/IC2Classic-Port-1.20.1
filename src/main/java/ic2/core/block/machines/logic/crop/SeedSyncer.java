package ic2.core.block.machines.logic.crop;

import ic2.api.crops.ICropRegistry;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

public class SeedSyncer implements INetworkDataBuffer {
   int cropCount = 0;
   int statCount = 0;
   int itemCount = 0;
   boolean active = true;
   List<ResourceLocation> existingCrops = CollectionUtils.<ResourceLocation>createList();

   public int size() {
      return this.existingCrops.size();
   }

   public ResourceLocation get(int index) {
      return this.existingCrops.get(index);
   }

   public int getCropCount() {
      return this.cropCount;
   }

   public int getItemCount() {
      return this.itemCount;
   }

   public int getStatCount() {
      return this.statCount;
   }

   public boolean isActive() {
      return this.active;
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeInt(this.cropCount);
      buffer.writeInt(this.statCount);
      buffer.writeInt(this.itemCount);
      buffer.writeBoolean(this.active);
      buffer.writeInt(this.existingCrops.size());
      int i = 0;

      for (int m = this.existingCrops.size(); i < m; i++) {
         buffer.writeString(this.existingCrops.get(i).toString());
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.existingCrops.clear();
      this.cropCount = buffer.readInt();
      this.statCount = buffer.readInt();
      this.itemCount = buffer.readInt();
      this.active = buffer.readBoolean();
      int size = buffer.readInt();

      for (int i = 0; i < size; i++) {
         this.existingCrops.add(new ResourceLocation(buffer.readString()));
      }
   }

   public void read(CompoundTag nbt) {
      this.cropCount = nbt.getInt("crop_count");
      this.statCount = nbt.getInt("stat_count");
      this.itemCount = nbt.getInt("item_count");

      for (StringTag entry : NBTListWrapper.wrap(nbt.getList("crops", 8), StringTag.class)) {
         ResourceLocation location = new ResourceLocation(entry.getAsString());
         if (ICropRegistry.INSTANCE.getCrop(location) != null) {
            this.existingCrops.add(location);
         }
      }
   }

   public CompoundTag write() {
      CompoundTag nbt = new CompoundTag();
      NBTUtils.putInt(nbt, "crop_count", this.cropCount, 0);
      NBTUtils.putInt(nbt, "stat_count", this.statCount, 0);
      NBTUtils.putInt(nbt, "item_count", this.itemCount, 0);
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.existingCrops.size(); i < m; i++) {
         list.add(StringTag.valueOf(this.existingCrops.get(i).toString()));
      }

      NBTUtils.put(nbt, "crops", list);
      return nbt;
   }
}
