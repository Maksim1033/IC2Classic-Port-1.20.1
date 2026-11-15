package ic2.core.block.machines.recipes;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.IngredientInput;
import ic2.api.recipes.ingridients.recipes.SimpleRecipeOutput;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CookingMachineRecipeList extends MachineRecipeList {
   boolean loadedRecipes = false;
   RecipeType<?> type;
   int expectedCookTime;

   public CookingMachineRecipeList(RecipeType<?> type, int time) {
      super("Empty", null);
      this.listeners.clear();
      this.type = type;
      this.expectedCookTime = time;
   }

   @Override
   public void registerListener(Consumer<IMachineRecipeList> listener) {
   }

   @Override
   public void reload() {
      this.loadedRecipes = false;
      this.mapped = false;
      this.recipeMap.clear();
      this.recipes.clear();
   }

   public void fetchRecipes() {
      RecipeManager manager = IC2.PLATFORM.getRecipes();

      for (Recipe<?> entry : manager.getRecipes()) {
         if (entry.getType() == this.type) {
            AbstractCookingRecipe recipe = (AbstractCookingRecipe)entry;
            int extraTime = recipe.getCookingTime() - this.expectedCookTime;
            CompoundTag nbt = null;
            if (extraTime != 0) {
               nbt = RecipeMods.RECIPE_TIME.create((double)recipe.getCookingTime() / (double)this.expectedCookTime);
            }

            RegistryAccess access = ServerLifecycleHooks.getCurrentServer() != null ? ServerLifecycleHooks.getCurrentServer().registryAccess() : RegistryAccess.EMPTY;
            this.addRecipe(
               recipe.getId(),
               new SimpleRecipeOutput(Collections.singletonList(new ItemWithNBTGenerator(recipe.getResultItem(access))), nbt, recipe.getExperience()),
               new IInput[]{new IngredientInput((Ingredient)entry.getIngredients().get(0), 1)}
            );
         }
      }
   }

   @Override
   public List<ResourceLocation> getAllRecipes() {
      if (!this.loadedRecipes) {
         this.loadedRecipes = true;
         this.fetchRecipes();
      }

      return super.getAllRecipes();
   }

   @Override
   public List<IMachineRecipeList.RecipeEntry> getAllEntries() {
      if (!this.loadedRecipes) {
         this.loadedRecipes = true;
         this.fetchRecipes();
      }

      return super.getAllEntries();
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      return Collections.emptyMap();
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(ItemStack stack, boolean hasStackSize) {
      if (!this.loadedRecipes) {
         this.loadedRecipes = true;
         this.fetchRecipes();
      }

      for (IMachineRecipeList.RecipeEntry subEntry : this.recipes.values()) {
         if (subEntry.getInputs()[0].matches(stack)) {
            return subEntry;
         }
      }

      return null;
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(Predicate<IMachineRecipeList.RecipeEntry> checker) {
      if (!this.loadedRecipes) {
         this.loadedRecipes = true;
         this.fetchRecipes();
      }

      return super.getRecipe(checker);
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(ResourceLocation location) {
      if (!this.loadedRecipes) {
         this.loadedRecipes = true;
         this.fetchRecipes();
      }

      return super.getRecipe(location);
   }
}
