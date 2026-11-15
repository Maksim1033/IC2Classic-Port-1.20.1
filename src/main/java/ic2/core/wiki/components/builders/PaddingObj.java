package ic2.core.wiki.components.builders;

import ic2.core.wiki.base.IPageBuilder;
import ic2.core.wiki.components.PaddingComponent;

public class PaddingObj implements IWikiObj {
   int padding;
   boolean optional;

   public PaddingObj(int padding) {
      this.padding = padding;
   }

   public PaddingObj setOptional() {
      this.optional = true;
      return this;
   }

   @Override
   public void buildComponents(IPageBuilder builder) {
      if (!this.optional || builder.hasComponents()) {
         builder.addComponent(new PaddingComponent(this.padding));
      }
   }
}
