package ic2.core.platform.registries;

import ic2.api.ticks.ITickScheduler;
import ic2.core.utils.collection.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public class TickScheduler implements ITickScheduler {
   Map<Level, TreeMap<Long, List<ToIntFunction<Level>>>> worldCallbacks = CollectionUtils.<Level, TreeMap<Long, List<ToIntFunction<Level>>>>createMap();
   NavigableMap<Long, List<ToIntFunction<MinecraftServer>>> serverCallbacks = CollectionUtils.createTreeMap();
   NavigableMap<Long, List<IntSupplier>> clientCallbacks = CollectionUtils.createTreeMap();
   long serverTick = 0L;
   long clientTicker = 0L;

   @Override
   public void addWorldCallback(Level world, ToIntFunction<Level> ticket) {
      this.addWorldCallback(world, ticket, 0);
   }

   @Override
   public void addWorldCallback(Level world, ToIntFunction<Level> ticket, int delay) {
      if (delay >= 0) {
         TreeMap<Long, List<ToIntFunction<Level>>> map = this.worldCallbacks.computeIfAbsent(world, k -> CollectionUtils.createTreeMap());
         long timeStamp = world.getGameTime() + (long)delay;
         List<ToIntFunction<Level>> list = map.computeIfAbsent(timeStamp, k -> new ArrayList<>());
         list.add(ticket);
      }
   }

   @Override
   public void addServerCallback(ToIntFunction<MinecraftServer> ticket) {
      this.addServerCallback(ticket, 0);
   }

   @Override
   public void addServerCallback(ToIntFunction<MinecraftServer> ticket, int delay) {
      if (delay >= 0) {
         long timeStamp = this.serverTick + (long)delay;
         List<ToIntFunction<MinecraftServer>> list = this.serverCallbacks.computeIfAbsent(Long.valueOf(timeStamp), k -> new ArrayList<>());
         list.add(ticket);
      }
   }

   @Override
   public void addClientCallback(IntSupplier ticket) {
      this.addClientCallback(ticket, 0);
   }

   @Override
   public void addClientCallback(IntSupplier ticket, int delay) {
      if (delay >= 0) {
         long timeStamp = this.clientTicker + (long)delay;
         List<IntSupplier> list = this.clientCallbacks.computeIfAbsent(Long.valueOf(timeStamp), k -> new ArrayList<>());
         list.add(ticket);
      }
   }

   public void onWorldTick(Level world) {
      TreeMap<Long, List<ToIntFunction<Level>>> map = this.worldCallbacks.get(world);
      if (map != null && !map.isEmpty()) {
         long timeStamp = world.getGameTime();

         while (!map.isEmpty()) {
            Entry<Long, List<ToIntFunction<Level>>> entry = map.firstEntry();
            if (entry.getKey() > timeStamp) {
               return;
            }

            List<ToIntFunction<Level>> toExecute = entry.getValue();
            map.pollFirstEntry();

            for (ToIntFunction<Level> ticket : toExecute) {
               int delay = ticket.applyAsInt(world);
               if (delay > 0) {
                  this.addWorldCallback(world, ticket, delay);
               }
            }
         }
      }
   }

   public void onClientTick() {
      this.clientTicker++;

      while (!this.serverCallbacks.isEmpty()) {
         Entry<Long, List<IntSupplier>> entry = this.clientCallbacks.firstEntry();
         if (entry.getKey() > this.clientTicker) {
            return;
         }

         List<IntSupplier> toExecute = entry.getValue();
         this.serverCallbacks.pollFirstEntry();

         for (IntSupplier ticket : toExecute) {
            int delay = ticket.getAsInt();
            if (delay > 0) {
               this.addClientCallback(ticket, delay);
            }
         }
      }
   }

   public void onWorldUnload(Level world) {
      this.worldCallbacks.remove(world);
   }

   public void onServerTick(MinecraftServer server) {
      this.serverTick++;

      while (!this.serverCallbacks.isEmpty()) {
         Entry<Long, List<ToIntFunction<MinecraftServer>>> entry = this.serverCallbacks.firstEntry();
         if (entry.getKey() > this.serverTick) {
            return;
         }

         List<ToIntFunction<MinecraftServer>> toExecute = entry.getValue();
         this.serverCallbacks.pollFirstEntry();

         for (ToIntFunction<MinecraftServer> ticket : toExecute) {
            int delay = ticket.applyAsInt(server);
            if (delay > 0) {
               this.addServerCallback(ticket, delay);
            }
         }
      }
   }

   public void onServerStopped() {
      this.serverTick = 0L;
      this.serverCallbacks.clear();
      this.worldCallbacks.clear();
   }
}
