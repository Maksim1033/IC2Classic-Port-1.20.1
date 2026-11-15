package ic2.core.networking.packets.client;

import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.tile.INetworkDataEventListener;
import ic2.core.networking.NetworkManager;
import ic2.core.networking.buffers.InputBuffer;
import ic2.core.networking.buffers.OutputBuffer;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;

public class TileDataBufferEventPacket extends IC2Packet {
   BlockPos pos;
   boolean client;
   String id;
   INetworkDataBuffer dataBuffer;

   public TileDataBufferEventPacket() {
   }

   public TileDataBufferEventPacket(BlockEntity tile, String id, INetworkDataBuffer dataBuffer, boolean client) {
      this.pos = tile.getBlockPos();
      this.id = id;
      this.dataBuffer = dataBuffer;
      this.client = client;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeLong(this.pos.asLong());
      buffer.writeBoolean(this.client);
      buffer.writeUtf(this.id);
      buffer.writeResourceLocation(NetworkManager.getID(this.dataBuffer));
      this.dataBuffer.write(new OutputBuffer(buffer));
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.pos = BlockPos.of(buffer.readLong());
      this.client = buffer.readBoolean();
      this.id = buffer.readUtf(32767);
      this.dataBuffer = NetworkManager.createBuffer(buffer.readResourceLocation());
      this.dataBuffer.read(new InputBuffer(buffer));
   }

   @Override
   public void handlePacket(Player source) {
      if (this.dataBuffer != null) {
         BlockEntity tile = source.level().getBlockEntity(this.pos);
         if (tile instanceof INetworkDataEventListener) {
            ((INetworkDataEventListener)tile).onDataBufferReceived(source, this.id, this.dataBuffer, this.client ? Dist.CLIENT : Dist.DEDICATED_SERVER);
         }
      }
   }
}
