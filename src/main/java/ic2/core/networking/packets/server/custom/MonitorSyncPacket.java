package ic2.core.networking.packets.server.custom;

import ic2.core.block.cables.mointor.MonitorTileEntity;
import ic2.core.networking.packets.IC2Packet;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MonitorSyncPacket extends IC2Packet {
   BlockPos pos;
   byte[] array;

   public MonitorSyncPacket() {
   }

   public MonitorSyncPacket(BlockPos pos, byte[] array) {
      this.pos = pos;
      this.array = array;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeLong(this.pos.asLong());
      buffer.writeByteArray(this.array);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.pos = BlockPos.of(buffer.readLong());
      this.array = buffer.readByteArray();
   }

   @Override
   public void handlePacket(Player source) {
      BlockEntity tile = source.level().getBlockEntity(this.pos);
      if (tile instanceof MonitorTileEntity) {
         ((MonitorTileEntity)tile).dataManager.onSyncReceived(new FriendlyByteBuf(Unpooled.wrappedBuffer(this.array)));
      }
   }
}
