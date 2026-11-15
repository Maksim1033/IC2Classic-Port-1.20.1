package ic2.core.networking.packets.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.IC2Packet;
import ic2.core.networking.packets.server.ChunkAnswerPacket;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.world.ForgeChunkManager.TicketTracker;

public class ChunkRequestPacket extends IC2Packet {
   static final Cache<ResourceKey<Level>, Long2IntLinkedOpenHashMap> CACHE = CacheBuilder.newBuilder()
      .concurrencyLevel(1)
      .expireAfterWrite(10L, TimeUnit.SECONDS)
      .build();

   @Override
   public void write(FriendlyByteBuf buffer) {
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
   }

   @Override
   public void handlePacket(Player source) {
      if (source instanceof ServerPlayer) {
         Long2IntLinkedOpenHashMap map = (Long2IntLinkedOpenHashMap)CACHE.getIfPresent(source.level().dimension());
         if (map == null) {
            map = new Long2IntLinkedOpenHashMap();
            ForcedChunksSavedData data = (ForcedChunksSavedData)((ServerPlayer)source)
               .serverLevel()
               .getDataStorage()
               .computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
            this.addToMap(map, data.getBlockForcedChunks());
            this.addToMap(map, data.getEntityForcedChunks());
            this.addToMap(map, data.getChunks());
            CACHE.put(source.level().dimension(), map);
         }

         PacketManager.INSTANCE.sendToPlayer(new ChunkAnswerPacket(map), source);
      }
   }

   private void addToMap(Long2IntLinkedOpenHashMap map, TicketTracker<?> tracker) {
      for (LongSet set : tracker.getChunks().values()) {
         this.addToMap(map, set);
      }

      for (LongSet set : tracker.getTickingChunks().values()) {
         this.addToMap(map, set);
      }
   }

   private void addToMap(Long2IntLinkedOpenHashMap map, LongSet set) {
      LongIterator iter = set.iterator();

      while (iter.hasNext()) {
         map.addTo(iter.nextLong(), 1);
      }
   }
}
