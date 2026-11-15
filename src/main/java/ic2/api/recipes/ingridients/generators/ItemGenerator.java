package ic2.api.recipes.ingridients.generators;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemGenerator implements IOutputGenerator {
   Item item;
   int count;

   public ItemGenerator(JsonObject obj) {
      this(ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())), obj.get("size").getAsInt());
   }

   public ItemGenerator(ItemLike item) {
      this(item, 1);
   }

   public ItemGenerator(ItemLike item, int count) {
      this.item = item.asItem();
      this.count = count;
   }

   @Override
   public void addItems(Consumer<ItemStack> output) {
      output.accept(new ItemStack(this.item, this.count));
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.item).toString());
      obj.addProperty("size", this.count);
      return obj;
   }
}
