package ic2.core.inventory.inv;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.ComponentContainerScreen;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PortableInventory implements IPortableInventory {
   protected final Player owner;
   protected NonNullList<ItemStack> inventory;
   protected final int id;
   protected final ItemStack stack;
   protected final Slot slot;

   public PortableInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      this.owner = owner;
      this.stack = stack.copy();
      this.slot = slot;
      this.inventory = NonNullList.withSize(this.getSlotCount(), ItemStack.EMPTY);
      if (IC2.PLATFORM.isSimulating()) {
         this.clearAllIds(owner.getInventory());
         if (slot != null) {
            this.clearAllIds(slot.container);
         }

         this.id = owner.level().random.nextInt(32767);
         held.setGuiID(stack, this.id);
      } else {
         this.id = -1;
      }
   }

   public int getID() {
      return this.id;
   }

   @Override
   public boolean canInteractWith(Player player) {
      return player.isAlive() && this.findContainer(player);
   }

   private boolean findContainer(Player player) {
      if (this.slot != null) {
         return this.isContainer(this.slot.getItem());
      } else {
         Inventory inv = player.getInventory();
         AbstractContainerMenu menu = player.containerMenu;
         ItemStack stack = menu.getCarried();
         if (this.isContainer(stack)) {
            return true;
         } else {
            for (int i = 0; i < inv.getContainerSize(); i++) {
               if (this.isContainer(inv.getItem(i))) {
                  return true;
               }
            }

            for (int ix = 0; ix < this.getSlotCount(); ix++) {
               if (this.isContainer(this.getStackInSlot(ix))) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   protected ItemStack getOwnerStack(Player player) {
      if (this.slot != null && this.isContainer(this.slot.getItem())) {
         return this.slot.getItem();
      } else {
         Inventory inv = player.getInventory();
         AbstractContainerMenu menu = player.containerMenu;
         ItemStack stack = menu.getCarried();
         if (this.isContainer(stack)) {
            return stack;
         } else {
            for (int i = 0; i < inv.getContainerSize(); i++) {
               if (this.isContainer(inv.getItem(i))) {
                  return inv.getItem(i);
               }
            }

            for (int ix = 0; ix < this.getSlotCount(); ix++) {
               if (this.isContainer(this.getStackInSlot(ix))) {
                  return this.getStackInSlot(ix);
               }
            }

            return this.getInventoryStack();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Screen createGui(Player player, InteractionHand hand, Direction side, IC2Container container) {
      return new ComponentContainerScreen((ContainerComponent<?>)container);
   }

   @Override
   public void onGuiClosed(Player player) {
      if (IC2.PLATFORM.isSimulating()) {
         if (this.slot != null) {
            ItemStack stack = this.slot.getItem();
            if (this.isContainer(stack)) {
               ((IHasHeldGui)stack.getItem()).setGuiID(stack, -1);
               this.save(this.getNBTData(stack, true));
               this.onClose(stack);
               this.clearAllIds(player.getInventory());
               this.clearAllIds(this.slot.container);
            }

            return;
         }

         ItemStack stack = this.getInv(player);
         if (!stack.isEmpty()) {
            ((IHasHeldGui)stack.getItem()).setGuiID(stack, -1);
            this.save(this.getNBTData(stack, true));
            this.onClose(stack);
            this.clearAllIds(player.getInventory());
         } else {
            ItemStack items = this.getInventoryStack();

            for (int i = 0; i < this.getSlotCount(); i++) {
               if (this.isContainer(this.getStackInSlot(i))) {
                  this.setStackInSlot(i, ItemStack.EMPTY);
                  ((IHasHeldGui)items.getItem()).setGuiID(items, -1);
                  this.save(this.getNBTData(items, true));
                  this.onClose(items);
                  Block.popResource(player.getCommandSenderWorld(), player.blockPosition(), items);
                  this.clearAllIds(player.getInventory());
                  return;
               }
            }
         }
      }
   }

   private void clearAllIds(Container container) {
      if (container != null) {
         int i = 0;

         for (int m = container.getContainerSize(); i < m; i++) {
            container.getItem(i).removeTagKey("GUI_ID");
         }
      }
   }

   public void onClose(ItemStack data) {
      if (!data.hasTag()) {
         data.setTag(null);
      }
   }

   protected final boolean isContainer(ItemStack stack) {
      return stack.getItem() instanceof IHasHeldGui && ((IHasHeldGui)stack.getItem()).getGuiId(stack) == this.id;
   }

   public ItemStack getInv(Player player) {
      Inventory inv = player.getInventory();
      AbstractContainerMenu menu = player.containerMenu;
      ItemStack stack = menu.getCarried();
      if (this.isContainer(stack)) {
         stack = this.getInventoryStack();
         menu.setCarried(stack);
         return stack;
      } else {
         for (int i = 0; i < inv.getContainerSize(); i++) {
            if (this.isContainer(inv.getItem(i))) {
               ItemStack invSlot = inv.getItem(i);
               inv.setItem(i, invSlot);
               return invSlot;
            }
         }

         return ItemStack.EMPTY;
      }
   }

   @Override
   public abstract int getSlotCount();

   @Override
   public ItemStack getStackInSlot(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      this.inventory.set(slot, stack);
      if (stack.getCount() > this.getMaxStackSize(slot)) {
         stack.setCount(this.getMaxStackSize(slot));
      }

      this.markDirty();
   }

   @Override
   public int getMaxStackSize(int slot) {
      return 64;
   }

   @Override
   public boolean canInsert(int slot, ItemStack stack) {
      return true;
   }

   @Override
   public boolean canExtract(int slot, ItemStack stack) {
      return true;
   }

   @Override
   public ItemStack getInventoryStack() {
      return this.stack;
   }

   protected void markDirty() {
      if (this.slot != null) {
         ItemStack stack = this.slot.getItem();
         if (this.isContainer(stack)) {
            this.save(this.getNBTData(stack, true));
         }

         this.slot.setChanged();
      }
   }

   @Override
   public IPortableInventory load(ItemStack stack) {
      this.load(this.getNBTData(stack, false));
      return this;
   }

   public void load(CompoundTag nbt) {
      NBTUtils.loadItems(nbt, this.inventory);
   }

   public void save(CompoundTag nbt) {
      NBTUtils.saveItems(nbt, this.inventory);
   }

   public CompoundTag getNBTData(ItemStack stack, boolean create) {
      return create ? stack.getOrCreateTag() : StackUtil.getNbtData(stack);
   }
}
