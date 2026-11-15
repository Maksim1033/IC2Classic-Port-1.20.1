package ic2.api.recipes.ingridients.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class ChanceRecipeOutput extends BaseRecipeOutput implements IRecipeOutputChance {
   List<ItemStack> outputs = new ObjectArrayList();
   float chance;
   CompoundTag nbt;
   float xp;

   public ChanceRecipeOutput(JsonObject obj) {
      this.chance = obj.get("chance").getAsFloat();
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

   public ChanceRecipeOutput(FriendlyByteBuf buffer) {
      int expected = buffer.readByte();

      for (int i = 0; i < expected; i++) {
         this.outputs.add(buffer.readItem());
      }

      this.chance = buffer.readFloat();
      this.nbt = buffer.readNbt();
      this.xp = buffer.readFloat();
   }

   public ChanceRecipeOutput(List<IOutputGenerator> outputs, float xp, CompoundTag nbt, float chance) {
      this.handleGenerators(outputs, this.outputs);
      this.chance = chance;
      this.nbt = nbt;
      this.xp = xp;
   }

   public ChanceRecipeOutput(List<IOutputGenerator> outputs, float xp, float chance) {
      this.handleGenerators(outputs, this.outputs);
      this.chance = chance;
      this.xp = xp;
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags) {
      return rand.nextFloat() < this.chance ? IRecipeOutput.copyItems(this.outputs) : Collections.emptyList();
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags, IRecipeOutput.IRecipeOverride overrides) {
      return rand.nextFloat() < overrides.getChance(this.chance) ? IRecipeOutput.copyItems(this.outputs) : Collections.emptyList();
   }

   @Override
   public float getChance() {
      return this.chance;
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

      buffer.writeFloat(this.chance);
      buffer.writeNbt(this.nbt);
      buffer.writeFloat(this.xp);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("chance", this.chance);
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
