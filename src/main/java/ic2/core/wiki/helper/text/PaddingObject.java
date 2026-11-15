package ic2.core.wiki.helper.text;

import ic2.core.wiki.components.PaddingComponent;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.FormattedCharSequence;

public class PaddingObject implements ITextObject {
   int padding;

   public PaddingObject(int padding) {
      this.padding = padding;
   }

   @Override
   public List<FormattedCharSequence> getText() {
      return Collections.emptyList();
   }

   public PaddingComponent create() {
      return new PaddingComponent(this.padding);
   }
}
