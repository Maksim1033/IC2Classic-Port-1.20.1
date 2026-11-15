package ic2.core.networking.packets.client;

import ic2.api.network.tile.INetworkClientEventListener;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientTileEventPacket extends IC2Packet {
   BlockPos pos;
   int key;
   int value;

   public ClientTileEventPacket() {
   }

   public ClientTileEventPacket(BlockEntity tile, int key, int value) {
      this.pos = tile.getBlockPos();
      this.key = key;
      this.value = value;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeLong(this.pos.asLong());
      buffer.writeInt(this.key);
      buffer.writeInt(this.value);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.pos = BlockPos.of(buffer.readLong());
      this.key = buffer.readInt();
      this.value = buffer.readInt();
   }

   @Override
   public void handlePacket(Player source) {
      BlockEntity tile = source.level().getBlockEntity(this.pos);
      if (tile instanceof INetworkClientEventListener) {
         ((INetworkClientEventListener)tile).onClientDataReceived(source, this.key, this.value);
      }
   }
}
