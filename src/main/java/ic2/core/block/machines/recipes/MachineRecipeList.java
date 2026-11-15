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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class MachineRecipeList implements IRecipeList, IMachineRecipeList {
   Map<ResourceLocation, IMachineRecipeList.RecipeEntry> recipes = CollectionUtils.<ResourceLocation, IMachineRecipeList.RecipeEntry>createLinkedMap();
   Map<ItemStack, IMachineRecipeList.RecipeEntry> recipeMap = CollectionUtils.<ItemStack, IMachineRecipeList.RecipeEntry>createLinkedMap(
      ItemStackStrategy.INSTANCE
   );
   List<Consumer<IMachineRecipeList>> listeners = ObjectLists.synchronize(CollectionUtils.createList());
   boolean mapped = false;
   String folder;

   public MachineRecipeList(String folder, Consumer<IMachineRecipeList> reloader) {
      this.folder = folder;
      if (reloader != null) {
         this.listeners.add(reloader);
      }
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.recipes.size());

      for (IMachineRecipeList.RecipeEntry entry : this.recipes.values()) {
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

      this.mapped = false;
      this.recipes = recipes;
      this.recipeMap = CollectionUtils.<ItemStack, IMachineRecipeList.RecipeEntry>createLinkedMap(ItemStackStrategy.INSTANCE);
   }

   @Override
   public String getFolder() {
      return this.folder;
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> result = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();

      for (IMachineRecipeList.RecipeEntry entry : this.recipes.values()) {
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
   public void readRecipe(ResourceLocation id, JsonObject data) throws Exception {
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
   public void registerListener(Consumer<IMachineRecipeList> listener) {
      this.listeners.add(listener);
   }

   public void reload() {
      this.recipes.clear();
      this.recipeMap.clear();
      this.mapped = false;

      for (Consumer<IMachineRecipeList> listener : this.listeners) {
         listener.accept(this);
      }
   }

   @Override
   public void addRecipe(IMachineRecipeList.RecipeEntry entry) {
      IInput[] inputs = entry.getInputs();
      if (inputs != null && inputs.length == 1 && entry.getOutput() != null) {
         this.recipes.put(entry.getLocation(), entry);
      } else {
         throw new RuntimeException("Recipe Is Invalid: " + entry.getLocation());
      }
   }

   @Override
   public List<ResourceLocation> getAllRecipes() {
      return new ObjectArrayList(this.recipes.keySet());
   }

   @Override
   public List<IMachineRecipeList.RecipeEntry> getAllEntries() {
      return new ObjectArrayList(this.recipes.values());
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(ResourceLocation location) {
      return this.recipes.get(location);
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(ItemStack stack, boolean hasStackSize) {
      if (!this.mapped) {
         this.mapRecipes();
         this.mapped = true;
      }

      IMachineRecipeList.RecipeEntry entry = this.recipeMap.get(stack);
      return entry == null || !entry.getInputs()[0].matches(stack) || hasStackSize && entry.getInputs()[0].getInputSize() > stack.getCount() ? null : entry;
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(Predicate<IMachineRecipeList.RecipeEntry> checker) {
      for (IMachineRecipeList.RecipeEntry entry : this.recipes.values()) {
         if (checker.test(entry)) {
            return entry;
         }
      }

      return null;
   }

   @Override
   public IMachineRecipeList.RecipeEntry removeRecipe(ResourceLocation location) {
      return this.recipes.remove(location);
   }

   protected void mapRecipes() {
      for (IMachineRecipeList.RecipeEntry entry : this.recipes.values()) {
         for (ItemStack stack : this.toStackList(entry.getInputs())) {
            this.recipeMap.put(stack, entry);
         }
      }
   }

   protected List<ItemStack> toStackList(IInput... inputs) {
      List<ItemStack> list = new ArrayList<>();

      for (IInput input : inputs) {
         list.addAll(input.getComponents());
      }

      return list;
   }
}
