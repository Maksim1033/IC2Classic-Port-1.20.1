package ic2.core.inventory.slot;

import net.minecraft.world.item.ItemStack;

public interface IGhostSlot {
   int getSlotID();

   int getXPos();

   int getYPos();

   boolean isStackValid(ItemStack var1);

   void setFilter(ItemStack var1);

   default IGhostSlot.GhostType getType() {
      return IGhostSlot.GhostType.NORMAL;
   }

   public static enum GhostType {
      FILTER,
      NORMAL;
   }
}
