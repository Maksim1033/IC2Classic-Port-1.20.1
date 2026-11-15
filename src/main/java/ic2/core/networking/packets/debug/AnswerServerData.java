package ic2.core.networking.packets.debug;

import ic2.core.networking.packets.IC2Packet;
import ic2.core.platform.events.IC2EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class AnswerServerData extends IC2Packet {
   CompoundTag nbt;

   public AnswerServerData() {
   }

   public AnswerServerData(CompoundTag nbt) {
      this.nbt = nbt;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeNbt(this.nbt);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.nbt = buffer.readAnySizeNbt();
   }

   @Override
   public void handlePacket(Player source) {
      IC2EventHandler.INSTANCE.NETWORK_DATA = this.nbt;
   }
}
