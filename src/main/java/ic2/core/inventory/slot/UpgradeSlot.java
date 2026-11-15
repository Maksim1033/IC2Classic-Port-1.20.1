package ic2.core.inventory.slot;

import ic2.api.items.IUpgradeItem;
import ic2.api.tiles.IMachine;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class UpgradeSlot extends SlotBase implements IGhostSlot {
   IMachine machine;

   public UpgradeSlot(IMachine machine, IHasInventory inv, int index, int xPosition, int yPosition) {
      super(inv, index, xPosition, yPosition);
      this.machine = machine;
      this.setBackground(new ResourceLocation("ic2", "misc/gui/upgrade"));
   }

   public <T extends IHasInventory & IMachine> UpgradeSlot(T inv, int index, int xPosition, int yPosition) {
      super(inv, index, xPosition, yPosition);
      this.machine = inv;
      this.setBackground(new ResourceLocation("ic2", "misc/gui/upgrade"));
   }

   @Override
   public void setChanged() {
      if (IC2.PLATFORM.isSimulating()) {
         this.machine.onUpgradesChanged();
      }
   }

   public boolean mayPlace(ItemStack stack) {
      return stack.getItem() instanceof IUpgradeItem ? this.machine.getSupportedUpgradeTypes().contains(((IUpgradeItem)stack.getItem()).getType(stack)) : false;
   }

   public boolean mayPickup(Player playerIn) {
      ItemStack stack = this.getItem();
      return stack.getItem() instanceof IUpgradeItem && ((IUpgradeItem)stack.getItem()).getExtraTier(stack, this.machine) > 0
         ? IC2.KEYBOARD.isAltKeyDown(playerIn)
         : super.mayPickup(playerIn);
   }

   public IMachine getMachine() {
      return this.machine;
   }

   @Override
   public int getSlotID() {
      return this.index;
   }

   @Override
   public int getXPos() {
      return this.x;
   }

   @Override
   public int getYPos() {
      return this.y;
   }

   @Override
   public boolean isStackValid(ItemStack stack) {
      return this.mayPlace(stack);
   }

   @Override
   public void setFilter(ItemStack stack) {
   }
}
