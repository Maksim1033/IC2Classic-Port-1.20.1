package ic2.core.wiki.components.builders;

import ic2.core.wiki.base.IPageBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class PreviewObj implements IWikiObj {
   ItemLike[] array;

   public PreviewObj(ItemLike... array) {
      this.array = array;
   }

   public PreviewObj(ItemStack... array) {
      this.array = new ItemLike[array.length];
      int i = 0;

      for (int m = array.length; i < m; i++) {
         this.array[i] = array[i].getItem();
      }
   }

   @Override
   public void buildComponents(IPageBuilder builder) {
      for (ItemLike itemLike : this.array) {
         builder.markPreview(itemLike);
      }
   }
}
