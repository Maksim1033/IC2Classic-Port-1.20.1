package ic2.core.wiki.components.builders;

import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.base.IPageBuilder;
import ic2.core.wiki.components.HeaderComponent;
import ic2.core.wiki.components.TextComponent;
import ic2.core.wiki.helper.WikiTextParser;
import ic2.core.wiki.helper.text.HeaderTextObject;
import ic2.core.wiki.helper.text.ITextObject;
import ic2.core.wiki.helper.text.ImageTextObject;
import ic2.core.wiki.helper.text.NewPageObject;
import ic2.core.wiki.helper.text.PaddingObject;
import ic2.core.wiki.helper.text.ResetHeaderObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class TextObj implements IWikiObj {
   Component text;
   Component header;
   boolean cutOff;
   int maxWidth = 118;

   public TextObj(String s) {
      this(Component.translatable(s));
   }

   public TextObj(String s, ChatFormatting... formats) {
      this(Component.translatable(s).withStyle(formats));
   }

   public TextObj(Component text) {
      this.text = text;
   }

   public TextObj setCutOff() {
      this.cutOff = true;
      return this;
   }

   public TextObj setMaxWidth(int pixels) {
      this.maxWidth = pixels;
      return this;
   }

   public TextObj withHeader(String text) {
      return this.withHeader(Component.translatable(text).withStyle(ChatFormatting.BOLD));
   }

   public TextObj withHeader(Component header) {
      this.header = header;
      return this;
   }

   @Override
   public void provideLang(Consumer<Component> comp) {
      comp.accept(this.text);
      if (this.header != null) {
         comp.accept(this.header);
      }
   }

   @Override
   public void buildComponents(IPageBuilder builder) {
      List<ITextObject> pages = WikiTextParser.convertMultiline(this.text, this.maxWidth);
      if (!pages.isEmpty()) {
         int rowsLeft = builder.getHeightLeft() / 9;
         if (pages.size() == 1 && rowsLeft >= pages.get(0).getText().size()) {
            builder.addComponent(new TextComponent(pages.get(0).getText()));
         } else if (this.cutOff) {
            builder.addComponent(new TextComponent(new ObjectArrayList(pages.get(0).getText().subList(0, rowsLeft))));
         } else {
            Component currentHeader = this.header;
            int i = 0;

            for (int m = pages.size(); i < m; i++) {
               ITextObject obj = pages.get(i);
               if (obj instanceof HeaderTextObject object) {
                  currentHeader = object.getHeader();
                  if (object.shouldApplyInstantly()) {
                     builder.clearPage();
                     builder.addComponent(new HeaderComponent(currentHeader));
                  }
               } else if (obj instanceof ResetHeaderObject objectx) {
                  currentHeader = this.header;
                  if (objectx.shouldApplyInstantly()) {
                     builder.clearPage();
                     if (currentHeader != null) {
                        builder.addComponent(new HeaderComponent(currentHeader));
                     }
                  }
               } else if (obj instanceof ImageTextObject objectxx) {
                  builder.addComponent(objectxx.createComponent());
               } else if (obj instanceof NewPageObject) {
                  builder.setEndOfPage();
               } else if (obj instanceof PaddingObject objectxx) {
                  builder.addComponent(objectxx.create());
               } else {
                  List<FormattedCharSequence> lines = obj.getText();

                  for (int offset = 0; offset < lines.size(); rowsLeft = builder.getHeightLeft() / 9) {
                     List<FormattedCharSequence> subList = CollectionUtils.<FormattedCharSequence>createList();

                     for (int j = 0; j + offset < lines.size() && j < rowsLeft; j++) {
                        subList.add(lines.get(j + offset));
                     }

                     offset += subList.size();
                     builder.addComponent(new TextComponent(subList));
                     if (offset < lines.size()) {
                        builder.setEndOfPage();
                        if (currentHeader != null) {
                           builder.addComponent(new HeaderComponent(currentHeader));
                        }
                     }
                  }
               }

               rowsLeft = builder.getHeightLeft() / 9;
            }
         }
      }
   }
}
