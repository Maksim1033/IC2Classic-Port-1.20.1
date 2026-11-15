package ic2.core.networking.packets.server.custom;

import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.networking.packets.IC2Packet;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class TubeSyncPacket extends IC2Packet {
   long chunkPos;
   Int2ObjectMap<byte[]> data = new Int2ObjectLinkedOpenHashMap();

   public TubeSyncPacket() {
   }

   public TubeSyncPacket(long chunkPos, Int2ObjectMap<byte[]> data) {
      this.chunkPos = chunkPos;
      this.data = data;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeLong(this.chunkPos);
      buffer.writeVarInt(this.data.size());
      ObjectIterator var2 = Int2ObjectMaps.fastIterable(this.data).iterator();

      while (var2.hasNext()) {
         Entry<byte[]> entry = (Entry<byte[]>)var2.next();
         buffer.writeInt(entry.getIntKey());
         buffer.writeByteArray((byte[])entry.getValue());
      }
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.chunkPos = buffer.readLong();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         this.data.put(buffer.readInt(), buffer.readByteArray());
      }
   }

   @Override
   public void handlePacket(Player source) {
      int x = ChunkPos.getX(this.chunkPos);
      int z = ChunkPos.getZ(this.chunkPos);
      int minHeight = source.level().getMinBuildHeight();
      LevelChunk chunk = source.level().getChunk(x, z);
      MutableBlockPos mutable = new MutableBlockPos();
      ObjectIterator var7 = Int2ObjectMaps.fastIterable(this.data).iterator();

      while (var7.hasNext()) {
         Entry<byte[]> entry = (Entry<byte[]>)var7.next();
         int pos = entry.getIntKey();
         BlockEntity tile = chunk.getBlockEntity(mutable.set((x << 4) + (pos >> 4 & 15), (pos >> 8 & 65535) + minHeight, (z << 4) + (pos & 15)));
         if (tile instanceof TubeTileEntity) {
            ((TubeTileEntity)tile).onSyncPacketReceived((byte[])entry.getValue());
         }
      }
   }
}
