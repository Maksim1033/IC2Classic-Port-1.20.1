package ic2.core.networking.packets.server.gui.open;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.networking.packets.IC2Packet;
import ic2.core.platform.player.PlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SpecialOpenItemGuiPacket extends IC2Packet {
   int windowID;
   int slotId;

   public SpecialOpenItemGuiPacket() {
   }

   public SpecialOpenItemGuiPacket(int windowID, int slotId) {
      this.windowID = windowID;
      this.slotId = slotId;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.slotId);
      buffer.writeInt(this.windowID);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.slotId = buffer.readInt();
      this.windowID = buffer.readInt();
   }

   @Override
   public void handlePacket(Player source) {
      if (this.slotId != -1) {
         try {
            Slot slot = source.containerMenu.getSlot(this.slotId);
            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof IHasHeldSlotInventory inv) {
               IHasGui gui = slot.container instanceof Inventory
                  ? inv.getInventory(source, InteractionHand.MAIN_HAND, stack)
                  : inv.getInventory(source, stack, slot);
               if (gui != null) {
                  if (source.containerMenu != source.inventoryMenu) {
                     PlayerHandler.getClientHandler().cachedGUIs.push(new PlayerHandler.GuiEntry(source.containerMenu, Minecraft.getInstance().screen));
                  }

                  IC2.PLATFORM.launchGuiClient(source, InteractionHand.MAIN_HAND, Direction.NORTH, gui, this.windowID);
               }
            }
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }
   }
}
