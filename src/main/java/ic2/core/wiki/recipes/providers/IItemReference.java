package ic2.core.wiki.recipes.providers;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface IItemReference {
   List<ItemStack> getItemList();
}
