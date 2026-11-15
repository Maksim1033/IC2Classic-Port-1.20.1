package ic2.core.networking.packets.server.gui.sync;

import ic2.api.network.container.IContainerDataEvent;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class ContainerSyncPacket extends IC2Packet {
   int key;
   int value;

   public ContainerSyncPacket() {
   }

   public ContainerSyncPacket(int key, int value) {
      this.key = key;
      this.value = value;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.key);
      buffer.writeInt(this.value);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.key = buffer.readInt();
      this.value = buffer.readInt();
   }

   @Override
   public void handlePacket(Player source) {
      if (source.containerMenu instanceof IContainerDataEvent event) {
         event.onDataReceived(this.key, this.value);
      }
   }
}
