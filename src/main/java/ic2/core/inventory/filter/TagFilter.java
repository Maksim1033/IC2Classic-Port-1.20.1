package ic2.core.inventory.filter;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagFilter implements IFilter {
   TagKey<Item> tag;

   public TagFilter(TagKey<Item> tag) {
      this.tag = tag;
   }

   @Override
   public boolean matches(ItemStack input) {
      return input.is(this.tag);
   }
}
