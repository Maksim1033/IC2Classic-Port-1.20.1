package ic2.api.recipes.ingridients.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class SimpleRecipeOutput extends BaseRecipeOutput {
   protected List<ItemStack> outputs = new ObjectArrayList();
   protected CompoundTag nbt;
   protected float xp;

   public SimpleRecipeOutput(JsonObject obj) {
      this.xp = obj.get("xp").getAsFloat();
      if (obj.has("nbt")) {
         this.nbt = IInput.readNBT(obj.get("nbt").getAsString());
      }

      for (JsonElement el : obj.getAsJsonArray("outputs")) {
         IOutputGenerator generator = RecipeRegistry.INGREDIENTS.readOutputGenerator(el.getAsJsonObject());
         if (generator != null) {
            this.generators.add(generator);
            generator.addItems(this.outputs::add);
         }
      }
   }

   public SimpleRecipeOutput(FriendlyByteBuf buffer) {
      int expected = buffer.readByte();

      for (int i = 0; i < expected; i++) {
         this.outputs.add(buffer.readItem());
      }

      this.nbt = buffer.readNbt();
      this.xp = buffer.readFloat();
   }

   public SimpleRecipeOutput(List<IOutputGenerator> outputs) {
      this.handleGenerators(outputs, this.outputs);
   }

   public SimpleRecipeOutput(List<IOutputGenerator> outputs, CompoundTag nbt) {
      this.handleGenerators(outputs, this.outputs);
      this.nbt = nbt;
   }

   public SimpleRecipeOutput(List<IOutputGenerator> outputs, float xp) {
      this.handleGenerators(outputs, this.outputs);
      this.xp = xp;
   }

   public SimpleRecipeOutput(List<IOutputGenerator> outputs, CompoundTag nbt, float xp) {
      this.handleGenerators(outputs, this.outputs);
      this.nbt = nbt;
      this.xp = xp;
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags) {
      return IRecipeOutput.copyItems(this.outputs);
   }

   @Override
   public List<ItemStack> getAllOutputs() {
      return IRecipeOutput.copyItems(this.outputs);
   }

   @Override
   public CompoundTag getMetadata() {
      return this.nbt == null ? EMPTY_COMPOUND : this.nbt;
   }

   @Override
   public float getExperience() {
      return this.xp;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeByte(this.outputs.size());

      for (ItemStack out : this.outputs) {
         buffer.writeItem(out);
      }

      buffer.writeNbt(this.nbt);
      buffer.writeFloat(this.xp);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("xp", this.xp);
      if (this.nbt != null && this.nbt != EMPTY_COMPOUND) {
         obj.addProperty("nbt", this.nbt.toString());
      }

      JsonArray array = new JsonArray();

      for (IOutputGenerator stack : this.generators) {
         array.add(RecipeRegistry.INGREDIENTS.serializeOutputGenerator(stack));
      }

      obj.add("outputs", array);
      return obj;
   }
}
