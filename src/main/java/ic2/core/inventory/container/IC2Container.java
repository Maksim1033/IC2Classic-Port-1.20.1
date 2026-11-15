package ic2.core.inventory.container;

import ic2.core.IC2;
import ic2.core.inventory.slot.IQuickMoveBlocker;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.inventory.slot.UpgradeSlot;
import ic2.core.platform.corehacks.mixins.server.ContainerMixin;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class IC2Container extends AbstractContainerMenu {
   int extra;

   public IC2Container(int id) {
      super(null, id);
   }

   protected void addExtraSlots(int extra) {
      this.extra += extra;
   }

   public abstract int getInventorySize();

   public boolean moveBackwardsIntoMachine() {
      return false;
   }

   public boolean moveBackwardsIntoInventory() {
      return true;
   }

   public Slot getSlot(int slotId) {
      try {
         return super.getSlot(slotId);
      } catch (Exception var3) {
         IC2.LOGGER.info("Slot Crash: {}", this.getClass());
         return super.getSlot(slotId);
      }
   }

   public void clearSlots(Class<? extends Slot> filter) {
      NonNullList<ItemStack> references = ((ContainerMixin)this).getLastSlots();
      int i = 0;

      for (int m = this.slots.size(); i < m; i++) {
         if (filter.isInstance(this.getSlot(i))) {
            references.set(i, ItemStack.EMPTY);
         }
      }
   }

   public void clearSlots() {
      NonNullList<ItemStack> references = ((ContainerMixin)this).getLastSlots();
      int i = 0;

      for (int m = references.size(); i < m; i++) {
         references.set(i, ItemStack.EMPTY);
      }
   }

   public boolean canDragTo(Slot slot) {
      if (slot instanceof SlotBase) {
         ((SlotBase)slot).markQuickCraft();
      }

      return true;
   }

   public ItemStack quickMoveStack(Player playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem() && !(slot instanceof IQuickMoveBlocker)) {
         ItemStack itemStack1 = slot.getItem();
         itemstack = itemStack1.copy();
         if (index < this.getInventorySize()) {
            if (!this.moveItemStackTo(itemStack1, this.getInventorySize() + this.extra, this.slots.size(), this.moveBackwardsIntoInventory())) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemStack1, 0, this.getInventorySize() + this.extra, this.moveBackwardsIntoMachine())) {
            return ItemStack.EMPTY;
         }

         if (itemStack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemStack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(playerIn, itemStack1);
      }

      return itemstack;
   }

   protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
      return this.moveItemStackTo(stack, this.slots, startIndex, endIndex, reverseDirection);
   }

   protected boolean moveItemStackToPriorizeUpgradeSlots(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
      List<Slot> targetSlots = new ObjectArrayList(endIndex - startIndex);
      List<Slot> normalSlots = new ObjectArrayList(endIndex - startIndex);

      for (int i = startIndex; i < endIndex; i++) {
         Slot s = (Slot)this.slots.get(i);
         (s instanceof UpgradeSlot ? targetSlots : normalSlots).add(s);
      }

      targetSlots.addAll(normalSlots);
      return this.moveItemStackTo(stack, targetSlots, 0, targetSlots.size(), reverseDirection);
   }

   protected boolean moveItemStackTo(ItemStack stack, List<Slot> slots, int startIndex, int endIndex, boolean reverseDirection) {
      boolean flag = false;
      int i = reverseDirection ? endIndex - 1 : startIndex;
      if (stack.isStackable()) {
         for (; !stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex); i += reverseDirection ? -1 : 1) {
            Slot slot = slots.get(i);
            ItemStack itemstack = slot.getItem();
            if (!itemstack.isEmpty() && slot.mayPlace(stack) && !(slot instanceof IQuickMoveBlocker) && ItemStack.isSameItemSameTags(stack, itemstack)) {
               int j = itemstack.getCount() + stack.getCount();
               int maxSize = Math.min(slot.getMaxStackSize(itemstack), stack.getMaxStackSize());
               if (j <= maxSize) {
                  stack.setCount(0);
                  itemstack.setCount(j);
                  slot.set(itemstack);
                  slot.setChanged();
                  flag = true;
               } else if (itemstack.getCount() < maxSize) {
                  stack.shrink(maxSize - itemstack.getCount());
                  itemstack.setCount(maxSize);
                  slot.set(itemstack);
                  slot.setChanged();
                  flag = true;
               }
            }
         }
      }

      if (!stack.isEmpty()) {
         for (int var12 = reverseDirection ? endIndex - 1 : startIndex;
            reverseDirection ? var12 >= startIndex : var12 < endIndex;
            var12 += reverseDirection ? -1 : 1
         ) {
            Slot slot1 = slots.get(var12);
            ItemStack itemStack1 = slot1.getItem();
            if (itemStack1.isEmpty() && slot1.mayPlace(stack) && !(slot1 instanceof IQuickMoveBlocker)) {
               slot1.set(stack.split(Math.min(stack.getCount(), slot1.getMaxStackSize(stack))));
               slot1.setChanged();
               flag = true;
               break;
            }
         }
      }

      return flag;
   }
}
