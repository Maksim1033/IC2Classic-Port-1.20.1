package ic2.core.inventory.transporter;

import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.inventory.filter.IFilter;
import ic2.core.utils.collection.IterableWrapper;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap.BasicEntry;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Iterator;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public interface IItemTransporter {
   int addItem(ItemStack var1, Direction var2, boolean var3);

   ItemStack removeItem(IFilter var1, Direction var2, int var3, boolean var4);

   int getInventorySize(Direction var1);

   Object2IntMap<ItemStack> getAllItems(Direction var1, boolean var2);

   IItemTransporter.InvResult getInventory(Direction var1, boolean var2);

   public static class InvResult {
      Object2IntLinkedOpenCustomHashMap<ItemStack> found;
      Object2LongLinkedOpenCustomHashMap<ItemStack> room;

      public InvResult(boolean nbt) {
         this.found = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(nbt));
         this.room = new Object2LongLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(nbt));
      }

      public Object2IntLinkedOpenCustomHashMap<ItemStack> getFound() {
         return this.found;
      }

      public Object2LongLinkedOpenCustomHashMap<ItemStack> getRoom() {
         return this.room;
      }

      public int getSpace(ItemStack stack) {
         return (int)(this.room.getOrDefault(stack, 2147483647L) - (long)this.found.getInt(stack));
      }

      public long getFree() {
         return this.room.getLong(ItemStack.EMPTY);
      }

      public Iterable<Entry<ItemStack>> getFreeRoomIterator() {
         return IterableWrapper.wrap(new Iterator<Entry<ItemStack>>() {
            ObjectIterator<Entry<ItemStack>> iter = Object2IntMaps.fastIterator(InvResult.this.found);

            @Override
            public boolean hasNext() {
               return this.iter.hasNext();
            }

            public Entry<ItemStack> next() {
               Entry<ItemStack> entry = (Entry<ItemStack>)this.iter.next();
               ItemStack stack = (ItemStack)entry.getKey();
               return new BasicEntry(stack, (int)Math.min(InvResult.this.room.getLong(stack), (long)entry.getIntValue()));
            }
         });
      }

      public void add(IItemTransporter.InvResult result) {
         ObjectIterator var2 = Object2IntMaps.fastIterable(result.getFound()).iterator();

         while (var2.hasNext()) {
            Entry<ItemStack> entry = (Entry<ItemStack>)var2.next();
            this.found.addTo((ItemStack)entry.getKey(), entry.getIntValue());
         }

         var2 = Object2LongMaps.fastIterable(result.getRoom()).iterator();

         while (var2.hasNext()) {
            it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<ItemStack> entry = (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<ItemStack>)var2.next();
            this.room.addTo((ItemStack)entry.getKey(), entry.getLongValue());
         }
      }

      public void add(ItemStack item, int max) {
         if (item.isEmpty()) {
            this.room.put(ItemStack.EMPTY, (long)max);
         } else {
            ItemStack stack = StackUtil.copyWithSize(item, 1);
            this.found.addTo(stack, item.getCount());
            this.room.addTo(stack, (long)max);
         }
      }
   }
}
