package ic2.core.networking.packets.client;

import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.item.inv.SlotRegistry;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.IC2Packet;
import ic2.core.networking.packets.server.gui.open.SpecialOpenItemGuiPacket;
import ic2.core.platform.player.PlayerHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class OpenSlotPacket extends IC2Packet {
   int slot;

   public OpenSlotPacket() {
   }

   public OpenSlotPacket(int slot) {
      this.slot = slot;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.slot);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.slot = buffer.readInt();
   }

   @Override
   public void handlePacket(Player player) {
      if (player instanceof ServerPlayer) {
         int slotId = this.slot < 0 ? -this.slot - 1 : this.slot;
         Slot slot = player.containerMenu.getSlot(slotId);
         if (!SlotRegistry.INSTANCE.isInvalidSlot(player.containerMenu, slot)) {
            ItemStack stack = slot.getItem();
            if (slot.mayPlace(stack) && slot.mayPickup(player) && stack.getItem() instanceof IHasHeldSlotInventory inv && stack.getCount() == 1) {
               if (!inv.hasInventory(stack)) {
                  return;
               }

               if (slot.container instanceof Inventory) {
                  this.create(inv.getInventory(player, InteractionHand.MAIN_HAND, stack), (ServerPlayer)player, slotId);
               } else {
                  this.create(inv.getInventory(player, stack, slot), (ServerPlayer)player, slotId);
               }
            }
         }
      }
   }

   protected void create(IHasGui gui, ServerPlayer player, int slot) {
      if (player.containerMenu != player.inventoryMenu) {
         PlayerHandler.getHandler(player).cachedGUIs.push(new PlayerHandler.GuiEntry(player.containerMenu));
      }

      player.nextContainerCounter();
      player.doCloseContainer();
      PacketManager.INSTANCE.sendToPlayer(new SpecialOpenItemGuiPacket(player.containerCounter, slot), player);
      player.containerMenu = gui.createContainer(player, InteractionHand.MAIN_HAND, Direction.NORTH, player.containerCounter);
      player.initMenu(player.containerMenu);
   }
}
