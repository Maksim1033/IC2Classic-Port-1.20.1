package ic2.core.utils.math;

import ic2.core.utils.helpers.Formatters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class MathUtils {
   public static final BiFunction<Integer, Integer, Integer> REMOVAL = MathUtils::merge;

   static Integer merge(Integer key, Integer value) {
      int result = key - value;
      return result <= 0 ? null : result;
   }

   public static int[] fromTo(int from, int to) {
      int[] array = new int[to - from];

      for (int i = 0; i < array.length; i++) {
         array[i] = i + from;
      }

      return array;
   }

   public static IntList fromToList(int from, int to) {
      IntList list = new IntArrayList(to - from);
      int i = 0;

      for (int m = to - from; i < m; i++) {
         list.add(from + i);
      }

      return list;
   }

   public static int sumUp(int base) {
      int sum = 0;

      for (int i = 1; i <= base; i++) {
         sum += i;
      }

      return sum;
   }

   public static <T> void shuffle(List<T> p_214674_, RandomSource p_214675_) {
      int i = p_214674_.size();

      for (int j = i; j > 1; j--) {
         int k = p_214675_.nextInt(j);
         p_214674_.set(j - 1, p_214674_.set(k, p_214674_.get(j - 1)));
      }
   }

   public static Vec3 divide(Vec3 source, double divider) {
      return new Vec3(source.x / divider, source.y / divider, source.z / divider);
   }

   public static BlockPos divide(BlockPos source, double divider) {
      return BlockPos.containing((double)source.getX() / divider, (double)source.getY() / divider, (double)source.getZ() / divider);
   }

   public static Vec3 clamp(Vec3 source, double min, double max) {
      return new Vec3(Mth.clamp(source.x, min, max), Mth.clamp(source.y, min, max), Mth.clamp(source.z, min, max));
   }

   public static long putIntoLong(long key, long value) {
      return key << 32 | value & 2147483647L;
   }

   public static int getIntKey(long input) {
      return (int)(input >> 32 & 2147483647L);
   }

   public static int getIntValue(long input) {
      return (int)(input & 2147483647L);
   }

   public static int putInt(int key, int value) {
      return key << 16 | value & 32767;
   }

   public static int getShortKey(int input) {
      return input >> 16 & 32767;
   }

   public static int getShortValue(int input) {
      return input & 32767;
   }

   public static int sign(int value) {
      return Integer.signum(value);
   }

   public static int sign(long value) {
      return Long.signum(value);
   }

   public static double easeInBack(double input) {
      double c1 = 1.70158;
      double c3 = c1 + 1.0;
      return c3 * input * input * input - c1 * input * input;
   }

   public static boolean isHexNumber(String s) {
      if (s.isEmpty()) {
         return true;
      } else {
         int i = 0;

         for (int m = s.length(); i < m; i++) {
            char charIn = s.charAt(i);
            if ((charIn < '0' || charIn > '9') && (charIn < 'a' || charIn > 'f') && (charIn < 'A' || charIn > 'F')) {
               return false;
            }
         }

         return true;
      }
   }

   public static String toLagStringProbe(String prefix, long lag) {
      return prefix
         + ": [ms="
         + Formatters.EU_READER_FORMAT.format(lag / 1000000L)
         + ", qs="
         + Formatters.EU_READER_FORMAT.format(lag / 1000L)
         + ", ns="
         + Formatters.EU_READER_FORMAT.format(lag)
         + "]";
   }

   public static String toLagString(String prefix, long lag) {
      return prefix + ": " + lag / 1000000L + "ms, " + lag / 1000L + "qs, " + lag + "ns";
   }
}
