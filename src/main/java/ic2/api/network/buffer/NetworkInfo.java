package ic2.api.network.buffer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface NetworkInfo {
   NetworkInfo.BitLevel value() default NetworkInfo.BitLevel.BIT_0;

   public static enum BitLevel {
      BIT_0(0),
      BIT_8(1),
      BIT_16(2),
      BIT_24(3),
      BIT_32(4),
      BIT_40(5),
      BIT_48(6),
      BIT_56(7),
      BIT_64(8);

      final int index;
      final long maxNumber;
      public static final NetworkInfo.BitLevel[] LEVELS = new NetworkInfo.BitLevel[values().length];

      private BitLevel(int lvl) {
         this.index = lvl;
         this.maxNumber = 1L << lvl * 8;
      }

      public int getIndex() {
         return this.index;
      }

      public boolean isSkip() {
         return this.index == 0;
      }

      public boolean isValid(NetworkInfo.BitLevel limit) {
         return this.index == 0 ? false : this.index < limit.index;
      }

      public long limitNumber(long input) {
         if (this.index == 0) {
            return 0L;
         } else {
            return input >= this.maxNumber ? this.maxNumber - 1L : input;
         }
      }

      public static NetworkInfo.BitLevel getLevel(int index) {
         return index >= 0 && index <= 8 ? LEVELS[index] : BIT_32;
      }

      static {
         for (NetworkInfo.BitLevel level : values()) {
            LEVELS[level.getIndex()] = level;
         }
      }
   }
}
