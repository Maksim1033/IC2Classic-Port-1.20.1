package ic2.core.inventory.container;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.base.IPortableInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public abstract class ItemContainer<T extends IPortableInventory> extends ContainerComponent<T> {
   int id;

   public ItemContainer(T key, Player player, int id, int windowID) {
      super(key, player, windowID);
      this.id = id;
   }

   @Override
   public Component getName() {
      return this.getHolder().getInventoryStack().getHoverName();
   }

   @Override
   public int getInventorySize() {
      return this.getHolder().getSlotCount();
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (IC2.PLATFORM.isSimulating()) {
         if (clickTypeIn == ClickType.THROW) {
            if (slotId == -999) {
               if (!this.getCarried().isEmpty()) {
                  this.gui.onGuiClosed(player);
                  player.closeContainer();
               }
            } else {
               ItemStack stack = this.getSlot(slotId).getItem();
               if (stack.getItem() instanceof IHasHeldGui) {
                  int id = ((IHasHeldGui)stack.getItem()).getGuiId(stack);
                  if (id == this.id) {
                     this.gui.onGuiClosed(player);
                     player.closeContainer();
                  }
               }
            }
         } else if (slotId == -999 && clickTypeIn == ClickType.PICKUP) {
            ItemStack stack = this.getCarried();
            if (stack.getItem() instanceof IHasHeldGui) {
               int id = ((IHasHeldGui)stack.getItem()).getGuiId(stack);
               if (id == this.id) {
                  this.gui.onGuiClosed(player);
                  player.closeContainer();
               }
            }
         }
      }

      super.clicked(slotId, dragType, clickTypeIn, player);
   }
}
