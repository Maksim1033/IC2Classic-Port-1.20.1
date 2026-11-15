package ic2.api.recipes.ingridients.recipes;

import com.google.gson.JsonObject;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class RangeRecipeOutput extends BaseRecipeOutput {
   CompoundTag nbt;
   ItemStack output;
   int minValue;
   int maxValue;

   public RangeRecipeOutput(JsonObject obj) {
      IOutputGenerator generator = RecipeRegistry.INGREDIENTS.readOutputGenerator(obj.getAsJsonObject("output"));
      generator.addItems(T -> this.output = T);
      this.generators.add(generator);
      this.minValue = obj.get("minOut").getAsInt();
      this.maxValue = obj.get("maxOut").getAsInt();
      if (obj.has("nbt")) {
         this.nbt = IInput.readNBT(obj.get("nbt").getAsString());
      }
   }

   public RangeRecipeOutput(FriendlyByteBuf buffer) {
      this.output = buffer.readItem();
      this.minValue = buffer.readByte();
      this.maxValue = buffer.readByte();
   }

   public RangeRecipeOutput(IOutputGenerator output, int minValue, int maxValue) {
      output.addItems(T -> this.output = T);
      this.generators.add(output);
      this.minValue = minValue;
      this.maxValue = maxValue;
   }

   public RangeRecipeOutput(IOutputGenerator output, CompoundTag nbt, int minValue, int maxValue) {
      output.addItems(T -> this.output = T);
      this.generators.add(output);
      this.nbt = nbt;
      this.minValue = minValue;
      this.maxValue = maxValue;
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags) {
      ItemStack stack = this.output.copy();
      stack.setCount(this.minValue + rand.nextInt(this.maxValue - this.minValue + 1));
      return stack.getCount() <= 0 ? ObjectLists.emptyList() : ObjectLists.singleton(stack);
   }

   @Override
   public List<ItemStack> getAllOutputs() {
      List<ItemStack> result = new ObjectArrayList();

      for (int i = this.minValue; i <= this.maxValue; i++) {
         if (i != 0) {
            ItemStack stack = this.output.copy();
            stack.setCount(i);
            result.add(stack);
         }
      }

      return result;
   }

   @Override
   public CompoundTag getMetadata() {
      return this.nbt == null ? EMPTY_COMPOUND : this.nbt;
   }

   @Override
   public float getExperience() {
      return 0.0F;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeItem(this.output);
      buffer.writeByte(this.minValue);
      buffer.writeByte(this.maxValue);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.add("output", RecipeRegistry.INGREDIENTS.serializeOutputGenerator(this.generators.get(0)));
      obj.addProperty("minOut", this.minValue);
      obj.addProperty("maxOut", this.maxValue);
      if (this.nbt != null && this.nbt != EMPTY_COMPOUND) {
         obj.addProperty("nbt", this.nbt.toString());
      }

      return obj;
   }
}
