package ic2.core.inventory.slot;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LockedSlot extends SlotBase {
   public LockedSlot(IHasInventory inv, int index, int xPosition, int yPosition) {
      super(inv, index, xPosition, yPosition);
   }

   public boolean mayPlace(ItemStack stack) {
      return false;
   }

   public boolean mayPickup(Player playerIn) {
      return false;
   }

   public static class DragableLockedSlot extends LockedSlot {
      public DragableLockedSlot(IHasInventory inv, int index, int xPosition, int yPosition) {
         super(inv, index, xPosition, yPosition);
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         return IC2.PLATFORM.isRendering() && this.isDragging();
      }

      @OnlyIn(Dist.CLIENT)
      public boolean isDragging() {
         Screen screen = Minecraft.getInstance().screen;
         return screen instanceof IC2Screen && ((IC2Screen)screen).isSlotDragging();
      }

      public void onClick(int mouseButton, boolean half, ItemStack itemStack) {
         if (mouseButton == 2) {
            this.set(ItemStack.EMPTY);
         } else {
            if (mouseButton == 0) {
               if (!this.hasItem()) {
                  if (!itemStack.isEmpty()) {
                     this.set(StackUtil.copyWithSize(itemStack, half ? itemStack.getCount() : 1));
                  }

                  return;
               }

               ItemStack stack = this.getItem();
               int decrease = 1;
               if (half && stack.getCount() > 1) {
                  decrease = stack.getCount() / 2;
               }

               stack.shrink(decrease);
            }

            if (mouseButton == 1) {
               if (!this.hasItem()) {
                  if (!itemStack.isEmpty()) {
                     this.set(StackUtil.copyWithSize(itemStack, 1));
                  }

                  return;
               }

               ItemStack stack = this.getItem();
               int increase = 1;
               if (half) {
                  increase = stack.getCount() / 2;
               }

               stack.setCount(Math.min(this.getMaxStackSize(stack), stack.getCount() + increase));
            }
         }
      }
   }
}
