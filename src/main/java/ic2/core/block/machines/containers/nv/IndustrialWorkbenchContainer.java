package ic2.core.block.machines.containers.nv;

import ic2.core.block.machines.components.nv.IndustrialWorkbenchComponent;
import ic2.core.block.machines.components.nv.IndustrialWorkbenchRequest;
import ic2.core.block.machines.logic.crafter.Snapshot;
import ic2.core.block.machines.tiles.nv.IndustrialWorkbenchTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.ClassFilter;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.inventory.slot.MemorySlot;
import ic2.core.inventory.slot.RecursiveCraftSlot;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.item.misc.MemoryStickItem;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class IndustrialWorkbenchContainer extends ContainerComponent<IndustrialWorkbenchTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_work_table.png");
   DataSlot saving = DataSlot.standalone();

   public IndustrialWorkbenchContainer(IndustrialWorkbenchTileEntity key, Player player, int id) {
      super(key, player, id);
      this.disablePreviewer();
      this.addSlot(new RecursiveCraftSlot(key.crafting_inventory, 9, 124, 35));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            this.addSlot(new LockedSlot.DragableLockedSlot(key.crafting_inventory, j + i * 3, 30 + j * 18, 17 + i * 18));
         }
      }

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 9; x++) {
            this.addSlot(new SlotBase(key, x + y * 9, 8 + 18 * x, 74 + 18 * y));
         }
      }

      for (int x = 0; x < 9; x++) {
         this.addSlot(new SlotBase(key.garbageSlot, x, 8 + 18 * x, 131));
      }

      for (int y = 0; y < 9; y++) {
         for (int x = 0; x < 3; x++) {
            this.addSlot(new MemorySlot(key, x + y * 3, 180 + 18 * x, 6 + 18 * y));
         }
      }

      this.addSlot(new FilterSlot(key.memory, 0, 152, 17, new ClassFilter(MemoryStickItem.class)));
      this.addSlot(new FilterSlot(key.memory, 1, 152, 35, new ClassFilter(MemoryStickItem.class)));

      for (int i = 0; i < 12; i++) {
         int x = i % 6 * 18;
         int y = i / 6 * 18;
         this.addSlot(new IndustrialWorkbenchContainer.RequestSlot(key.requestCache, i, x - 112, y + 42, key.viewOpen));
      }

      this.addSlot(new IndustrialWorkbenchContainer.ClosableGhostSlot(key.requestedItem, 0, -104, 18, SpecialFilters.NO_TAGS, key.viewOpen));
      this.addLockablePlayerInventoryWithOffset(player.getInventory(), 0, 75, this::isSaving);
      this.addComponent(new IndustrialWorkbenchComponent(key));
      this.addComponent(new IndustrialWorkbenchRequest(key, key.viewOpen));
      this.addComponent(new FilterComponent(new Vec2i(0, 83)).blockClosing());
      this.addDataSlot(this.saving);
   }

   @Override
   public void removed(Player playerIn) {
      if (this.isSaving()) {
         this.setCarried(ItemStack.EMPTY);
      }

      super.removed(playerIn);
   }

   @Override
   public int getInventorySize() {
      return 75;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(237, 241);
      screen.setPlayerInventoryOffset(0, 1);
      screen.setContainerOffset(-25, 0);
   }

   public boolean isSaving() {
      return this.saving.get() == 1;
   }

   public void setSaving(boolean value) {
      this.saving.set(value ? 1 : 0);
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId >= 0) {
         Slot slot = this.getSlot(slotId);
         if (this.isSaving()) {
            if (slot instanceof MemorySlot && this.getHolder().isSimulating() && this.getHolder().saveRecipe(slot.getSlotIndex())) {
               this.setCarried(ItemStack.EMPTY);
               ((ServerPlayer)player).connection.send(new ClientboundContainerSetSlotPacket(-1, this.incrementStateId(), -1, this.getCarried()));
               this.setSaving(false);
               this.broadcastChanges();
            }

            return;
         }

         if (slot instanceof IndustrialWorkbenchContainer.ClosableGhostSlot) {
            ItemStack stack = this.getCarried();
            this.getHolder().requestedItem.setStackInSlot(0, stack.isEmpty() ? ItemStack.EMPTY : StackUtil.copyWithSize(stack, 1));
            return;
         }

         if (slot instanceof LockedSlot.DragableLockedSlot) {
            ((LockedSlot.DragableLockedSlot)slot)
               .onClick(
                  clickTypeIn == ClickType.QUICK_CRAFT ? 1 : dragType,
                  clickTypeIn == ClickType.QUICK_MOVE,
                  clickTypeIn == ClickType.QUICK_CRAFT ? StackUtil.copyWithSize(this.getCarried(), 1) : this.getCarried()
               );
            this.getHolder().updateRecipe();
            this.broadcastChanges();
            return;
         }

         if (slot instanceof MemorySlot) {
            if (this.getHolder().isSimulating()) {
               if (dragType == 2) {
                  this.getHolder().deleteRecipe(slot.getSlotIndex());
               } else if (dragType == 0) {
                  this.getHolder().setRecipe(slot.getSlotIndex());
               }

               this.broadcastChanges();
            }

            return;
         }

         if (slot instanceof IndustrialWorkbenchContainer.RequestSlot) {
            return;
         }

         if (slot instanceof RecursiveCraftSlot) {
            if (this.getHolder().isRendering()) {
               this.disableDoubleClick();
               return;
            }

            if (!slot.hasItem()) {
               return;
            }

            IndustrialWorkbenchTileEntity work = this.getHolder();
            work.createSnapshot();
            Snapshot[] snap = work.takeSnapshot();
            boolean needsRefill = false;
            if (clickTypeIn == ClickType.QUICK_MOVE) {
               int count = 0;

               for (int max = work.getCraftLimit(); count < max && work.canCraft(player); needsRefill = true) {
                  work.createSnapshot();
                  ItemStack output = work.craft(player);
                  ItemStack copy = output.copy();
                  count += copy.getCount();
                  if (!this.moveItemStackTo(output, this.getInventorySize(), this.slots.size(), true)) {
                     if (copy.getCount() != output.getCount()) {
                        player.drop(output, true);
                        work.destroySnapshot();
                     } else {
                        work.releaseSnapshot();
                     }
                     break;
                  }

                  work.destroySnapshot();
               }
            } else if (this.canFitIntoPlayerHand(slot.getItem()) && work.canCraft(player)) {
               ItemStack output = work.craft(player);
               ItemStack playerHeld = this.getCarried();
               if (playerHeld.isEmpty()) {
                  this.setCarried(output);
               } else {
                  int left = Math.min(output.getCount(), StackUtil.getStackSizeLeft(playerHeld));
                  output.shrink(left);
                  playerHeld.grow(left);
                  this.setCarried(playerHeld);
                  if (output.getCount() > 0) {
                     player.drop(output, true);
                  }
               }

               ((ServerPlayer)player).connection.send(new ClientboundContainerSetSlotPacket(-1, this.incrementStateId(), -1, this.getCarried()));
               needsRefill = true;
            }

            if (needsRefill) {
               work.putSnapshot(snap);
               work.triggerRestock(player);
               this.broadcastChanges();
            }

            return;
         }
      }

      if (!this.isSaving()) {
         super.clicked(slotId, dragType, clickTypeIn, player);
         this.broadcastChanges();
      }
   }

   public boolean canFitIntoPlayerHand(ItemStack stack) {
      ItemStack playerHand = this.getCarried();
      return playerHand.isEmpty() ? true : StackUtil.isStackEqual(stack, playerHand) && StackUtil.getStackSizeLeft(playerHand) >= stack.getCount();
   }

   @OnlyIn(Dist.CLIENT)
   public void disableDoubleClick() {
      Screen screen = Minecraft.getInstance().screen;
      if (screen instanceof IC2Screen) {
         ((IC2Screen)screen).preventDoubleClick();
      }
   }

   public static class ClosableGhostSlot extends GhostSlot {
      MutableBoolean value;

      public ClosableGhostSlot(IHasInventory inv, int index, int xPosition, int yPosition, IFilter filter, MutableBoolean value) {
         super(inv, index, xPosition, yPosition, filter);
         this.value = value;
      }

      @OnlyIn(Dist.CLIENT)
      public boolean isActive() {
         return this.value.isTrue();
      }
   }

   public static class RequestSlot extends LockedSlot {
      MutableBoolean value;

      public RequestSlot(IHasInventory inv, int index, int xPosition, int yPosition, MutableBoolean value) {
         super(inv, index, xPosition, yPosition);
         this.value = value;
      }

      @OnlyIn(Dist.CLIENT)
      public boolean isActive() {
         return this.value.isTrue();
      }
   }
}
