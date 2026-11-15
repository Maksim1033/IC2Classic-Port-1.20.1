package ic2.core.item.base.features;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface ISonarProvider {
   BlockPos getStartPosition(ItemStack var1);

   int getViewRadius(ItemStack var1);

   boolean isDone(ItemStack var1);

   int getSonarColor(ItemStack var1);

   static ISonarProvider simple(int color, BlockPos startPos, int radius) {
      return new ISonarProvider.SimpleSonar(color, startPos, radius);
   }

   public static class SimpleSonar implements ISonarProvider {
      int color;
      boolean done = false;
      BlockPos startPos;
      int radius;

      public SimpleSonar(int color, BlockPos startPos, int radius) {
         this.color = color;
         this.startPos = startPos;
         this.radius = radius;
      }

      @Override
      public BlockPos getStartPosition(ItemStack stack) {
         return this.startPos;
      }

      @Override
      public int getViewRadius(ItemStack stack) {
         return this.radius;
      }

      @Override
      public boolean isDone(ItemStack stack) {
         if (this.done) {
            return true;
         } else {
            this.done = true;
            return false;
         }
      }

      @Override
      public int getSonarColor(ItemStack stack) {
         return this.color;
      }
   }
}
