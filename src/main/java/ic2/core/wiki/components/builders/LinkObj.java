package ic2.core.wiki.components.builders;

import ic2.core.wiki.base.IPageBuilder;
import net.minecraft.resources.ResourceLocation;

public class LinkObj implements IWikiObj {
   ResourceLocation link;

   public LinkObj(String mod, String path) {
      this(new ResourceLocation(mod, path));
   }

   public LinkObj(ResourceLocation link) {
      this.link = link;
   }

   @Override
   public void buildComponents(IPageBuilder builder) {
      builder.setPageLink(this.link);
   }
}
