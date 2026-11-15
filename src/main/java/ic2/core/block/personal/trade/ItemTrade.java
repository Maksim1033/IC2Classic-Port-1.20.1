package ic2.core.block.personal.trade;

import ic2.api.items.ICoinItem;
import ic2.api.items.ItemRegistries;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemTrade extends Trade {
   public SimpleInventory offered = new SimpleInventory(4);

   @Override
   public boolean canDoPerPlayer() {
      return true;
   }

   public boolean hasStock(IItemTransporter transporter, int filter, NonNullList<ItemStack> result) {
      int amount = 0;

      for (int i = 0; i < 4; i++) {
         ItemStack stack = this.offered.getStackInSlot(i);
         if (!stack.isEmpty()) {
            amount++;
            if (this.infinite) {
               result.add(stack.copy());
            } else if (stack.getItem() instanceof ICoinItem) {
               if (!this.extractCoinValue(transporter, this.getCoinValue(stack), result)) {
                  return false;
               }
            } else {
               ItemStack fetched = transporter.removeItem(new StackFilter(stack, filter), null, stack.getCount(), false);
               if (fetched.getCount() < stack.getCount()) {
                  return false;
               }

               result.add(fetched);
            }
         }
      }

      return amount > 0;
   }

   private int getCoinValue(ItemStack stack) {
      return stack.getItem() instanceof ICoinItem item ? item.getMoneyValue(stack) : 0;
   }

   public boolean extractCoinValue(IItemTransporter transporter, int coinValue, NonNullList<ItemStack> extract) {
      Object2IntMap<ItemStack> map = transporter.getAllItems(null, false);
      ItemStack filter = ItemStack.EMPTY;
      int count = 0;
      ObjectIterator iterations = map.object2IntEntrySet().iterator();

      while (iterations.hasNext()) {
         Entry<ItemStack> entry = (Entry<ItemStack>)iterations.next();
         ItemStack stack = (ItemStack)entry.getKey();
         Item m = stack.getItem();
         if (m instanceof ICoinItem) {
            ICoinItem coin = (ICoinItem)m;
            int amount = entry.getIntValue();
            int value = coin.getMoneyValue(stack);
            if (value > 0) {
               int toExtract = Math.max(1, Mth.ceil((float)coinValue / (float)value));
               if (value * amount >= coinValue && (filter.isEmpty() || count > toExtract)) {
                  filter = stack;
                  count = toExtract;
               }
            }
         }
      }

      if (count <= 0) {
         return false;
      } else {
         int iterationsx = 0;

         int totalExtracted;
         for (totalExtracted = 0; count > 0 && iterationsx < 100; iterationsx++) {
            ItemStack extracted = transporter.removeItem(StackFilter.defaultCompare(filter), null, count, false);
            int amount = this.getCoinValue(extracted);
            count -= extracted.getCount();
            totalExtracted += amount * extracted.getCount();
         }

         if (totalExtracted > coinValue) {
            NonNullList<ItemStack> result = ItemRegistries.generateCoins(totalExtracted - coinValue, NonNullList.create(), false);
            int i = 0;

            for (int m = result.size(); i < m; i++) {
               transporter.addItem((ItemStack)result.get(i), null, false);
            }
         }

         ItemRegistries.generateCoins(coinValue, extract, false);
         return count <= 0;
      }
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      super.save(nbt);
      CompoundTag data = this.offered.save(new CompoundTag());
      if (!data.isEmpty()) {
         nbt.put("offered", data);
      }

      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt);
      this.offered.load(nbt.getCompound("offered"));
   }
}
