package ic2.core.inventory.inv;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class IC2CraftingInventory extends TransientCraftingContainer {
   public IC2CraftingInventory(int width, int height) {
      super(new AbstractContainerMenu(null, height) {
         public boolean stillValid(Player playerIn) {
            return false;
         }

         public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
            return ItemStack.EMPTY;
         }
      }, width, height);
   }
}
