package ic2.api.recipes.ingridients.inputs;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientInput implements IInput {
   Ingredient entry;
   int size;

   public IngredientInput(JsonObject obj) {
      this(Ingredient.fromJson(obj.get("ingredient")), obj.get("size").getAsInt());
   }

   public IngredientInput(FriendlyByteBuf buffer) {
      this.entry = Ingredient.fromNetwork(buffer);
      this.size = buffer.readByte();
   }

   public IngredientInput(Ingredient entry, int size) {
      this.entry = entry;
      this.size = size;
   }

   @Override
   public List<ItemStack> getComponents() {
      return ObjectArrayList.wrap(this.entry.getItems());
   }

   @Override
   public Ingredient asIngredient() {
      return this.entry;
   }

   @Override
   public int getInputSize() {
      return this.size;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return this.entry.test(stack);
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      this.entry.toNetwork(buffer);
      buffer.writeByte(this.size);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.add("ingredient", this.entry.toJson());
      obj.addProperty("size", this.size);
      return obj;
   }
}
