package ic2.core.wiki.helper.text;

import java.util.List;
import net.minecraft.util.FormattedCharSequence;

public class TextObject implements ITextObject {
   List<FormattedCharSequence> lines;

   public TextObject(List<FormattedCharSequence> lines) {
      this.lines = lines;
   }

   @Override
   public List<FormattedCharSequence> getText() {
      return this.lines;
   }
}
