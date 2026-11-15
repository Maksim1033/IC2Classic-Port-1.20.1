package ic2.probeplugin.override.styles;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.ITextStyle;
import mcjty.theoneprobe.apiimpl.styles.TextStyle;

public interface ITextStyleBuilder {
   static ITextStyle create() {
      return new TextStyle();
   }

   static ITextStyle aligned(ElementAlignment align) {
      return new TextStyle().alignment(align);
   }

   static ITextStyle width(Integer width) {
      return new TextStyle().width(width);
   }

   static ITextStyle height(Integer height) {
      return new TextStyle().height(height);
   }

   static ITextStyle bounds(Integer width, Integer height) {
      return new TextStyle().width(width).height(height);
   }

   static ITextStyle padding(int padding) {
      return new TextStyle().padding(padding);
   }

   static ITextStyle padding(int xPadding, int yPadding) {
      return new TextStyle().hPadding(yPadding).vPadding(xPadding);
   }

   static ITextStyle padding(int top, int bottom, int left, int right) {
      return new TextStyle().topPadding(top).bottomPadding(bottom).leftPadding(left).rightPadding(right);
   }
}
