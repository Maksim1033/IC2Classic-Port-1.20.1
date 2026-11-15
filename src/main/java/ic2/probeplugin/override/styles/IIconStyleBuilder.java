package ic2.probeplugin.override.styles;

import mcjty.theoneprobe.api.Color;
import mcjty.theoneprobe.api.IIconStyle;
import mcjty.theoneprobe.apiimpl.styles.IconStyle;

public interface IIconStyleBuilder {
   static IIconStyle create() {
      return new IconStyle();
   }

   static IIconStyle color(Color color) {
      return new IconStyle().color(color);
   }

   static IIconStyle color(int color) {
      return new IconStyle().color(color);
   }

   static IIconStyle bounds(int width, int height) {
      return new IconStyle().width(width).height(height);
   }

   static IIconStyle texture(int texWidth, int texHeight) {
      return new IconStyle().textureWidth(texWidth).textureHeight(texHeight);
   }
}
