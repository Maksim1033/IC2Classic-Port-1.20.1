package ic2.probeplugin.override.styles;

import mcjty.theoneprobe.api.Color;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.ILayoutStyle;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;

public interface ILayoutStyleBuilder {
   static ILayoutStyle create() {
      return new LayoutStyle();
   }

   static ILayoutStyle border(Color color) {
      return new LayoutStyle().borderColor(color);
   }

   static ILayoutStyle border(Integer color) {
      return new LayoutStyle().borderColor(color);
   }

   static ILayoutStyle spacing(int spacing) {
      return new LayoutStyle().spacing(spacing);
   }

   static ILayoutStyle aligned(ElementAlignment align) {
      return new LayoutStyle().alignment(align);
   }

   static ILayoutStyle padding(int padding) {
      return new LayoutStyle().padding(padding);
   }

   static ILayoutStyle padding(int xPadding, int yPadding) {
      return new LayoutStyle().hPadding(xPadding).vPadding(yPadding);
   }

   static ILayoutStyle padding(int top, int bottom, int left, int right) {
      return new LayoutStyle().topPadding(top).bottomPadding(bottom).leftPadding(left).rightPadding(right);
   }
}
