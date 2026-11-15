package ic2.core.wiki.helper.text;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.util.FormattedCharSequence;

public class NewPageObject implements ITextObject {
   @Override
   public List<FormattedCharSequence> getText() {
      return ObjectLists.emptyList();
   }
}
