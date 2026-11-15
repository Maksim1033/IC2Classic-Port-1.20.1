package ic2.core.block.machines.recipes;

import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public class ItemStackStrategy implements Strategy<ItemStack> {
   public static final ItemStackStrategy INSTANCE = new ItemStackStrategy();
   public static final ItemStackStrategy.ItemStackSimpleStrategy SIMPLE_INSTANCE = new ItemStackStrategy.ItemStackSimpleStrategy();
   public static final ItemStackStrategy.ItemStackCountStrategy COUNT_INSTANCE = new ItemStackStrategy.ItemStackCountStrategy();

   public static Strategy<ItemStack> getStrategy(boolean nbt) {
      return (Strategy<ItemStack>)(nbt ? INSTANCE : SIMPLE_INSTANCE);
   }

   public int hashCode(ItemStack stack) {
      return stack == null ? 0 : Objects.hash(stack.getItem(), stack.getTag());
   }

   public boolean equals(ItemStack key, ItemStack value) {
      key = key == null ? ItemStack.EMPTY : key;
      value = value == null ? ItemStack.EMPTY : value;
      return StackUtil.isStackEqual(key, value);
   }

   public static class ItemStackCountStrategy implements Strategy<ItemStack> {
      public int hashCode(ItemStack stack) {
         return stack == null ? 0 : Objects.hash(stack.getItem(), stack.getTag(), stack.getCount());
      }

      public boolean equals(ItemStack key, ItemStack value) {
         key = key == null ? ItemStack.EMPTY : key;
         value = value == null ? ItemStack.EMPTY : value;
         return key.getCount() == value.getCount() && StackUtil.isStackEqual(key, value);
      }
   }

   public static class ItemStackSimpleStrategy implements Strategy<ItemStack> {
      public int hashCode(ItemStack o) {
         return o == null ? 0 : o.getItem().hashCode();
      }

      public boolean equals(ItemStack a, ItemStack b) {
         return ItemStack.isSameItem(a == null ? ItemStack.EMPTY : a, b == null ? ItemStack.EMPTY : b);
      }
   }
}
