package ic2.core.networking.packets.server;

import ic2.core.block.rendering.world.impl.ChunkLoaderOverlay;
import ic2.core.networking.packets.IC2Packet;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class ChunkAnswerPacket extends IC2Packet {
   Long2IntMap map = new Long2IntLinkedOpenHashMap();

   public ChunkAnswerPacket() {
   }

   public ChunkAnswerPacket(Long2IntMap map) {
      this.map = map;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.map.size());
      ObjectIterator var2 = Long2IntMaps.fastIterable(this.map).iterator();

      while (var2.hasNext()) {
         Entry entry = (Entry)var2.next();
         buffer.writeLong(entry.getLongKey());
         buffer.writeVarInt(entry.getIntValue());
      }
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         this.map.put(buffer.readLong(), buffer.readVarInt());
      }
   }

   @Override
   public void handlePacket(Player source) {
      ChunkLoaderOverlay.INSTANCE.setForceLoadedChunks(this.map);
   }
}
