package ic2.core.platform.wind.impl;

import ic2.core.IC2;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;

public class OverworldCalculation extends RealisticWindCalculation {
   public OverworldCalculation(Level world) {
      int i = 0;

      for (int m = IC2.CONFIG.windStreams.get(); i <= m; i++) {
         this.streams.add(new OverworldCalculation.WindStream(world));
      }
   }

   public static class WindStream extends RealisticWindCalculation.HorizontalStream {
      int ticksLeft;

      public WindStream(Level world) {
         LevelData info = world.getLevelData();
         RandomSource rand = world.random;
         this.rotation.set((float)rand.nextInt(360));
         this.yLevel.set((float)Math.min(32 + rand.nextInt(world.getMaxBuildHeight()), world.getMaxBuildHeight()));
         this.thickness.set((float)Math.max(Math.min(rand.nextInt(8) - (rand.nextInt(14) - rand.nextInt(5)), 7), -7));
         this.ticksLeft = rand.nextInt(IC2.CONFIG.windChangeTime.get());
         float newSpeed = (float)rand.nextInt(109);
         if (info.isThundering()) {
            newSpeed *= 1.5F;
         } else if (info.isRaining()) {
            newSpeed *= 1.25F;
         }

         this.speed.set((double)newSpeed);
      }

      @Override
      public void onUpdate(Level world) {
         super.onUpdate(world);
         if (this.ticksLeft-- <= 0) {
            LevelData info = world.getLevelData();
            RandomSource rand = world.random;
            this.rotation.setNext((float)rand.nextInt(360));
            this.yLevel.setNext((float)Math.min(32 + rand.nextInt(world.getMaxBuildHeight()), world.getMaxBuildHeight()));
            this.thickness.setNext((float)Math.max(Math.min(rand.nextInt(8) - (rand.nextInt(14) - rand.nextInt(5)), 7), -7));
            this.ticksLeft = rand.nextInt(IC2.CONFIG.windChangeTime.get());
            float newSpeed = (float)rand.nextInt(109);
            if (info.isThundering()) {
               newSpeed *= 1.5F;
            } else if (info.isRaining()) {
               newSpeed *= 1.25F;
            }

            this.speed.setNext((double)newSpeed);
         }
      }
   }
}
