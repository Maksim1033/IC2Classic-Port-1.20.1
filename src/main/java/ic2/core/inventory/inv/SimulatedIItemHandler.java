package ic2.core.inventory.inv;

import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.transporters.ItemHandlerTransporter;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class SimulatedIItemHandler implements IItemHandler {
   IItemHandler handler;
   Direction dir;
   NonNullList<ItemStack> inv;
   int[] baseStackSize;

   public SimulatedIItemHandler(IItemHandler handler, Direction dir) {
      this.dir = dir;
      this.handler = handler;
      this.inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
      this.baseStackSize = new int[this.inv.size()];

      for (int i = 0; i < this.inv.size(); i++) {
         ItemStack stack = handler.getStackInSlot(i).copy();
         this.baseStackSize[i] = stack.getCount();
         this.inv.set(i, stack);
      }
   }

   public static IItemHandler createSimulation(IItemHandler handler, Direction dir, Object2IntMap<ItemStack> preload) {
      return handler == null ? null : new SimulatedIItemHandler(handler, dir).preload(preload);
   }

   public IItemHandler preload(Object2IntMap<ItemStack> preload) {
      IItemTransporter transporter = new ItemHandlerTransporter(this);
      ObjectIterator var3 = Object2IntMaps.fastIterable(preload).iterator();

      while (var3.hasNext()) {
         Entry<ItemStack> entry = (Entry<ItemStack>)var3.next();
         ItemStack stack = (ItemStack)entry.getKey();
         if (!stack.isEmpty()) {
            transporter.addItem(StackUtil.copyWithSize(stack, entry.getIntValue()), this.dir, false);
         }
      }

      return this;
   }

   public int getSlots() {
      return this.inv.size();
   }

   public ItemStack getStackInSlot(int slot) {
      return (ItemStack)this.inv.get(slot);
   }

   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else if (!this.isItemValid(slot, stack)) {
         return stack;
      } else {
         int limit = this.getSlotLimit(slot);
         int added = limit - this.handler.insertItem(slot, StackUtil.copyWithSize(stack, limit), true).getCount();
         int alreadyAdded = ((ItemStack)this.inv.get(slot)).getCount() - this.baseStackSize[slot];
         int left = Math.min(added - alreadyAdded, stack.getCount());
         return left <= 0 ? stack : this.insert(slot, StackUtil.copyWithSize(stack, Math.max(left, stack.getCount())), false);
      }
   }

   public ItemStack insert(int slot, ItemStack stack, boolean simulate) {
      ItemStack existing = (ItemStack)this.inv.get(slot);
      int limit = this.getSlotLimit(slot);
      if (!existing.isEmpty()) {
         if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
            return stack;
         }

         limit -= existing.getCount();
      }

      if (limit <= 0) {
         return stack;
      } else {
         boolean reachedLimit = stack.getCount() > limit;
         if (!simulate) {
            if (existing.isEmpty()) {
               this.inv.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
               existing.grow(reachedLimit ? limit : stack.getCount());
            }
         }

         return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
      }
   }

   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (amount != 0 && slot >= 0 && slot < this.inv.size()) {
         ItemStack existing = (ItemStack)this.inv.get(slot);
         if (existing.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            int toExtract = Math.min(amount, existing.getMaxStackSize());
            if (existing.getCount() <= toExtract) {
               if (!simulate) {
                  this.inv.set(slot, ItemStack.EMPTY);
                  return existing;
               } else {
                  return existing.copy();
               }
            } else {
               if (!simulate) {
                  this.inv.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
               }

               return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
            }
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   protected int getStackLimit(int slot, ItemStack stack) {
      return this.getSlotLimit(slot);
   }

   public int getSlotLimit(int slot) {
      return this.handler.getSlotLimit(slot);
   }

   public boolean isItemValid(int slot, ItemStack stack) {
      return this.handler.isItemValid(slot, stack);
   }
}
