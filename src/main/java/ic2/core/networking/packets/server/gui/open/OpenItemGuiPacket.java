package ic2.core.networking.packets.server.gui.open;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class OpenItemGuiPacket extends IC2Packet {
   int windowID;
   int inventorySlot;

   public OpenItemGuiPacket() {
   }

   public OpenItemGuiPacket(int inventorySlot, int windowID) {
      this.windowID = windowID;
      this.inventorySlot = inventorySlot;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.inventorySlot);
      buffer.writeInt(this.windowID);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.inventorySlot = buffer.readInt();
      this.windowID = buffer.readInt();
   }

   @Override
   public void handlePacket(Player source) {
      if (source.getInventory().selected == this.inventorySlot || this.inventorySlot == -1) {
         InteractionHand hand = this.inventorySlot == -1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         ItemStack stack = source.getItemInHand(hand);
         if (stack.getItem() instanceof IHasHeldGui) {
            IHasGui gui = ((IHasHeldGui)stack.getItem()).getInventory(source, hand, stack);
            if (gui != null) {
               IC2.PLATFORM.launchGuiClient(source, hand, Direction.NORTH, gui, this.windowID);
            }
         }
      }
   }
}
