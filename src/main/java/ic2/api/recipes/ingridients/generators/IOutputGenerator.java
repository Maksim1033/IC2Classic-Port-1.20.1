package ic2.api.recipes.ingridients.generators;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

public interface IOutputGenerator {
   void addItems(Consumer<ItemStack> var1);

   JsonObject serialize();
}
