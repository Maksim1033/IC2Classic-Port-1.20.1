package ic2.core.platform.events;

import ic2.core.block.base.features.multiblock.IStructureListener;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.MobSpawnEvent.PositionCheck;
import net.minecraftforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;

public class StructureManager {
   public static final StructureManager INSTANCE = new StructureManager();
   Map<LevelAccessor, StructureManager.StructureListeners> listeners = CollectionUtils.<LevelAccessor, StructureManager.StructureListeners>createMap();

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onBlockUpdate(NeighborNotifyEvent event) {
      this.getListeners(event.getLevel()).onTrigger(event.getPos(), event.getState());
   }

   @SubscribeEvent
   public void onWorldUnload(Unload event) {
      StructureManager.StructureListeners listener = this.listeners.remove(event.getLevel());
      if (listener != null) {
         listener.clear();
      }
   }

   @SubscribeEvent
   public void canSpawnEvent(PositionCheck event) {
      if (event.getSpawnType() == MobSpawnType.NATURAL
         && this.getListeners(event.getLevel()).isBlocked(new BlockPos((int)event.getX(), (int)event.getY(), (int)event.getZ()).below())) {
         event.setResult(Result.DENY);
      }
   }

   public IStructureListener getListener(Level world, BlockPos pos) {
      return this.getListeners(world).getListener(pos);
   }

   public void onServerStopped() {
      this.listeners.clear();
   }

   public void addListeners(Level world, LongCollection positions, IStructureListener listener) {
      if (listener != null && !positions.isEmpty()) {
         this.getListeners(world).add(positions, listener);
      }
   }

   public boolean isBlocked(Level world, LongList positions) {
      return this.getListeners(world).isBlocked(positions);
   }

   public void clearListeners(Level world, IStructureListener listener) {
      this.getListeners(world).removeListeners(listener);
   }

   private StructureManager.StructureListeners getListeners(LevelAccessor world) {
      StructureManager.StructureListeners listener = this.listeners.get(world);
      if (listener == null) {
         listener = new StructureManager.StructureListeners();
         this.listeners.put(world, listener);
      }

      return listener;
   }

   public static class StructureListeners {
      Long2ObjectMap<IStructureListener> listeners = new Long2ObjectOpenHashMap();
      Map<IStructureListener, LongSet> listenerToPos = CollectionUtils.<IStructureListener, LongSet>createMap();

      public void onTrigger(BlockPos pos, BlockState newState) {
         IStructureListener listener = (IStructureListener)this.listeners.get(pos.asLong());
         if (listener != null) {
            listener.markStructureDirty(pos, newState);
         }
      }

      public void add(LongCollection positions, IStructureListener listener) {
         LongSet set = new LongOpenHashSet();
         LongIterator var4 = positions.iterator();

         while (var4.hasNext()) {
            long entry = (Long)var4.next();
            if (this.listeners.putIfAbsent(entry, listener) == null) {
               set.add(entry);
            }
         }

         this.listenerToPos.put(listener, set);
      }

      public boolean isBlocked(LongList positions) {
         return positions.removeAll(this.listeners.keySet());
      }

      public boolean isBlocked(BlockPos pos) {
         return this.listeners.containsKey(pos.asLong());
      }

      public IStructureListener getListener(BlockPos pos) {
         return (IStructureListener)this.listeners.get(pos.asLong());
      }

      public void removeListeners(IStructureListener listener) {
         LongSet set = this.listenerToPos.remove(listener);
         if (set != null) {
            this.listeners.keySet().removeAll(set);
         }
      }

      public void clear() {
         this.listeners.clear();
         this.listeners = null;
      }
   }
}
