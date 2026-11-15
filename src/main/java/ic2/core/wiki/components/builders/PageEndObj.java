package ic2.core.wiki.components.builders;

import ic2.core.wiki.base.IPageBuilder;

public class PageEndObj implements IWikiObj {
   @Override
   public void buildComponents(IPageBuilder builder) {
      builder.setEndOfPage();
   }
}
