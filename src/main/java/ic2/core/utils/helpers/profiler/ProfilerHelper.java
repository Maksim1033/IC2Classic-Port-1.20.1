package ic2.core.utils.helpers.profiler;

import ic2.core.block.base.features.ITickListener;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ProfilerHelper {
   public static final ProfilerHelper INSTANCE = new ProfilerHelper();
   Map<Level, Long2ObjectMap<ProfilerHelper.ProfilerInstance>> tracked = CollectionUtils.<Level, Long2ObjectMap<ProfilerHelper.ProfilerInstance>>createLinkedMap();

   private ProfilerHelper() {
   }

   public ProfilerHelper.ProfilerInstance getOrCreate(BlockEntity tile) {
      Long2ObjectMap<ProfilerHelper.ProfilerInstance> dataMap = this.tracked.computeIfAbsent(tile.getLevel(), k -> new Long2ObjectOpenHashMap());
      ProfilerHelper.ProfilerInstance instance = (ProfilerHelper.ProfilerInstance)dataMap.get(tile.getBlockPos().asLong());
      if (instance == null) {
         instance = new ProfilerHelper.ProfilerInstance();
         dataMap.put(tile.getBlockPos().asLong(), instance);
      }

      return instance;
   }

   public void onTickEnded() {
      for (Long2ObjectMap<ProfilerHelper.ProfilerInstance> entry : this.tracked.values()) {
         ObjectIterator var3 = entry.values().iterator();

         while (var3.hasNext()) {
            ProfilerHelper.ProfilerInstance instance = (ProfilerHelper.ProfilerInstance)var3.next();
            instance.onTickFinished();
         }
      }

      for (ITickListener.GlobalLag lag : ITickListener.GLOBAL_LAG.values()) {
         lag.onTickFinished();
      }

      ITickListener.SERVER_LAG.onTickFinished();
   }

   public void onServerStopped() {
      this.tracked.clear();
   }

   public void onWorldUnload(Level world) {
      this.tracked.remove(world);
   }

   public static class ProfilerInstance {
      Object2ObjectLinkedOpenHashMap<String, ProfilerEntry> cache = new Object2ObjectLinkedOpenHashMap();
      Stack<ProfilerEntry> activeProfilers = new ObjectArrayList();

      public ProfilerEntry getMain() {
         if (this.cache.isEmpty()) {
            return null;
         } else {
            ProfilerEntry entry = (ProfilerEntry)this.cache.get(this.cache.firstKey());

            while (entry.getParent() != null) {
               entry = entry.getParent();
            }

            return entry;
         }
      }

      public void next(String name) {
         this.stop();
         this.start(name);
      }

      public void start(String name) {
         String entry = (this.activeProfilers.isEmpty() ? "" : ((ProfilerEntry)this.activeProfilers.top()).getPathName() + "/") + name;
         ProfilerEntry active = (ProfilerEntry)this.cache.get(entry);
         if (active == null) {
            active = new ProfilerEntry(name, this);
            if (!this.activeProfilers.isEmpty()) {
               ((ProfilerEntry)this.activeProfilers.top()).addChild(active);
            }

            this.cache.put(entry, active);
         }

         this.activeProfilers.push(active);
         active.start();
      }

      public void stop() {
         if (!this.activeProfilers.isEmpty()) {
            ((ProfilerEntry)this.activeProfilers.pop()).stop();
         }
      }

      public void onTickFinished() {
         ObjectIterator var1 = this.cache.values().iterator();

         while (var1.hasNext()) {
            ProfilerEntry entry = (ProfilerEntry)var1.next();
            entry.onFrameFinished();
         }
      }
   }
}
