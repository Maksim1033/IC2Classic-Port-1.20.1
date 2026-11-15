package ic2.probeplugin.override.styles;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;

public interface IProgressStyleBuilder {
   static IProgressStyle create() {
      return new ProgressStyle();
   }

   static IProgressStyle armor() {
      return new ProgressStyle().armorBar(true);
   }

   static IProgressStyle life() {
      return new ProgressStyle().lifeBar(true);
   }

   static IProgressStyle aligned(ElementAlignment align) {
      return new ProgressStyle().alignment(align);
   }

   static IProgressStyle bounds(int width, int height) {
      return new ProgressStyle().bounds(width, height);
   }

   static IProgressStyle textOnly(String prefix) {
      return new ProgressStyle().prefix(prefix).numberFormat(NumberFormat.NONE);
   }

   static IProgressStyle text(String prefix, String suffix) {
      return new ProgressStyle().prefix(prefix).suffix(suffix);
   }
}
