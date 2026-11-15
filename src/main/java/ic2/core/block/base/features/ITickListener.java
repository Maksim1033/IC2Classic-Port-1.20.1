package ic2.core.block.base.features;

import ic2.core.IC2;
import ic2.core.utils.collection.LongAverager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ITickListener extends IProfileListener {
   Map<Class<? extends ITickListener>, ITickListener.GlobalLag> GLOBAL_LAG = new Object2ObjectOpenHashMap();
   ITickListener.GlobalLag SERVER_LAG = new ITickListener.GlobalLag();
   boolean GLOBAL_PROFILE = true;
   boolean PROFILE = IC2.CONFIG.tileProfiler.get();

   void onTick();

   default void tick() {
      if (PROFILE) {
         long start = System.nanoTime();
         this.onTick();
         start = System.nanoTime() - start;
         this.onProfile(start);
         ITickListener.GlobalLag lag = GLOBAL_LAG.get(this.getClass());
         if (lag == null) {
            lag = new ITickListener.GlobalLag();
            GLOBAL_LAG.put((Class<? extends ITickListener>)this.getClass(), lag);
         }

         lag.add(start);
         SERVER_LAG.add(start);
      } else {
         this.onTick();
      }
   }

   boolean checkTicking();

   static <T extends BlockEntity> void update(Level level, BlockPos pos, BlockState state, T tile) {
      if (tile instanceof ITickListener listener) {
         if (listener.checkTicking()) {
            return;
         }

         listener.tick();
      }
   }

   public static final class GlobalLag {
      LongAverager averager = new LongAverager(20);
      LongAverager tiles = new LongAverager(20);
      int totalCount;
      long thisTick = 0L;

      public void onTickFinished() {
         this.averager.addEntry(this.thisTick);
         this.tiles.addEntry((long)this.totalCount);
         this.thisTick = 0L;
         this.totalCount = 0;
      }

      public void add(long time) {
         this.thisTick += time;
         this.totalCount++;
      }

      public long getGlobalTotal() {
         return this.averager.getCachedAverage();
      }

      public long getGlobalAverage() {
         long count = this.tiles.getCachedAverage();
         return count == 0L ? 0L : this.averager.getCachedAverage() / count;
      }
   }
}
