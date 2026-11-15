package ic2.core.block.machines.recipes.misc;

import com.google.gson.JsonObject;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.api.recipes.ingridients.recipes.BaseRecipeOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutputChance;
import ic2.api.recipes.ingridients.recipes.SimpleRecipeOutput;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.platform.registries.IC2Items;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class ScrapOutput extends BaseRecipeOutput implements IRecipeOutputChance {
   ItemStack output;
   int chance;
   CompoundTag nbt;

   public ScrapOutput(JsonObject obj) {
      IOutputGenerator generator = RecipeRegistry.INGREDIENTS.readOutputGenerator(obj.getAsJsonObject("output"));
      generator.addItems(T -> this.output = T);
      this.generators.add(generator);
      this.chance = obj.get("chance").getAsInt();
      this.nbt = obj.has("nbt") ? IInput.readNBT(obj.get("nbt").getAsString()) : null;
   }

   public ScrapOutput(FriendlyByteBuf buffer) {
      this.output = buffer.readItem();
      this.nbt = buffer.readNbt();
      this.chance = buffer.readInt();
   }

   public ScrapOutput(ItemStack drop, int chance) {
      this.output = drop.copy();
      this.chance = chance;
   }

   public static IMachineRecipeList.RecipeEntry createEntry(int time) {
      return new IMachineRecipeList.RecipeEntry(
         new ResourceLocation("ic2", "box"),
         new SimpleRecipeOutput(ObjectLists.singleton(new ItemGenerator(IC2Items.SCRAPBOX)), RecipeMods.RECIPE_TIME.create(time), 0.0F),
         new ItemInput(IC2Items.SCRAP, 9)
      );
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags) {
      return (List<ItemStack>)(rand.nextInt(this.chance) == 0 ? ObjectLists.singleton(this.output.copy()) : Collections.emptyList());
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags, IRecipeOutput.IRecipeOverride overrides) {
      return (List<ItemStack>)(rand.nextFloat() < overrides.getChance(1.0F / (float)this.chance)
         ? ObjectLists.singleton(this.output.copy())
         : Collections.emptyList());
   }

   @Override
   public List<ItemStack> getAllOutputs() {
      return ObjectLists.singleton(this.output.copy());
   }

   @Override
   public float getChance() {
      return 1.0F / (float)this.chance;
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
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.add("output", RecipeRegistry.INGREDIENTS.serializeOutputGenerator(this.generators.get(0)));
      obj.addProperty("chance", this.chance);
      if (this.nbt != null && this.nbt != EMPTY_COMPOUND) {
         obj.addProperty("nbt", this.nbt.toString());
      }

      return obj;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeItem(this.output);
      buffer.writeNbt(this.nbt);
      buffer.writeInt(this.chance);
   }
}
