package ic2.core.inventory.handler;

import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.handler.filter.ISlotFilter;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasicInventory implements IItemHandler {
   InventoryHandler handler;
   Direction dir;
   IModularSlot slots;

   public BasicInventory(InventoryHandler handler, Direction dir, IModularSlot slots) {
      this.handler = handler;
      this.dir = dir;
      this.slots = slots;
   }

   public int getSlots() {
      return this.slots.getSlotCount();
   }

   public ItemStack getStackInSlot(int slot) {
      return this.handler.getInventory().getStackInSlot(this.slots.getRealSlot(slot));
   }

   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         int realSlot = this.slots.getRealSlot(slot);
         if (!this.handler.getSlotAccess(realSlot, this.dir).canImport()) {
            return stack;
         } else {
            ISlotFilter filter = this.handler.getInputFilter(realSlot);
            if (filter != null && !filter.matches(realSlot, stack)) {
               return stack;
            } else {
               IHasInventory inv = this.handler.getInventory();
               int maxStack = 2147483647;
               if (this.handler.isNoDuplicateMode()) {
                  if (!this.canInsert(realSlot, stack)) {
                     return stack;
                  }

                  maxStack = this.getRoom(stack);
                  if (maxStack <= 0) {
                     return stack;
                  }
               }

               ItemStack invStack = inv.getStackInSlot(realSlot);
               if (invStack.isEmpty()) {
                  int max = Math.min(Math.min(stack.getMaxStackSize(), inv.getMaxStackSize(realSlot)), maxStack);
                  if (stack.getCount() <= max) {
                     ItemStack copy = stack.copy();
                     if (!simulate) {
                        inv.setStackInSlot(realSlot, copy);
                     }

                     return ItemStack.EMPTY;
                  } else {
                     if (!simulate) {
                        inv.setStackInSlot(realSlot, StackUtil.copyWithSize(stack, max));
                     }

                     return StackUtil.copyWithSize(stack, stack.getCount() - max);
                  }
               } else if (!ItemHandlerHelper.canItemStacksStack(stack, invStack)) {
                  return stack;
               } else {
                  int toAdd = Math.min(Math.min(stack.getMaxStackSize(), inv.getMaxStackSize(realSlot)) - invStack.getCount(), maxStack);
                  if (toAdd <= 0) {
                     return stack;
                  } else if (toAdd > stack.getCount()) {
                     if (!simulate) {
                        ItemStack copy = invStack.copy();
                        copy.grow(stack.getCount());
                        inv.setStackInSlot(realSlot, copy);
                     }

                     return ItemStack.EMPTY;
                  } else {
                     ItemStack copy = stack.copy();
                     if (simulate) {
                        copy.shrink(toAdd);
                        return copy;
                     } else {
                        copy.shrink(toAdd);
                        ItemStack realCopy = invStack.copy();
                        realCopy.grow(toAdd);
                        inv.setStackInSlot(realSlot, realCopy);
                        return copy;
                     }
                  }
               }
            }
         }
      }
   }

   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (amount <= 0) {
         return ItemStack.EMPTY;
      } else {
         int realSlot = this.slots.getRealSlot(slot);
         if (!this.handler.getSlotAccess(realSlot, this.dir).canExport()) {
            return ItemStack.EMPTY;
         } else {
            IHasInventory inv = this.handler.getInventory();
            ItemStack stack = inv.getStackInSlot(realSlot);
            if (stack.isEmpty()) {
               return ItemStack.EMPTY;
            } else {
               ISlotFilter exportFilter = this.handler.getOutputFilter(realSlot);
               if (exportFilter != null && !exportFilter.matches(realSlot, stack)) {
                  return ItemStack.EMPTY;
               } else {
                  ItemStack split;
                  if (simulate) {
                     split = stack.copy();
                     split.setCount(Math.min(stack.getCount(), amount));
                  } else {
                     split = stack.split(amount);
                     inv.setStackInSlot(realSlot, stack);
                  }

                  return split;
               }
            }
         }
      }
   }

   public int getSlotLimit(int slot) {
      return this.handler.getInventory().getMaxStackSize(this.slots.getRealSlot(slot));
   }

   public boolean isItemValid(int slot, ItemStack stack) {
      int realSlot = this.slots.getRealSlot(slot);
      ISlotFilter filter = this.handler.getInputFilter(realSlot);
      return filter == null || filter.matches(realSlot, stack);
   }

   protected boolean canInsert(int mySlot, ItemStack item) {
      IHasInventory inv = this.handler.getInventory();
      ItemStack myStack = inv.getStackInSlot(mySlot);
      if (!myStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(myStack, item)) {
         return false;
      } else {
         int count = 0;
         boolean allowStack = this.handler.isMultiStack();
         Item toInsert = item.getItem();
         int i = 0;

         for (int m = this.slots.getSlotCount(); i < m; i++) {
            int slot = this.slots.getRealSlot(i);
            if (slot != mySlot || allowStack) {
               ItemStack stack = inv.getStackInSlot(slot);
               if (stack.getItem() == toInsert) {
                  count += stack.getCount();
                  if (!allowStack) {
                     return false;
                  }
               }
            }
         }

         return !allowStack || count < this.getLimit(item);
      }
   }

   protected int getRoom(ItemStack toInsert) {
      int count = 0;
      IHasInventory inv = this.handler.getInventory();
      int i = 0;

      for (int m = this.slots.getSlotCount(); i < m; i++) {
         ItemStack stack = inv.getStackInSlot(this.slots.getRealSlot(i));
         if (StackUtil.isStackEqual(stack, toInsert)) {
            count += stack.getCount();
         }
      }

      return this.getLimit(toInsert) - count;
   }

   private int getLimit(ItemStack stack) {
      int value = this.handler.getMaxStackSize(stack.getItem());
      return value == -1 ? stack.getMaxStackSize() : value;
   }
}
