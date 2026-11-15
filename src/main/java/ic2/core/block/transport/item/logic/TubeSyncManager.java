package ic2.core.block.transport.item.logic;

import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.custom.TubeSyncPacket;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TubeSyncManager {
   static final Object2ObjectMap<Level, TubeSyncManager> CACHE = CollectionUtils.<Level, TubeSyncManager>createLinkedMap();
   Long2ObjectMap<Int2ObjectMap<byte[]>> data = new Long2ObjectLinkedOpenHashMap();
   Level world;

   public TubeSyncManager(Level world) {
      this.world = world;
   }

   public static void onWorldUnload(Level world) {
      CACHE.remove(world);
   }

   public static void onServerStop() {
      CACHE.clear();
   }

   public static void onTick() {
      ObjectIterator var0 = CACHE.values().iterator();

      while (var0.hasNext()) {
         TubeSyncManager cache = (TubeSyncManager)var0.next();
         ObjectIterator var2 = Long2ObjectMaps.fastIterable(cache.data).iterator();

         while (var2.hasNext()) {
            Entry<Int2ObjectMap<byte[]>> entry = (Entry<Int2ObjectMap<byte[]>>)var2.next();
            PacketManager.INSTANCE
               .sendToAllChunkWatchers(
                  cache.world.getChunk(ChunkPos.getX(entry.getLongKey()), ChunkPos.getZ(entry.getLongKey())),
                  new TubeSyncPacket(entry.getLongKey(), (Int2ObjectMap<byte[]>)entry.getValue())
               );
         }

         cache.data.clear();
      }
   }

   public static void markDirty(BlockEntity tile, byte[] data) {
      Level world = tile.getLevel();
      if (!world.isClientSide) {
         TubeSyncManager cache = (TubeSyncManager)CACHE.get(world);
         if (cache == null) {
            cache = new TubeSyncManager(world);
            CACHE.put(world, cache);
         }

         BlockPos pos = tile.getBlockPos();
         Int2ObjectMap<byte[]> chunk = (Int2ObjectMap<byte[]>)cache.data.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
         if (chunk == null) {
            chunk = new Int2ObjectLinkedOpenHashMap();
            cache.data.put(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), chunk);
         }

         chunk.put((pos.getY() - world.getMinBuildHeight() & 65535) << 8 | (pos.getX() & 15) << 4 | pos.getZ() & 15, data);
      }
   }
}
