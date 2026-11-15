package ic2.core.wiki.components.builders;

import ic2.api.crops.ICrop;
import ic2.core.wiki.base.IPageBuilder;
import ic2.core.wiki.components.CropComponent;

public class CropObj implements IWikiObj {
   ICrop crop;

   public CropObj(ICrop crop) {
      this.crop = crop;
   }

   @Override
   public void buildComponents(IPageBuilder builder) {
      builder.addComponent(new CropComponent(this.crop));
   }
}
