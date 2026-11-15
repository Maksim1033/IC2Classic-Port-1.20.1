package ic2.core.networking.packets.debug;

import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class RequestServerData extends IC2Packet {
   int slot;

   public RequestServerData() {
   }

   public RequestServerData(int slot) {
      this.slot = slot;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.slot);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.slot = buffer.readVarInt();
   }

   @Override
   public void handlePacket(Player source) {
      try {
         Slot s = source.containerMenu.getSlot(this.slot);
         PacketManager.INSTANCE.sendToPlayer(new AnswerServerData(s.getItem().getTag()), source);
      } catch (Exception var3) {
      }
   }
}
