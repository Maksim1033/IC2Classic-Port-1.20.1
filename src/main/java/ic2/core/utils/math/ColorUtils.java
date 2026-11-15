package ic2.core.utils.math;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class ColorUtils {
   static final float DIVIDER = 0.003921569F;
   public static final int WHITE = -1;
   public static final int LIGHT_GRAY = rgb(192, 192, 192);
   public static final int GRAY = rgb(128, 128, 128);
   public static final int DARK_GRAY = rgb(64, 64, 64);
   public static final int BLACK = -16777216;
   public static final int RED = rgb(255, 0, 0);
   public static final int PINK = rgb(255, 175, 175);
   public static final int ORANGE = rgb(255, 200, 0);
   public static final int YELLOW = rgb(255, 255, 0);
   public static final int GREEN = rgb(0, 255, 0);
   public static final int MAGENTA = rgb(255, 0, 255);
   public static final int CYAN = rgb(0, 255, 255);
   public static final int BLUE = rgb(0, 0, 255);

   public static int rgb(int r, int g, int b) {
      return rgb(r, g, b, 255);
   }

   public static int rgb(int r, int g, int b, int a) {
      return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
   }

   public static int rgb(float r, float g, float b) {
      return rgb(r, g, b, 1.0F);
   }

   public static int rgb(float r, float g, float b, float a) {
      return rgb((int)((double)(r * 255.0F) + 0.5), (int)((double)(g * 255.0F) + 0.5), (int)((double)(b * 255.0F) + 0.5), (int)((double)(a * 255.0F) + 0.5));
   }

   public static float[] rgbFloat(int color) {
      return new float[]{getFloatR(color), getFloatG(color), getFloatB(color)};
   }

   public static int getRed(int rgb) {
      return rgb >> 16 & 0xFF;
   }

   public static int getGreen(int rgb) {
      return rgb >> 8 & 0xFF;
   }

   public static int getBlue(int rgb) {
      return rgb & 0xFF;
   }

   public static int getAlpha(int rgb) {
      return rgb >> 24 & 0xFF;
   }

   public static float getFloatR(int rgb) {
      return (float)(rgb >> 16 & 0xFF) * 0.003921569F;
   }

   public static float getFloatG(int rgb) {
      return (float)(rgb >> 8 & 0xFF) * 0.003921569F;
   }

   public static float getFloatB(int rgb) {
      return (float)(rgb & 0xFF) * 0.003921569F;
   }

   public static float getFloatA(int rgb) {
      return (float)(rgb >> 24 & 0xFF) * 0.003921569F;
   }

   public static boolean needsDarkColor(int rgba) {
      return getBrightness(rgba) >= 130;
   }

   public static int getBrightness(int rgba) {
      return getBrightness(rgba >> 16 & 0xFF, rgba >> 8 & 0xFF, rgba & 0xFF);
   }

   public static int getBrightness(int r, int g, int b) {
      return (int)Math.sqrt((double)((float)(r * r) * 0.241F + (float)(g * g) * 0.691F + (float)(b * b) * 0.068F));
   }

   public static int mixColors(int from, int to, float progress) {
      float weight0 = 1.0F - progress;
      int r = (int)((float)(from >> 16 & 0xFF) * weight0 + (float)(to >> 16 & 0xFF) * progress);
      int g = (int)((float)(from >> 8 & 0xFF) * weight0 + (float)(to >> 8 & 0xFF) * progress);
      int b = (int)((float)(from & 0xFF) * weight0 + (float)(to & 0xFF) * progress);
      int a = (int)((float)(from >> 24 & 0xFF) * weight0 + (float)(to >> 24 & 0xFF) * progress);
      return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
   }

   public static int doubleDarker(int color) {
      return darker(darker(color));
   }

   public static int tripleDarker(int color) {
      return darker(darker(darker(color)));
   }

   public static int darker(int color) {
      return darker(color, 0.7F);
   }

   public static int darker(int color, float factor) {
      if (Float.compare(factor, 1.0F) == 0) {
         return color;
      } else {
         int r = Math.max(0, (int)((float)(color >> 16 & 0xFF) * factor));
         int g = Math.max(0, (int)((float)(color >> 8 & 0xFF) * factor));
         int b = Math.max(0, (int)((float)(color & 0xFF) * factor));
         return color & 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
      }
   }

   public static int doubleBrighter(int color) {
      return brighter(brighter(color));
   }

   public static int brighter(int color) {
      return brighter(color, 0.7F);
   }

   public static int brighter(int color, float factor) {
      if (Float.compare(factor, 1.0F) == 0) {
         return color;
      } else {
         int r = color >> 16 & 0xFF;
         int g = color >> 8 & 0xFF;
         int b = color & 0xFF;
         int i = (int)(1.0 / (1.0 - (double)factor));
         if (r == 0 && g == 0 && b == 0) {
            return color & 0xFF000000 | (i & 0xFF) << 16 | (i & 0xFF) << 8 | i & 0xFF;
         } else {
            if (r > 0 && r < i) {
               r = i;
            }

            if (g > 0 && g < i) {
               g = i;
            }

            if (b > 0 && b < i) {
               b = i;
            }

            return color & 0xFF000000
               | Math.min(255, (int)((float)r / factor)) << 16
               | Math.min(255, (int)((float)g / factor)) << 8
               | Math.min(255, (int)((float)b / factor));
         }
      }
   }

   public static Style toColor(int color) {
      return Style.EMPTY.withColor(TextColor.fromRgb(color & 16777215));
   }

   public static String toHex(int color) {
      return "#" + Integer.toHexString(16777216 | color & 16777215).substring(1);
   }
}
