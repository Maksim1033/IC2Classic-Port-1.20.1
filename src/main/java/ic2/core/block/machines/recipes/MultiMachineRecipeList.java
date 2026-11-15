package ic2.core.block.machines.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;

public class MultiMachineRecipeList implements IMachineRecipeList, IRecipeList {
   List<Consumer<IMachineRecipeList>> listeners = ObjectLists.synchronize(CollectionUtils.createList());
   Map<ResourceLocation, IMachineRecipeList.RecipeEntry> recipeMap = CollectionUtils.<ResourceLocation, IMachineRecipeList.RecipeEntry>createLinkedMap();
   int maxInputs;
   boolean exact;
   String folder;

   public MultiMachineRecipeList(String folder, int maxInputs, boolean exact, Consumer<IMachineRecipeList> listener) {
      this.folder = folder;
      this.maxInputs = maxInputs;
      this.exact = exact;
      if (listener != null) {
         this.listeners.add(listener);
      }
   }

   @Override
   public void registerListener(Consumer<IMachineRecipeList> listener) {
      this.listeners.add(listener);
   }

   public void reload() {
      this.recipeMap.clear();

      for (Consumer<IMachineRecipeList> listener : this.listeners) {
         listener.accept(this);
      }
   }

   @Override
   public void addRecipe(IMachineRecipeList.RecipeEntry recipe) {
      IInput[] inputs = recipe.getInputs();
      if (inputs != null && (this.exact ? inputs.length == this.maxInputs : inputs.length <= this.maxInputs) && recipe.getOutput() != null) {
         this.recipeMap.put(recipe.getLocation(), recipe);
      } else {
         throw new RuntimeException("Recipe Is Invalid: " + recipe.getLocation());
      }
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.recipeMap.size());

      for (IMachineRecipeList.RecipeEntry entry : this.recipeMap.values()) {
         buffer.writeResourceLocation(entry.getLocation());
         IngredientRegistry.INSTANCE.writeRecipeOutput(entry.getOutput(), buffer);
         buffer.writeByte(entry.getInputs().length);

         for (IInput input : entry.getInputs()) {
            IngredientRegistry.INSTANCE.writeInput(input, buffer);
         }
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Map<ResourceLocation, IMachineRecipeList.RecipeEntry> recipes = CollectionUtils.<ResourceLocation, IMachineRecipeList.RecipeEntry>createLinkedMap();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         ResourceLocation location = buffer.readResourceLocation();
         IRecipeOutput output = IngredientRegistry.INSTANCE.createOutput(buffer);
         IInput[] inputs = new IInput[buffer.readByte()];

         for (int j = 0; j < inputs.length; j++) {
            inputs[j] = IngredientRegistry.INSTANCE.readInput(buffer);
         }

         recipes.put(location, new IMachineRecipeList.RecipeEntry(location, output, inputs));
      }

      this.recipeMap = recipes;
   }

   @Override
   public String getFolder() {
      return this.folder;
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> result = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();

      for (IMachineRecipeList.RecipeEntry entry : this.recipeMap.values()) {
         JsonObject obj = new JsonObject();
         if (entry.getInputs().length <= 1) {
            obj.add("input", IngredientRegistry.INSTANCE.serializeInput(entry.getInputs()[0]));
         } else {
            JsonArray inputs = new JsonArray();

            for (IInput input : entry.getInputs()) {
               inputs.add(IngredientRegistry.INSTANCE.serializeInput(input));
            }

            obj.add("input", inputs);
         }

         obj.add("output", IngredientRegistry.INSTANCE.serializeOutput(entry.getOutput()));
         result.put(entry.getLocation(), obj);
      }

      return result;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      if (IRecipeList.isRemover(data)) {
         IRecipeList.getEntriesToRemove(data, this::removeRecipe);
      } else {
         List<IInput> inputs = CollectionUtils.<IInput>createList();
         IRecipeList.iterateObject(data.get("input"), T -> inputs.add(IngredientRegistry.INSTANCE.readInput(T)));
         if (inputs.isEmpty()) {
            throw new RuntimeException("Inputs are empty");
         } else {
            IRecipeOutput out = IngredientRegistry.INSTANCE.readOutput(GsonHelper.getAsJsonObject(data, "output"));
            if (out == null) {
               throw new RuntimeException("Output is Empty");
            } else {
               this.addRecipe(new IMachineRecipeList.RecipeEntry(id, out, inputs.toArray(IInput[]::new)));
            }
         }
      }
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(ResourceLocation location) {
      return this.recipeMap.get(location);
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(Predicate<IMachineRecipeList.RecipeEntry> checker) {
      for (IMachineRecipeList.RecipeEntry entry : this.recipeMap.values()) {
         if (checker.test(entry)) {
            return entry;
         }
      }

      return null;
   }

   @Override
   public IMachineRecipeList.RecipeEntry removeRecipe(ResourceLocation location) {
      return this.recipeMap.remove(location);
   }

   @Override
   public List<ResourceLocation> getAllRecipes() {
      return new ObjectArrayList(this.recipeMap.keySet());
   }

   @Override
   public List<IMachineRecipeList.RecipeEntry> getAllEntries() {
      return new ObjectArrayList(this.recipeMap.values());
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(ItemStack stack, boolean hasStackSize) {
      throw new NotImplementedException("Function not supported in MultiRecipeLists");
   }
}
