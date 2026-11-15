package ic2.core.inventory.slot;

import ic2.core.block.base.features.IXPMachine;
import ic2.core.inventory.base.IHasInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class XPSlot extends SlotBase {
   IXPMachine machine;

   public <T extends IXPMachine & IHasInventory> XPSlot(T inv, int index, int xPosition, int yPosition) {
      super(inv, index, xPosition, yPosition);
      this.machine = inv;
   }

   public boolean mayPlace(ItemStack stack) {
      return false;
   }

   public void onTake(Player player, ItemStack stack) {
      player.giveExperiencePoints(this.machine.getCreatedXP(true));
      super.onTake(player, stack);
   }
}
