package ic2.api.recipes.registries;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.IInput;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface IRecipeFilter {
   int SIMPLE = 1;
   int FILTER = 2;
   int BOTH = 3;

   void add(ItemLike... var1);

   void add(IInput... var1);

   void remove(ItemStack var1, int var2);

   boolean contains(ItemStack var1);

   List<ItemStack> getFilteredItems();

   void clear();

   JsonObject serialize();

   void read(JsonObject var1);
}
