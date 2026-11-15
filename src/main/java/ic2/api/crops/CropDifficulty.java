package ic2.api.crops;

import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class CropDifficulty {
   int level;

   public CropDifficulty(int level) {
      this.level = level;
   }

   public int getWeedChance() {
      switch (this.level) {
         case 0:
            return 25;
         case 1:
            return 50;
         case 2:
         default:
            return 100;
         case 3:
            return 200;
      }
   }

   public int getCropGrowth(int value) {
      switch (this.level) {
         case 0:
            return value * 2;
         case 1:
            return (int)((double)value * 1.3);
         case 2:
         default:
            return value;
         case 3:
            return value == 0 ? 0 : (int)Math.max(1.0, (double)value * 0.75);
      }
   }

   public int getBreedingDifficulty() {
      switch (this.level) {
         case 0:
            return 10;
         case 1:
            return 6;
         case 2:
         default:
            return 4;
         case 3:
            return 2;
      }
   }

   public int[] getCropStats(List<ICropTile> crops, RandomSource rand) {
      int[] result = new int[3];
      int size = crops.size();

      for (int k = 0; k < size; k++) {
         ICropTile tile = crops.get(k);
         result[0] += tile.getGrowthStat();
         result[1] += tile.getResistanceStat();
         result[2] += tile.getGainStat();
      }

      for (int i = 0; i < result.length; i++) {
         int value = result[i];
         value /= size;
         value += this.getRandomBoost(rand, size);
         result[i] = (byte)Mth.clamp(value, 0, 31);
      }

      return result;
   }

   private int getRandomBoost(RandomSource rand, int count) {
      switch (this.level) {
         case 0:
            return rand.nextInt(1 + 2 * count);
         case 1:
            return rand.nextInt(1 + 2 * count) - count / 2;
         case 2:
         default:
            return rand.nextInt(1 + 2 * count) - count;
         case 3:
            int value = rand.nextInt(1 + 2 * count) - count;
            return rand.nextBoolean() ? 0 : value;
      }
   }
}
