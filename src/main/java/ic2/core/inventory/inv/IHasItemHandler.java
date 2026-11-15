package ic2.core.inventory.inv;

import ic2.core.inventory.base.IHasInventory;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class IHasItemHandler implements IItemHandler {
   IHasInventory inv;

   public IHasItemHandler(IHasInventory inv) {
      this.inv = inv;
   }

   public int getSlots() {
      return this.inv.getSlotCount();
   }

   public ItemStack getStackInSlot(int slot) {
      return this.inv.getStackInSlot(slot);
   }

   @Nonnull
   public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else if (!this.isItemValid(slot, stack)) {
         return stack;
      } else {
         ItemStack invStack = this.inv.getStackInSlot(slot);
         if (invStack.isEmpty()) {
            int max = Math.min(stack.getMaxStackSize(), this.inv.getMaxStackSize(slot));
            if (stack.getCount() <= max) {
               if (!simulate) {
                  this.inv.setStackInSlot(slot, stack.copy());
               }

               return ItemStack.EMPTY;
            } else {
               if (!simulate) {
                  this.inv.setStackInSlot(slot, stack.split(max));
               } else {
                  stack.shrink(max);
               }

               return stack;
            }
         } else if (!ItemHandlerHelper.canItemStacksStack(stack, invStack)) {
            return stack;
         } else {
            int toAdd = Math.min(stack.getMaxStackSize(), this.inv.getMaxStackSize(slot)) - invStack.getCount();
            if (toAdd <= 0) {
               return stack;
            } else if (toAdd > stack.getCount()) {
               if (!simulate) {
                  ItemStack copy = invStack.copy();
                  copy.grow(stack.getCount());
                  this.inv.setStackInSlot(slot, copy);
               }

               return ItemStack.EMPTY;
            } else {
               ItemStack copy = stack.copy();
               copy.shrink(toAdd);
               if (simulate) {
                  return copy;
               } else {
                  ItemStack realCopy = invStack.copy();
                  realCopy.grow(toAdd);
                  this.inv.setStackInSlot(slot, realCopy);
                  return copy;
               }
            }
         }
      }
   }

   @Nonnull
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (amount == 0) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = this.inv.getStackInSlot(slot);
         if (stack.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack split;
            if (simulate) {
               split = stack.copy();
               split.setCount(Math.min(stack.getCount(), amount));
            } else {
               split = stack.split(amount);
               this.inv.setStackInSlot(slot, stack);
            }

            return split;
         }
      }
   }

   public int getSlotLimit(int slot) {
      return this.inv.getMaxStackSize(slot);
   }

   public boolean isItemValid(int slot, ItemStack stack) {
      return this.inv.canInsert(slot, stack);
   }
}
