package ic2.core.utils.tooltips;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextHelper {
   public static String convertText(Component component) {
      TextHelper.StringConsumer consumer = new TextHelper.StringConsumer();
      component.getVisualOrderText().accept(consumer);
      return consumer.getBuilder().toString();
   }

   public static void splitString(Component component, Consumer<Component> listener) {
      for (String s : convertText(component).split("\n")) {
         listener.accept(Component.literal(s));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class StringConsumer implements FormattedCharSink {
      StringBuilder builder = new StringBuilder();
      ChatFormatting formatting = ChatFormatting.RESET;

      public boolean accept(int index, Style style, int character) {
         ChatFormatting format = this.getFormat(style);
         if (format != this.formatting) {
            this.formatting = format;
            this.builder.append(format.toString());
         }

         this.builder.append((char)character);
         return true;
      }

      protected ChatFormatting getFormat(Style style) {
         ChatFormatting format = style.getColor() == null ? null : ChatFormatting.getByName(style.getColor().serialize());
         return format == null ? ChatFormatting.RESET : format;
      }

      public StringBuilder getBuilder() {
         return this.builder;
      }
   }
}
