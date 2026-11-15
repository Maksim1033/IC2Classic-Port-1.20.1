package ic2.core.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public abstract class IC2Packet {
   public abstract void write(FriendlyByteBuf var1);

   public abstract void read(FriendlyByteBuf var1);

   public abstract void handlePacket(Player var1);

   public boolean needsMainThread() {
      return true;
   }
}
