package ic2.core.platform.events;

import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class ChunkSaver {
   static final Map<Level, ChunkSaver> SAVER_MAP = Object2ObjectMaps.synchronize(CollectionUtils.createMap());
   Long2ObjectMap<LongSet> chunkMap = new Long2ObjectLinkedOpenHashMap();

   public static ChunkSaver getSaver(Level world) {
      if (world.isClientSide) {
         return null;
      } else {
         ChunkSaver saver = SAVER_MAP.get(world);
         if (saver == null) {
            saver = new ChunkSaver();
            SAVER_MAP.put(world, saver);
         }

         return saver;
      }
   }

   public static void onWorldUnload(Level world) {
      SAVER_MAP.remove(world);
   }

   public void addPosition(BlockPos pos) {
      long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
      LongSet blockSet = (LongSet)this.chunkMap.get(key);
      if (blockSet == null) {
         blockSet = new LongOpenHashSet();
         this.chunkMap.put(key, blockSet);
      }

      blockSet.add(pos.asLong());
   }

   public void removePosition(BlockPos pos) {
      long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
      LongSet blockSet = (LongSet)this.chunkMap.get(key);
      if (blockSet != null) {
         blockSet.remove(pos.asLong());
         if (blockSet.isEmpty()) {
            this.chunkMap.remove(key);
         }
      }
   }

   public boolean shouldSave(ChunkPos pos) {
      return this.chunkMap.containsKey(pos.toLong());
   }

   public void onSave(Level world) {
      LongIterator var2 = this.chunkMap.keySet().iterator();

      while (var2.hasNext()) {
         long entry = (Long)var2.next();
         ChunkAccess chunk = world.getChunk(ChunkPos.getX(entry), ChunkPos.getZ(entry), ChunkStatus.FULL, false);
         if (chunk != null) {
            chunk.setUnsaved(true);
         }
      }
   }
}
