package ic2.core.inventory.container;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.slot.HiddenPlayerSlot;
import ic2.core.inventory.slot.LockablePlayerSlot;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public abstract class ContainerHasGui<T extends IHasGui> extends IC2Container {
   T gui;
   Player player;
   protected boolean curioAdded;
   public boolean cosmeticCurio = false;
   protected boolean addingCurio = false;
   Vec2i curioPos;
   int scrollOffset;

   public ContainerHasGui(T gui, Player player, int id) {
      super(id);
      this.gui = gui;
      this.player = player;
   }

   public boolean stillValid(Player playerIn) {
      return this.gui.canInteractWith(playerIn);
   }

   public void removed(Player playerIn) {
      this.gui.onGuiClosed(playerIn);
      super.removed(playerIn);
   }

   public T getHolder() {
      return this.gui;
   }

   public Player getPlayer() {
      return this.player;
   }

   protected void addCurioSlots() {
      this.addCurioSlots(new Vec2i());
   }

   protected void addCurioSlots(Vec2i button) {
      this.addCurioSlots(new Vec2i(), button);
   }

   protected void addCurioSlots(Vec2i curioPos, Vec2i button) {
      if (IC2.CURIO_PLUGIN != null) {
         this.curioPos = curioPos;
         this.addingCurio = true;
         this.curioAdded = IC2.CURIO_PLUGIN.addCurioSlots(this, this.player, curioPos, button, 0, false);
         this.addingCurio = false;
      }
   }

   public void addCurioSlots(Slot slot) {
      if (IC2.CURIO_PLUGIN != null && this.addingCurio) {
         this.addSlot(slot);
      }
   }

   public void setCurioOffset(int offset) {
      if (this.curioAdded && this.scrollOffset != offset) {
         this.scrollOffset = offset;
         this.cosmeticCurio = false;
         this.addingCurio = true;
         IC2.CURIO_PLUGIN.addCurioSlots(this, this.player, this.curioPos, new Vec2i(0, 0), offset, true);
         this.addingCurio = false;
      }
   }

   protected void addInternalSlots(Inventory player) {
   }

   public void addHiddenPlayerInventory(Inventory player) {
      this.addInternalSlots(player);

      for (int i = 0; i < 36; i++) {
         this.addSlot(new HiddenPlayerSlot(player, i));
      }
   }

   public void addLockablePlayerInventory(Inventory player, BooleanSupplier isLocked) {
      this.addInternalSlots(player);

      for (int i = 0; i < 3; i++) {
         for (int k = 0; k < 9; k++) {
            this.addSlot(new LockablePlayerSlot(player, k + i * 9 + 9, 8 + k * 18, 84 + i * 18, isLocked));
         }
      }

      for (int j = 0; j < 9; j++) {
         this.addSlot(new LockablePlayerSlot(player, j, 8 + j * 18, 142, isLocked));
      }
   }

   public void addLockablePlayerInventoryWithOffset(Inventory player, int x, int y, BooleanSupplier isLocked) {
      this.addInternalSlots(player);

      for (int i = 0; i < 3; i++) {
         for (int k = 0; k < 9; k++) {
            this.addSlot(new LockablePlayerSlot(player, k + i * 9 + 9, x + 8 + k * 18, y + 84 + i * 18, isLocked));
         }
      }

      for (int j = 0; j < 9; j++) {
         this.addSlot(new LockablePlayerSlot(player, j, x + 8 + j * 18, 142 + y, isLocked));
      }
   }

   public void addPlayerInventory(Inventory player) {
      this.addInternalSlots(player);

      for (int i = 0; i < 3; i++) {
         for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(player, k + i * 9 + 9, 8 + k * 18, 84 + i * 18));
         }
      }

      for (int j = 0; j < 9; j++) {
         this.addSlot(new Slot(player, j, 8 + j * 18, 142));
      }
   }

   public void addPlayerInventoryWithOffset(Inventory player, int x, int y) {
      this.addInternalSlots(player);

      for (int i = 0; i < 3; i++) {
         for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(player, k + i * 9 + 9, x + 8 + k * 18, y + 84 + i * 18));
         }
      }

      for (int j = 0; j < 9; j++) {
         this.addSlot(new Slot(player, j, x + 8 + j * 18, 142 + y));
      }
   }
}
