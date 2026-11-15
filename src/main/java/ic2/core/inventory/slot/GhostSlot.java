package ic2.core.inventory.slot;

import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.filter.IFilter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GhostSlot extends SlotBase implements IGhostSlot, IQuickMoveBlocker {
   IFilter filter;

   public GhostSlot(IHasInventory inv, int index, int xPosition, int yPosition, IFilter filter) {
      super(inv, index, xPosition, yPosition);
      this.filter = filter;
   }

   public boolean mayPlace(ItemStack stack) {
      return this.filter == null || this.filter.matches(stack);
   }

   public boolean mayPickup(Player playerIn) {
      return false;
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
      this.set(stack);
   }

   @Override
   public IGhostSlot.GhostType getType() {
      return IGhostSlot.GhostType.FILTER;
   }
}
