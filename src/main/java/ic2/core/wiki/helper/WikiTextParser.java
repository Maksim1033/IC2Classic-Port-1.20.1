package ic2.core.wiki.helper;

import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.helper.text.HeaderTextObject;
import ic2.core.wiki.helper.text.ITextObject;
import ic2.core.wiki.helper.text.ImageTextObject;
import ic2.core.wiki.helper.text.NewPageObject;
import ic2.core.wiki.helper.text.PaddingObject;
import ic2.core.wiki.helper.text.ResetHeaderObject;
import ic2.core.wiki.helper.text.TextObject;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableObject;

@OnlyIn(Dist.CLIENT)
public class WikiTextParser {
   public static List<ITextObject> convertMultiline(Component comp, int maxWidth) {
      List<ITextObject> result = CollectionUtils.<ITextObject>createList();
      MutableObject<MutableComponent> transformed = new MutableObject(Component.literal(""));
      Runnable finish = () -> {
         result.add(new TextObject(Minecraft.getInstance().font.split((FormattedText)transformed.getValue(), maxWidth)));
         transformed.setValue(Component.literal(""));
      };
      comp.visit(
         (K, V) -> {
            String newString = V;

            for (int start = V.indexOf("\u00a7<"); start >= 0; start = newString.indexOf("\u00a7<")) {
               int end = newString.indexOf(">", start) + 1;
               if (end <= start) {
                  break;
               }

               String toReplace = newString.substring(start, end);
               String[] arguments = toReplace.substring(2, toReplace.length() - 1).split(";");
               if (arguments.length != 0) {
                  if (start > 0) {
                     ((MutableComponent)transformed.getValue()).append(Component.literal(newString.substring(0, start)).setStyle(K));
                  }

                  if (arguments[0].equalsIgnoreCase("redirect")) {
                     if (arguments.length >= 4) {
                        Style style = K.withClickEvent(createClickEvent(arguments))
                           .withBold(Boolean.parseBoolean(arguments[1]))
                           .withHoverEvent(createHoverEvent(arguments));
                        ((MutableComponent)transformed.getValue()).append(Component.translatable(arguments[2]).setStyle(style));
                     }
                  } else if (arguments[0].equalsIgnoreCase("newPage")) {
                     finish.run();
                     result.add(new NewPageObject());
                  } else if (arguments[0].equalsIgnoreCase("header")) {
                     finish.run();
                     if (arguments.length >= 2) {
                        result.add(
                           new HeaderTextObject(
                              Component.translatable(arguments[1]).withStyle(ChatFormatting.BOLD), arguments.length >= 3 && Boolean.parseBoolean(arguments[2])
                           )
                        );
                     }
                  } else if (!arguments[0].equalsIgnoreCase("reset_header")) {
                     if (arguments[0].equalsIgnoreCase("image")) {
                        finish.run();
                        if (arguments.length >= 3) {
                           ImageTextObject object = new ImageTextObject(arguments[1], Float.parseFloat(arguments[2]));
                           if (arguments.length >= 5) {
                              object.setOffset(Float.parseFloat(arguments[3]));
                              object.setWidth(Float.parseFloat(arguments[4]));
                           }

                           result.add(object);
                        }
                     } else if (arguments[0].equalsIgnoreCase("padding")) {
                        finish.run();
                        if (arguments.length >= 2) {
                           result.add(new PaddingObject(Integer.parseInt(arguments[1])));
                        }
                     } else if (arguments[0].equalsIgnoreCase("key") && arguments.length >= 2) {
                        KeyMapping mapping = getKey(arguments[1]);
                        if (mapping != null) {
                           ((MutableComponent)transformed.getValue()).append(mapping.getTranslatedKeyMessage().copy().setStyle(Style.EMPTY.withBold(true)));
                        }
                     }
                  } else {
                     finish.run();
                     result.add(new ResetHeaderObject(arguments.length >= 2 && Boolean.parseBoolean(arguments[1])));
                  }

                  newString = newString.substring(end);
                  if (newString.startsWith("\n")) {
                     newString = newString.substring(1);
                  }
               }
            }

            if (!newString.isEmpty()) {
               ((MutableComponent)transformed.getValue()).append(Component.literal(newString).setStyle(K));
            }

            return Optional.empty();
         },
         Style.EMPTY
      );
      finish.run();
      return result;
   }

   @OnlyIn(Dist.CLIENT)
   private static KeyMapping getKey(String name) {
      for (KeyMapping binding : Minecraft.getInstance().options.keyMappings) {
         if (binding.getName().equals(name)) {
            return binding;
         }
      }

      return null;
   }

   public static List<FormattedCharSequence> convert(Component comp, int maxWidth) {
      MutableComponent transformed = Component.literal("");
      comp.visit(
         (K, V) -> {
            String newString = V;

            for (int start = V.indexOf("\u00a7<"); start >= 0; start = newString.indexOf("\u00a7<")) {
               int end = newString.indexOf(">") + 1;
               if (end <= start) {
                  break;
               }

               String toReplace = newString.substring(start, end);
               String[] arguments = toReplace.substring(2, toReplace.length() - 1).split(";");
               if (arguments.length >= 4) {
                  if (start > 0) {
                     transformed.append(Component.literal(newString.substring(0, start)).setStyle(K));
                  }

                  if (arguments[0].equalsIgnoreCase("redirect")) {
                     Style style = K.withClickEvent(createClickEvent(arguments))
                        .withBold(Boolean.parseBoolean(arguments[1]))
                        .withHoverEvent(createHoverEvent(arguments));
                     transformed.append(Component.translatable(arguments[2]).setStyle(style));
                  }

                  newString = newString.substring(end);
               }
            }

            if (!newString.isEmpty()) {
               transformed.append(Component.literal(newString));
            }

            return Optional.empty();
         },
         Style.EMPTY
      );
      return Minecraft.getInstance().font.split(transformed, maxWidth);
   }

   private static ClickEvent createClickEvent(String[] data) {
      return new ClickEvent(Action.CHANGE_PAGE, data[0] + ";" + data[3] + ";" + (data.length > 4 ? data[4] : "0"));
   }

   private static HoverEvent createHoverEvent(String[] data) {
      return new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.translatable("tooltip.ic2.open").append(data[2]));
   }
}
