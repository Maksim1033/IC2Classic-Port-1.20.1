package ic2.core.utils.helpers;

import ic2.api.items.ITagItem;
import ic2.core.inventory.filter.IFilter;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class StackUtil {
   public static final int EMPTY_FLAG = 1;
   public static final int EMPTY_KEY = 2;
   public static final int ITEM_FLAG = 4;
   public static final int NBT_FUZZY_FLAG = 8;
   public static final int NBT_EXACT_FLAG = 16;
   public static final int TAG_FLAG = 32;
   public static final int DURABILITY_FLAG = 64;
   public static final int FLUID_FLAG = 128;
   public static final int IGNORE_DURABILITY_NBT = 256;
   public static final int DEFAULT_COMPARE = 20;
   public static final int EMPTY_OR_DEFAULT = 22;
   private static final String DAMAGE_TAG = "Damage";

   public static void addOrPop(Player player, BlockPos pos, ItemStack stack) {
      if (!stack.isEmpty()) {
         if (!player.addItem(stack)) {
            Block.popResource(player.level(), pos, stack);
         } else {
            player.containerMenu.broadcastChanges();
         }
      }
   }

   public static void pop(Level level, BlockPos pos, ItemStack stack) {
      List<ItemStack> result = new ObjectArrayList();
      expand(stack, stack.getCount(), result);

      for (ItemStack entry : result) {
         Block.popResource(level, pos, entry);
      }
   }

   public static void addOrDrop(Player player, ItemStack stack) {
      if (!stack.isEmpty()) {
         if (!player.addItem(stack)) {
            player.drop(stack, false);
         } else {
            player.containerMenu.broadcastChanges();
         }
      }
   }

   public static CompoundTag getNbtData(ItemStack stack) {
      return !stack.hasTag() ? new CompoundTag() : stack.getTag();
   }

   public static void addTooltip(ItemStack stack, Component s) {
      CompoundTag nbt = stack.getOrCreateTagElement("display");
      ListTag list = nbt.getList("Lore", 8);
      list.add(StringTag.valueOf(Serializer.toJson(s)));
      nbt.put("Lore", list);
   }

   public static void addTooltip(ItemStack stack, String s) {
      CompoundTag nbt = stack.getOrCreateTagElement("display");
      ListTag list = nbt.getList("Lore", 8);
      list.add(StringTag.valueOf(Serializer.toJson(Component.literal(s))));
      nbt.put("Lore", list);
   }

   public static int getStackSizeLeft(ItemStack stack) {
      return stack.getMaxStackSize() - stack.getCount();
   }

   public static ItemStack copyWithSize(ItemStack stack, int newSize) {
      ItemStack copy = stack.copy();
      copy.setCount(newSize);
      return copy;
   }

   public static List<ItemStack> copyList(List<ItemStack> list) {
      List<ItemStack> result = new ObjectArrayList(list.size());

      for (ItemStack stack : list) {
         result.add(stack.copy());
      }

      return result;
   }

   public static List<ItemStack> copyNonEmpty(List<ItemStack> list) {
      List<ItemStack> result = new ObjectArrayList(list.size());

      for (ItemStack stack : list) {
         if (!stack.isEmpty()) {
            result.add(stack.copy());
         }
      }

      return result;
   }

   public static boolean isNBTEqual(CompoundTag subject, CompoundTag target, boolean durability) {
      if (subject != null && target != null) {
         for (String key : subject.getAllKeys()) {
            if (!durability || !key.equals("Damage")) {
               Tag subjectNBT = subject.get(key);
               Tag targetNBT = target.get(key);
               if (subjectNBT == null || targetNBT == null || subjectNBT.getId() != targetNBT.getId() || !subjectNBT.equals(targetNBT)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return target == null || target.isEmpty();
      }
   }

   public static boolean isNBTExact(ItemStack subject, ItemStack target, boolean durability) {
      boolean key = subject.hasTag();
      boolean value = target.hasTag();
      if (!key && !value) {
         return true;
      } else if (key && value) {
         CompoundTag keyTag = subject.getTag();
         CompoundTag valueTag = target.getTag();
         if (keyTag.size() != valueTag.size()) {
            return false;
         } else {
            for (String entry : keyTag.getAllKeys()) {
               if (!durability || !entry.equals("Damage")) {
                  Tag subjectNBT = keyTag.get(entry);
                  Tag targetNBT = valueTag.get(entry);
                  if (subjectNBT == null || targetNBT == null || subjectNBT.getId() != targetNBT.getId() || !subjectNBT.equals(targetNBT)) {
                     return false;
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isTagEqual(ItemStack key, ItemStack other) {
      if (key.isEmpty() && other.isEmpty()) {
         return true;
      } else {
         return key.getItem() instanceof ITagItem ? ((ITagItem)key.getItem()).matches(key, other) : false;
      }
   }

   public static boolean canFitInto(ItemStack key, ItemStack value) {
      return isStackEqual(key, value) && getStackSizeLeft(key) >= value.getCount();
   }

   public static boolean canFitInto(ItemStack key, ItemStack value, int flags) {
      return isStackEqual(key, value, flags) && getStackSizeLeft(key) >= value.getCount();
   }

   public static boolean isFluidEqual(ItemStack key, ItemStack other) {
      return isFluidEqual(key, other, true);
   }

   public static boolean isFluidEqual(ItemStack key, ItemStack other, boolean allowEmpty) {
      FluidStack first = FluidUtil.getFluidContained(key).orElse(FluidStack.EMPTY);
      FluidStack second = FluidUtil.getFluidContained(other).orElse(FluidStack.EMPTY);
      return first.isEmpty() && second.isEmpty() ? allowEmpty : first.isFluidEqual(second);
   }

   public static boolean isStackEqual(ItemStack key, ItemStack other) {
      return ItemStack.isSameItem(key, other) && isNBTExact(key, other, false);
   }

   public static boolean isStackEqual(ItemStack key, ItemStack other, int flags) {
      boolean keyEmpty = key.isEmpty();
      boolean otherEmpty = other.isEmpty();
      if (keyEmpty && otherEmpty) {
         return (flags & 1) != 0;
      } else if (!keyEmpty && !otherEmpty) {
         if ((flags & 4) != 0 && !ItemStack.isSameItem(key, other)) {
            return (flags & 32) != 0 && isTagEqual(key, other) ? true : (flags & 128) != 0 && isFluidEqual(key, other, (flags & 1) != 0);
         } else {
            if ((flags & 4) == 0) {
               if ((flags & 32) != 0 && isTagEqual(key, other)) {
                  return true;
               }

               if ((flags & 128) != 0 && isFluidEqual(key, other, (flags & 1) != 0)) {
                  return true;
               }
            }

            if ((flags & 64) != 0 && key.getDamageValue() != other.getDamageValue()) {
               return false;
            } else {
               return (flags & 8) != 0 && !isNBTEqual(key.getTag(), other.getTag(), (flags & 256) != 0)
                  ? false
                  : (flags & 16) == 0 || isNBTExact(key, other, (flags & 256) != 0);
            }
         }
      } else {
         return key.isEmpty() && (flags & 2) != 0;
      }
   }

   public static void expand(ItemStack stack, int count, List<ItemStack> result) {
      while (count > 0) {
         int max = Math.min(count, stack.getMaxStackSize());
         result.add(copyWithSize(stack, max));
         count -= max;
      }
   }

   public static boolean containsItemStack(List<ItemStack> stacks, ItemStack stack) {
      int i = 0;

      for (int m = stacks.size(); i < m; i++) {
         if (isStackEqual(stack, stacks.get(i))) {
            return true;
         }
      }

      return false;
   }

   public static boolean isInventoryEmpty(NonNullList<ItemStack> inventory) {
      boolean empty = true;

      for (int i = 0; i < inventory.size(); i++) {
         if (!((ItemStack)inventory.get(i)).isEmpty()) {
            empty = false;
            break;
         }
      }

      return empty;
   }

   public static int countItems(List<ItemStack> items) {
      int count = 0;
      int i = 0;

      for (int m = items.size(); i < m; i++) {
         count += items.get(i).getCount();
      }

      return count;
   }

   public static List<ItemStack> nonAir(List<ItemStack> list) {
      List<ItemStack> items = CollectionUtils.<ItemStack>createList();

      for (ItemStack stack : list) {
         if (!stack.isEmpty()) {
            items.add(stack);
         }
      }

      return items;
   }

   public static int hasItems(NonNullList<ItemStack> items, IFilter... filters) {
      if (filters.length <= 30 && items != null && !items.isEmpty()) {
         int result = 0;
         int x = 0;

         for (int m = filters.length; x < m; x++) {
            int i = 0;

            for (int n = items.size(); i < n; i++) {
               if (filters[x].matches((ItemStack)items.get(i))) {
                  result |= 1 << x;
                  break;
               }
            }
         }

         return result;
      } else {
         return 0;
      }
   }

   public static boolean hasHotbarItems(Player player, IFilter filter) {
      if (player == null) {
         return false;
      } else {
         Inventory inv = player.getInventory();

         for (int i = 0; i < 9; i++) {
            if (filter.matches(inv.getItem(i))) {
               return true;
            }
         }

         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (filter.matches(player.getItemBySlot(slot))) {
               return true;
            }
         }

         return false;
      }
   }

   public static int hasHotbarItems(Player player, IFilter... filters) {
      if (filters.length <= 30 && player != null) {
         List<ItemStack> items = CollectionUtils.<ItemStack>createList();

         for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
               items.add(stack);
            }
         }

         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot != EquipmentSlot.MAINHAND) {
               ItemStack stack = player.getItemBySlot(slot);
               if (!stack.isEmpty()) {
                  items.add(stack);
               }
            }
         }

         if (items.isEmpty()) {
            return 0;
         } else {
            int result = 0;
            int x = 0;

            for (int m = filters.length; x < m; x++) {
               int ix = 0;

               for (int n = items.size(); ix < n; ix++) {
                  if (filters[x].matches(items.get(ix))) {
                     result |= 1 << x;
                     break;
                  }
               }
            }

            return result;
         }
      } else {
         return 0;
      }
   }
}
