package ic2.core.wiki.helper.text;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.util.FormattedCharSequence;

public class ResetHeaderObject implements ITextObject {
   boolean applyInstantly;

   public ResetHeaderObject(boolean applyInstantly) {
      this.applyInstantly = applyInstantly;
   }

   public boolean shouldApplyInstantly() {
      return this.applyInstantly;
   }

   @Override
   public List<FormattedCharSequence> getText() {
      return ObjectLists.emptyList();
   }
}
