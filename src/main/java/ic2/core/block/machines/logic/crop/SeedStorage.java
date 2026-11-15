package ic2.core.block.machines.logic.crop;

import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.item.misc.CropSeedItem;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class SeedStorage {
   final SimpleInventory inventory;
   final int typeLimit;
   final int statLimit;
   final int sizeLimit;
   Object2ObjectMap<ICrop, ObjectList<ISeedEntry>> crops = CollectionUtils.<ICrop, ObjectList<ISeedEntry>>createLinkedMap();
   LinkedList<ICrop> sorter = new LinkedList<>();
   SeedSyncer syncer = new SeedSyncer();
   int offset = 0;
   boolean active = true;

   public SeedStorage(int typeLimit, int statLimit, int sizeLimit) {
      this.inventory = new SimpleInventory(25);
      this.typeLimit = typeLimit;
      this.statLimit = statLimit;
      this.sizeLimit = sizeLimit;
      this.crops.put(null, ObjectArrayList.wrap(new ISeedEntry[0]));
   }

   public int getSizeLimit() {
      return this.sizeLimit;
   }

   public int getStatLimit() {
      return this.statLimit;
   }

   public int getTypeLimit() {
      return this.typeLimit;
   }

   public SeedSyncer getSyncer() {
      return this.syncer;
   }

   public void clear() {
      this.crops.clear();
      this.crops.put(null, ObjectArrayList.wrap(new ISeedEntry[0]));
      this.sorter.clear();
      this.syncer.existingCrops.clear();
      this.syncer.cropCount = 0;
      this.syncer.itemCount = 0;
      this.syncer.statCount = 0;
   }

   public NonNullList<ItemStack> getTypes() {
      NonNullList<ItemStack> items = NonNullList.create();
      ObjectIterator var2 = this.crops.object2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<ICrop, ObjectList<ISeedEntry>> entry = (Entry<ICrop, ObjectList<ISeedEntry>>)var2.next();
         if (entry.getKey() != null) {
            ItemStack stack = CropSeedItem.createStack((ICrop)entry.getKey(), 0, 0, 0, 4);
            CropSeedItem.setVisible(stack, true);
            ObjectListIterator var5 = ((ObjectList)entry.getValue()).iterator();

            while (var5.hasNext()) {
               ISeedEntry subEntry = (ISeedEntry)var5.next();
               stack.grow(subEntry.getCount());
            }

            stack.shrink(1);
            items.add(stack);
         }
      }

      return items;
   }

   public void addDrops(List<ItemStack> list) {
      ObjectList<ISeedEntry> seeds = (ObjectList<ISeedEntry>)this.crops.get(null);
      if (seeds != null) {
         ObjectListIterator var3 = seeds.iterator();

         while (var3.hasNext()) {
            ISeedEntry entry = (ISeedEntry)var3.next();
            ItemStack stack = CropSeedItem.createStack(entry.getCrop(null), entry.getGrowth(), entry.getGain(), entry.getResistance(), 4);
            if (!stack.isEmpty()) {
               int left = entry.getCount();

               while (left > 0) {
                  int toRemove = Math.min(left, 64);
                  list.add(StackUtil.copyWithSize(stack, toRemove));
                  left -= toRemove;
               }
            }
         }
      }
   }

   public SimpleInventory getInventory() {
      return this.inventory;
   }

   public boolean setActive(boolean value) {
      if (this.active != value) {
         this.active = value;
         this.syncer.active = value;
         if (!value) {
            this.inventory.clear();
         }

         return true;
      } else {
         return false;
      }
   }

   public void markAllDirty(ICrop current) {
      this.sorter.clear();
      List<ICrop> allCrops = new ObjectArrayList(this.crops.keySet());
      allCrops.remove(current);
      allCrops.add(0, current);
      this.sorter.addAll(allCrops);
   }

   public void markCurrentDirty(ICrop crop) {
      this.markDirty(crop);
   }

   public void offset(ICrop crop, int offset) {
      if (!this.active) {
         this.offset = 0;
      } else {
         ObjectList<ISeedEntry> list = (ObjectList<ISeedEntry>)this.crops.get(crop);
         this.offset = Mth.clamp(this.offset + offset, 0, list.isEmpty() ? 0 : (list.size() - 1) / 5);
      }
   }

   public boolean addCrop(ICrop crop, ISeedEntry stats) {
      if (!this.active) {
         return false;
      } else {
         ObjectList<ISeedEntry> entry = (ObjectList<ISeedEntry>)this.crops.get(crop);
         if (entry == null) {
            if (this.typeLimit > 0 && this.crops.size() >= this.typeLimit + 1) {
               return false;
            }

            entry = ObjectArrayList.wrap(new ISeedEntry[0]);
            this.crops.put(crop, entry);
            if (crop != null && crop.id() != null) {
               this.syncer.existingCrops.add(crop.id());
            }

            this.syncer.cropCount++;
         }

         int i = 0;

         for (int m = entry.size(); i < m; i++) {
            ISeedEntry present = (ISeedEntry)entry.get(i);
            if (present.hashCode() == stats.hashCode()) {
               if (this.sizeLimit > 0 && present.getCount() >= this.sizeLimit) {
                  return false;
               }

               if (this.sizeLimit < 0) {
                  this.syncer.itemCount = this.syncer.itemCount + present.grow(stats.drain(stats.getCount(), true));
               } else {
                  this.syncer.itemCount = this.syncer.itemCount
                     + present.grow(stats.drain(Math.min(stats.getCount(), this.sizeLimit - present.getCount()), true));
               }

               this.markDirty(crop);
               return true;
            }
         }

         if (this.statLimit > 0 && entry.size() >= this.statLimit) {
            return false;
         } else {
            ISeedEntry injected = new SeedEntry(
               stats.getGrowth(),
               stats.getGain(),
               stats.getResistance(),
               stats.getTier(),
               Math.min(this.sizeLimit == -1 ? 2147483647 : this.sizeLimit, stats.getCount())
            );
            stats.drain(injected.getCount(), true);
            entry.add(injected);
            ((ObjectList)this.crops.get(null)).add(new WrapperEntry(crop, injected));
            this.syncer.statCount++;
            this.syncer.itemCount = this.syncer.itemCount + injected.getCount();
            this.markDirty(crop);
            return true;
         }
      }
   }

   protected void markDirty(ICrop crop) {
      this.sorter.remove(crop);
      this.sorter.add(crop);
      this.sorter.remove(null);
      this.sorter.add(null);
   }

   public ItemStack remove(ICrop crop, int slot, int requested, boolean doDrain) {
      if (!this.active) {
         return ItemStack.EMPTY;
      } else {
         ObjectList<ISeedEntry> list = (ObjectList<ISeedEntry>)this.crops.get(crop);
         int slotIndex = this.offset * 5 + slot;
         if (list != null && list.size() > slotIndex) {
            ISeedEntry entry = (ISeedEntry)list.get(slotIndex);
            int amount = entry.drain(requested, doDrain);
            if (doDrain) {
               this.syncer.itemCount -= amount;
               if (entry.isEmpty()) {
                  this.syncer.statCount--;
                  list.remove(slotIndex);
                  if (list.isEmpty() && crop != null) {
                     this.crops.remove(crop);
                     this.syncer.cropCount--;
                     if (crop.id() != null) {
                        this.syncer.existingCrops.remove(crop.id());
                     }
                  }

                  if (!(entry instanceof WrapperEntry)) {
                     list = (ObjectList<ISeedEntry>)this.crops.get(null);
                     int i = 0;

                     for (int m = list.size(); i < m; i++) {
                        if (((ISeedEntry)list.get(i)).isEmpty()) {
                           list.remove(i--);
                           m--;
                        }
                     }
                  } else {
                     ICrop subCrop = entry.getCrop(crop);
                     list = (ObjectList<ISeedEntry>)this.crops.get(subCrop);
                     int i = 0;

                     for (int mx = list.size(); i < mx; i++) {
                        if (((ISeedEntry)list.get(i)).isEmpty()) {
                           list.remove(i--);
                           mx--;
                        }
                     }

                     if (list.isEmpty() && subCrop != null) {
                        this.crops.remove(subCrop);
                        this.syncer.cropCount--;
                        if (subCrop.id() != null) {
                           this.syncer.existingCrops.remove(subCrop.id());
                        }
                     }
                  }
               }

               this.sorter.add(crop);
            }

            ItemStack stack = CropSeedItem.createStack(entry.getCrop(crop), entry.getGrowth(), entry.getGain(), entry.getResistance(), 4);
            stack.setCount(amount);
            return stack;
         } else {
            return ItemStack.EMPTY;
         }
      }
   }

   public boolean processSorting(ICrop displayCrop, Comparator<ISeedEntry> comparator) {
      if (!this.sorter.isEmpty() && comparator != null) {
         ICrop toSort = this.sorter.removeFirst();
         ObjectList<ISeedEntry> seeds = (ObjectList<ISeedEntry>)this.crops.get(toSort);
         if (seeds == null) {
            return false;
         } else {
            ObjectArrays.parallelQuickSort((ISeedEntry[])((ObjectArrayList)seeds).elements(), 0, seeds.size(), comparator);
            return toSort == displayCrop;
         }
      } else {
         return false;
      }
   }

   public void updateDisplay(ICrop currentCrop) {
      this.inventory.clear();
      List<ISeedEntry> seeds = (List<ISeedEntry>)this.crops.get(currentCrop);
      if (seeds != null && !seeds.isEmpty() && this.active) {
         int index = this.offset * 5;
         int i = 0;

         for (int m = seeds.size() - index; i < m && i < 25; i++) {
            ISeedEntry entry = seeds.get(i + index);
            ItemStack stack = CropSeedItem.createStack(entry.getCrop(currentCrop), entry.getGrowth(), entry.getGain(), entry.getResistance(), 4);
            CropSeedItem.setVisible(stack, true);
            stack.setCount(Mth.clamp(entry.getCount(), 1, 127));
            stack.addTagElement("crop_count", IntTag.valueOf(entry.getCount()));
            this.inventory.setStackInSlot(i, stack);
         }
      }
   }

   public boolean containsCrop(ICrop crop) {
      return this.crops.get(crop) != null;
   }

   public void read(CompoundTag base) {
      ObjectList<ISeedEntry> nullSeeds = (ObjectList<ISeedEntry>)this.crops.get(null);

      for (CompoundTag data : NBTListWrapper.wrap(base.getList("crops", 10), CompoundTag.class)) {
         ICrop crop = ICropRegistry.INSTANCE.getCrop(new ResourceLocation(data.getString("crop")));
         if (crop != null) {
            ObjectList<ISeedEntry> list = ObjectArrayList.wrap(new ISeedEntry[0]);

            for (LongTag value : NBTListWrapper.wrap(data.getList("types", 4), LongTag.class)) {
               ISeedEntry entry = new SeedEntry(value.getAsLong());
               list.add(entry);
               nullSeeds.add(new WrapperEntry(crop, entry));
            }

            this.crops.put(crop, list);
         }
      }

      this.offset = base.getInt("offset");
      this.syncer.read(base.getCompound("syncer"));
   }

   public CompoundTag write(CompoundTag base) {
      ListTag list = new ListTag();
      ObjectIterator var3 = Object2ObjectMaps.fastIterable(this.crops).iterator();

      while (var3.hasNext()) {
         Entry<ICrop, ObjectList<ISeedEntry>> entry = (Entry<ICrop, ObjectList<ISeedEntry>>)var3.next();
         if (entry.getKey() != null && entry.getValue() != null && !((ObjectList)entry.getValue()).isEmpty()) {
            CompoundTag data = new CompoundTag();
            ListTag types = new ListTag();
            ObjectList<ISeedEntry> entries = (ObjectList<ISeedEntry>)entry.getValue();
            int i = 0;

            for (int m = entries.size(); i < m; i++) {
               types.add(((ISeedEntry)entries.get(i)).write());
            }

            data.putString("crop", ((ICrop)entry.getKey()).id().toString());
            data.put("types", types);
            list.add(data);
         }
      }

      NBTUtils.put(base, "crops", list);
      NBTUtils.putInt(base, "offset", this.offset, 0);
      NBTUtils.put(base, "syncer", this.syncer.write());
      return base;
   }

   public ListTag saveToList() {
      ListTag list = new ListTag();
      ObjectIterator var2 = Object2ObjectMaps.fastIterable(this.crops).iterator();

      while (var2.hasNext()) {
         Entry<ICrop, ObjectList<ISeedEntry>> entry = (Entry<ICrop, ObjectList<ISeedEntry>>)var2.next();
         if (entry.getKey() != null && entry.getValue() != null && !((ObjectList)entry.getValue()).isEmpty()) {
            String id = ((ICrop)entry.getKey()).id().toString();
            ObjectListIterator var5 = ((ObjectList)entry.getValue()).iterator();

            while (var5.hasNext()) {
               ISeedEntry seed = (ISeedEntry)var5.next();
               CompoundTag tag = new CompoundTag();
               tag.putString("id", id);
               tag.put("type", seed.write());
               list.add(tag);
            }
         }
      }

      return list;
   }

   public ListTag loadFromList(ListTag list) {
      if (!this.active) {
         return list;
      } else {
         ListTag result = new ListTag();
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            CompoundTag tag = list.getCompound(i);
            ICrop crop = ICropRegistry.INSTANCE.getCrop(new ResourceLocation(tag.getString("id")));
            if (crop != null) {
               ISeedEntry entry = new SeedEntry(tag.getLong("type"));
               if (!this.addCrop(crop, entry)) {
                  result.add(tag);
               }
            }
         }

         return result;
      }
   }
}
