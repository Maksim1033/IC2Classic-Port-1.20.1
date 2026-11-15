package ic2.curioplugin.core.packet;

import ic2.core.IC2;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.IC2Packet;
import ic2.curioplugin.core.client.CurioComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class CurioScrollPacket extends IC2Packet {
   int scrollOffset;

   public CurioScrollPacket() {
   }

   public CurioScrollPacket(int scrollOffset) {
      this.scrollOffset = scrollOffset;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.scrollOffset);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.scrollOffset = buffer.readVarInt();
   }

   @Override
   public void handlePacket(Player source) {
      if (source.containerMenu instanceof ContainerComponent<?> container) {
         container.setCurioOffset(this.scrollOffset);
         if (IC2.PLATFORM.isRendering()) {
            CurioComponent comp = container.getComponent(CurioComponent.class);
            if (comp != null) {
               comp.updateSlots();
            }
         } else {
            PacketManager.INSTANCE.sendToPlayer(new CurioScrollPacket(this.scrollOffset), source);
         }
      }
   }
}
