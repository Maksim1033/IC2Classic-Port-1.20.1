package ic2.core.networking.packets.server.custom;

import ic2.api.tiles.tubes.IItemCache;
import ic2.core.block.transport.item.cache.ClientItemCache;
import ic2.core.networking.packets.IC2Packet;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class CacheAnswerPacket extends IC2Packet {
   byte[] answerData;

   public CacheAnswerPacket() {
   }

   public CacheAnswerPacket(byte[] answerData) {
      this.answerData = answerData;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeByteArray(this.answerData);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.answerData = buffer.readByteArray();
   }

   @Override
   public void handlePacket(Player source) {
      IItemCache cache = IItemCache.getCache();
      if (cache instanceof ClientItemCache) {
         ((ClientItemCache)cache).onDataReceived(new FriendlyByteBuf(Unpooled.wrappedBuffer(this.answerData)));
      }
   }
}
