package ic2.core.utils.helpers;

import com.google.common.collect.UnmodifiableIterator;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.tooltips.TextHelper;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class NBTUtils {
   public static void putBoolean(CompoundTag data, String id, boolean value, boolean defaultValue) {
      if (value == defaultValue) {
         data.remove(id);
      } else {
         data.putBoolean(id, value);
      }
   }

   public static void putByte(CompoundTag data, String id, byte value, byte defaultValue) {
      if (value == defaultValue) {
         data.remove(id);
      } else {
         data.putByte(id, value);
      }
   }

   public static void putByte(CompoundTag data, String id, int value, int defaultValue) {
      if (value == defaultValue) {
         data.remove(id);
      } else {
         data.putByte(id, (byte)value);
      }
   }

   public static void putShort(CompoundTag data, String id, short value, short defaultValue) {
      if (value == defaultValue) {
         data.remove(id);
      } else {
         data.putShort(id, value);
      }
   }

   public static void putShort(CompoundTag data, String id, int value, int defaultValue) {
      if (value == defaultValue) {
         data.remove(id);
      } else {
         data.putShort(id, (short)value);
      }
   }

   public static void putInt(CompoundTag data, String id, int value, int defaultValue) {
      if (value == defaultValue) {
         data.remove(id);
      } else {
         data.putInt(id, value);
      }
   }

   public static void putLong(CompoundTag data, String id, long value, long defaultValue) {
      if (value == defaultValue) {
         data.remove(id);
      } else {
         data.putLong(id, value);
      }
   }

   public static void putFloat(CompoundTag data, String id, float value, float defaultValue) {
      if (Float.floatToIntBits(value) == Float.floatToIntBits(defaultValue)) {
         data.remove(id);
      } else {
         data.putFloat(id, value);
      }
   }

   public static void putDouble(CompoundTag data, String id, double value, double defaultValue) {
      if (Double.doubleToLongBits(value) == Double.doubleToLongBits(defaultValue)) {
         data.remove(id);
      } else {
         data.putDouble(id, value);
      }
   }

   public static void putString(CompoundTag data, String id, String value, String defaultValue) {
      if (Objects.equals(value, defaultValue)) {
         data.remove(id);
      } else {
         data.putString(id, value);
      }
   }

   public static <T extends Enum<T>> void putEnum(CompoundTag data, String id, @Nullable T value) {
      int index = value == null ? 0 : value.ordinal() + 1;
      if (index == 0) {
         data.remove(id);
      } else {
         if (index < 127) {
            data.putByte(id, (byte)index);
         } else if (index < 32767) {
            data.putShort(id, (short)index);
         } else {
            data.putInt(id, index);
         }
      }
   }

   public static <T extends Enum<T>> void putEnum(CompoundTag data, String id, @Nonnull T value, @Nonnull T defaultValue) {
      if (value != defaultValue && value != null) {
         int index = value.ordinal() + 1;
         if (index < 127) {
            data.putByte(id, (byte)index);
         } else if (index < 32767) {
            data.putShort(id, (short)index);
         } else {
            data.putInt(id, index);
         }
      } else {
         data.remove(id);
      }
   }

   public static <T> void putForgeEntry(CompoundTag data, String id, T value, T defaultValue, IForgeRegistry<T> registry) {
      if (value == defaultValue) {
         data.remove(id);
      } else {
         data.putString(id, registry.getKey(value).toString());
      }
   }

   public static void putByteArray(CompoundTag data, String id, byte[] value) {
      if (value.length == 0) {
         data.remove(id);
      } else {
         data.putByteArray(id, value);
      }
   }

   public static void putByteArray(CompoundTag data, String id, ByteCollection value) {
      if (value.isEmpty()) {
         data.remove(id);
      } else {
         data.putByteArray(id, value.toByteArray());
      }
   }

   public static void putByteArray(CompoundTag data, String id, byte[] value, byte[] defaultValue) {
      if (Arrays.equals(value, defaultValue)) {
         data.remove(id);
      } else {
         data.putByteArray(id, value);
      }
   }

   public static void putIntArray(CompoundTag data, String id, int[] value) {
      if (value.length == 0) {
         data.remove(id);
      } else {
         data.putIntArray(id, value);
      }
   }

   public static void putIntArray(CompoundTag data, String id, IntCollection value) {
      if (value.isEmpty()) {
         data.remove(id);
      } else {
         data.putIntArray(id, value.toIntArray());
      }
   }

   public static void putIntArray(CompoundTag data, String id, int[] value, int[] defaultValue) {
      if (Arrays.equals(value, defaultValue)) {
         data.remove(id);
      } else {
         data.putIntArray(id, value);
      }
   }

   public static void putSortedIntArray(CompoundTag data, String id, int[] value, int[] defaultValue) {
      IntArrays.quickSort(value);
      if (Arrays.equals(value, defaultValue)) {
         data.remove(id);
      } else {
         data.putIntArray(id, value);
      }
   }

   public static void putLongArray(CompoundTag data, String id, long[] value) {
      if (value.length == 0) {
         data.remove(id);
      } else {
         data.putLongArray(id, value);
      }
   }

   public static void putLongArray(CompoundTag data, String id, LongCollection value) {
      if (value.isEmpty()) {
         data.remove(id);
      } else {
         data.putLongArray(id, value.toLongArray());
      }
   }

   public static void putLongArray(CompoundTag data, String id, long[] value, long[] defaultValue) {
      if (Arrays.equals(value, defaultValue)) {
         data.remove(id);
      } else {
         data.putLongArray(id, value);
      }
   }

   public static void put(CompoundTag data, String id, CompoundTag value) {
      if (value.isEmpty()) {
         data.remove(id);
      } else {
         data.put(id, value);
      }
   }

   public static void put(CompoundTag data, String id, ListTag value) {
      if (value.isEmpty()) {
         data.remove(id);
      } else {
         data.put(id, value);
      }
   }

   public static void putIf(CompoundTag data, String id, CompoundTag value, boolean add) {
      if (!value.isEmpty() && add) {
         data.put(id, value);
      } else {
         data.remove(id);
      }
   }

   public static void putIf(CompoundTag data, String id, ListTag value, boolean add) {
      if (!value.isEmpty() && add) {
         data.put(id, value);
      } else {
         data.remove(id);
      }
   }

   public static void add(ListTag data, Tag value) {
      if (!(value instanceof CompoundTag) || !((CompoundTag)value).isEmpty()) {
         if (!(value instanceof ListTag) || !((ListTag)value).isEmpty()) {
            data.add(value);
         }
      }
   }

   public static boolean getBoolean(CompoundTag data, String id, boolean defaultValue) {
      return getByte(data, id, (byte)(defaultValue ? 1 : 0)) == 1;
   }

   public static byte getByte(CompoundTag data, String id, byte defaultValue) {
      Tag value = data.get(id);
      return value instanceof NumericTag ? ((NumericTag)value).getAsByte() : defaultValue;
   }

   public static short getShort(CompoundTag data, String id, short defaultValue) {
      Tag value = data.get(id);
      return value instanceof NumericTag ? ((NumericTag)value).getAsShort() : defaultValue;
   }

   public static int getInt(CompoundTag data, String id, int defaultValue) {
      Tag value = data.get(id);
      return value instanceof NumericTag ? ((NumericTag)value).getAsInt() : defaultValue;
   }

   public static long getLong(CompoundTag data, String id, long defaultValue) {
      Tag value = data.get(id);
      return value instanceof NumericTag ? ((NumericTag)value).getAsLong() : defaultValue;
   }

   public static float getFloat(CompoundTag data, String id, float defaultValue) {
      Tag value = data.get(id);
      return value instanceof NumericTag ? ((NumericTag)value).getAsFloat() : defaultValue;
   }

   public static double getDouble(CompoundTag data, String id, double defaultValue) {
      Tag value = data.get(id);
      return value instanceof NumericTag ? ((NumericTag)value).getAsDouble() : defaultValue;
   }

   public static String getString(CompoundTag data, String id, String defaultValue) {
      Tag value = data.get(id);
      return value instanceof StringTag ? value.getAsString() : defaultValue;
   }

   public static <T extends Enum<T>> T getEnum(CompoundTag data, String id, Class<T> clz) {
      int value = data.getInt(id);
      return value == 0 ? null : clz.getEnumConstants()[value - 1];
   }

   public static <T extends Enum<T>> T getEnum(CompoundTag data, String id, Class<T> clz, T defaultValue) {
      int value = data.getInt(id);
      return value == 0 ? defaultValue : clz.getEnumConstants()[value - 1];
   }

   public static <T> T getForgeEntry(CompoundTag data, String id, IForgeRegistry<T> registry, T defaultValue) {
      Tag value = data.get(id);
      return (T)(value instanceof StringTag ? registry.getValue(ResourceLocation.tryParse(value.getAsString())) : defaultValue);
   }

   public static byte[] getByteArray(CompoundTag data, String id, byte[] defaultValue) {
      Tag value = data.get(id);
      return value instanceof ByteArrayTag ? ((ByteArrayTag)value).getAsByteArray() : defaultValue;
   }

   public static int[] getIntArray(CompoundTag data, String id, int[] defaultValue) {
      Tag value = data.get(id);
      return value instanceof IntArrayTag ? ((IntArrayTag)value).getAsIntArray() : defaultValue;
   }

   public static long[] getLongArray(CompoundTag data, String id, long[] defaultValue) {
      Tag value = data.get(id);
      return value instanceof LongArrayTag ? ((LongArrayTag)value).getAsLongArray() : defaultValue;
   }

   public static void removeIfEmpty(CompoundTag data, String subTag) {
      Tag nbt = data.get(subTag);
      if (nbt != null) {
         if (nbt instanceof CompoundTag subNBT) {
            if (subNBT.size() == 1) {
               removeIfEmpty(subNBT, (String)subNBT.getAllKeys().iterator().next());
            }

            if (subNBT.isEmpty()) {
               data.remove(subTag);
            }
         } else if (nbt instanceof ListTag && ((ListTag)nbt).isEmpty()) {
            data.remove(subTag);
         }
      }
   }

   public static void saveItem(CompoundTag data, String id, ItemStack stack) {
      putIf(data, id, stack.save(new CompoundTag()), !stack.isEmpty());
   }

   public static void saveItems(CompoundTag data, NonNullList<ItemStack> inv) {
      saveItems(data, "items", inv);
   }

   public static void saveItems(CompoundTag data, String id, NonNullList<ItemStack> inv) {
      Object2ObjectMap<ItemStack, ByteList> mappedSlots = CollectionUtils.<ItemStack, ByteList>createLinkedMap(ItemStackStrategy.COUNT_INSTANCE);
      int i = 0;

      for (int m = inv.size(); i < m; i++) {
         ItemStack stack = (ItemStack)inv.get(i);
         if (!stack.isEmpty()) {
            ByteList items = (ByteList)mappedSlots.get(stack);
            if (items == null) {
               items = new ByteArrayList();
               mappedSlots.put(stack, items);
            }

            items.add((byte)i);
         }
      }

      ListTag list = new ListTag();
      ObjectIterator var10 = Object2ObjectMaps.fastIterable(mappedSlots).iterator();

      while (var10.hasNext()) {
         Entry<ItemStack, ByteList> entry = (Entry<ItemStack, ByteList>)var10.next();
         CompoundTag nbt = ((ItemStack)entry.getKey()).save(new CompoundTag());
         ByteList slots = (ByteList)entry.getValue();
         if (slots.size() > 1) {
            nbt.putByteArray("slots", slots.toByteArray());
         } else {
            if (slots.size() != 1) {
               continue;
            }

            nbt.putByte("slot", slots.getByte(0));
         }

         list.add(nbt);
      }

      put(data, id, list);
   }

   public static ItemStack loadItem(CompoundTag data, String id, ItemStack defaultValue) {
      Tag value = data.get(id);
      return value instanceof CompoundTag ? ItemStack.of((CompoundTag)value) : defaultValue;
   }

   public static void loadItems(CompoundTag data, NonNullList<ItemStack> inv) {
      loadItems(data, "items", inv);
   }

   public static void loadItems(CompoundTag data, String id, NonNullList<ItemStack> inv) {
      ListTag items = data.getList(id, 10);
      int max = inv.size();
      int i = 0;

      for (int m = items.size(); i < m; i++) {
         CompoundTag nbt = (CompoundTag)items.get(i);
         if (nbt.contains("slot")) {
            int slot = nbt.getInt("slot");
            if (slot >= 0 && slot < max) {
               inv.set(slot, ItemStack.of(nbt));
            }
         } else {
            ItemStack stack = ItemStack.of(nbt);
            byte[] slots = nbt.getByteArray("slots");

            for (int slot : slots) {
               if (slot >= 0 && slot < max) {
                  inv.set(slot, stack.copy());
               }
            }
         }
      }
   }

   public static NonNullList<ItemStack> getItems(ItemStack stack) {
      return getItems(stack, "items");
   }

   public static NonNullList<ItemStack> getItems(ItemStack stack, String tag) {
      NonNullList<ItemStack> list = NonNullList.create();
      CompoundTag data = StackUtil.getNbtData(stack);
      ListTag items = data.getList(tag, 10);
      int i = 0;

      for (int m = items.size(); i < m; i++) {
         CompoundTag nbt = (CompoundTag)items.get(i);
         if (nbt.contains("slot")) {
            ItemStack item = ItemStack.of(nbt);
            if (!item.isEmpty()) {
               list.add(item);
            }
         } else {
            ItemStack item = ItemStack.of(nbt);
            if (!item.isEmpty()) {
               int j = 0;

               for (int n = nbt.getByteArray("slots").length; j < n; j++) {
                  list.add(item.copy());
               }
            }
         }
      }

      return list;
   }

   public static List<Component> convertState(BlockState state) {
      List<Component> list = new ObjectArrayList();
      UnmodifiableIterator var2 = state.getValues().entrySet().iterator();

      while (var2.hasNext()) {
         java.util.Map.Entry<Property<?>, Comparable<?>> entry = (java.util.Map.Entry<Property<?>, Comparable<?>>)var2.next();
         list.add(Component.literal("[" + entry.getKey().getName() + ": " + Util.getPropertyName(entry.getKey(), entry.getValue()) + "]"));
      }

      return list;
   }

   public static List<Component> convertNBT(CompoundTag nbt) {
      List<Component> list = new ArrayList<>();
      Logger logger = (Logger)LogManager.getLogger(CompoundTag.class);
      Level before = logger.getLevel();
      logger.setLevel(Level.DEBUG);
      int layer = 0;

      for (String s : TextHelper.convertText(new TextComponentTagVisitor("\n", 0).visit(nbt)).split("\n")) {
         s = s.trim();
         if (!StringUtil.isNullOrEmpty(s)) {
            if (isCloser(s)) {
               layer--;
            }

            list.add(Component.literal(getBuffer(layer, s)));
            if (isOpener(s)) {
               layer++;
            }
         }
      }

      logger.setLevel(before);
      return list;
   }

   static boolean isOpener(String s) {
      return s.startsWith("{") || s.startsWith("[") || s.endsWith("[") || s.endsWith("{");
   }

   static boolean isCloser(String s) {
      return s.startsWith("}") || s.startsWith("]");
   }

   static String getBuffer(int layer, String s) {
      return "    ".repeat(Math.max(0, layer)) + s;
   }
}
