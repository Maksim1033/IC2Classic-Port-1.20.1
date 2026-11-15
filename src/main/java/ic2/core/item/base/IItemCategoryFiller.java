package ic2.core.item.base;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IItemCategoryFiller {
   default boolean allowedIn(CreativeModeTab group) {
      return true;
   }

   default boolean m_220152_(CreativeModeTab group) {
      return this.allowedIn(group);
   }

   default void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this instanceof Item item) {
         items.add(new ItemStack(item));
      }
   }
}
