package ic2.core.item.base.features;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IScrollableInventory {
   boolean canScrollInventory(ItemStack var1);

   int getScrollIndex(ItemStack var1);

   void setScrollIndex(ItemStack var1, int var2);

   int getSlotCount(ItemStack var1);

   boolean isValidItem(ItemStack var1, ItemStack var2);

   IScrollableInventory.IndexedStack[] getItemsToSwapWith(ItemStack var1, boolean var2);

   boolean swapItems(ItemStack var1, int var2, int var3, ItemStack var4);

   public static class IndexedStack {
      int slot;
      ItemStack stack;

      public IndexedStack(int slot, NonNullList<ItemStack> list) {
         this(slot, (ItemStack)list.get(slot));
      }

      public IndexedStack(int slot, ItemStack stack) {
         this.slot = slot;
         this.stack = stack;
      }

      public int getSlot() {
         return this.slot;
      }

      public ItemStack getStack() {
         return this.stack;
      }
   }
}
