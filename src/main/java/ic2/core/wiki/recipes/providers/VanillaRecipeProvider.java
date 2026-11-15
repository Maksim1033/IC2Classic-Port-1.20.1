package ic2.core.wiki.recipes.providers;

import ic2.core.IC2;
import ic2.core.platform.recipes.crafting.RecipeIC2Base;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.recipes.renderers.CraftingRecipeRenderer;
import ic2.core.wiki.recipes.renderers.IRecipeRenderer;
import ic2.core.wiki.recipes.renderers.SmeltingRecipeRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class VanillaRecipeProvider implements IRecipeProvider {
   Object2ObjectMap<Item, List<Recipe<?>>> outputs = CollectionUtils.<Item, List<Recipe<?>>>createLinkedMap();
   Object2ObjectMap<Item, List<Tuple<List<Ingredient>, Recipe<?>>>> inputs = CollectionUtils.<Item, List<Tuple<List<Ingredient>, Recipe<?>>>>createLinkedMap();
   RecipeType<?> type;

   public VanillaRecipeProvider(RecipeType<?> type) {
      this.type = type;
   }

   @Override
   public void reloadRecipes() {
      this.inputs.clear();
      this.outputs.clear();
      Set<Item> items = CollectionUtils.<Item>createLinkedSet();

      for (Recipe<?> recipe : IC2.PLATFORM.getRecipes().getRecipes()) {
         if (recipe.getType() == this.type) {
            this.addToList(recipe.getResultItem(IC2.PLATFORM.getRegistryAccess()).getItem(), recipe, this.outputs);
            items.clear();
            NonNullList<Ingredient> input = recipe.getIngredients();
            Tuple<List<Ingredient>, Recipe<?>> tuple = new Tuple(input, recipe);
            this.collectInputs(input, items);

            for (Item item : items) {
               this.addToList(item, tuple, this.inputs);
            }
         }
      }
   }

   protected void collectInputs(NonNullList<Ingredient> list, Set<Item> items) {
      for (Ingredient entry : list) {
         for (ItemStack stack : entry.getItems()) {
            items.add(stack.getItem());
         }
      }
   }

   private boolean isHidden() {
      return IC2.CONFIG.recipeHiding.get();
   }

   @Override
   public List<IRecipeRenderer> getFromInput(ItemStack stack, boolean ignoreHidden) {
      List<IRecipeRenderer> result = CollectionUtils.<IRecipeRenderer>createList();

      for (Tuple<List<Ingredient>, Recipe<?>> entry : this.inputs.getOrDefault(stack.getItem(), Collections.emptyList())) {
         if (!ignoreHidden || !this.isHidden() || !(entry.getB() instanceof RecipeIC2Base recipe) || !recipe.isHidden()) {
            for (Ingredient test : entry.getA()) {
               if (test.test(stack)) {
                  if (this.type == RecipeType.CRAFTING) {
                     result.add(new CraftingRecipeRenderer((Recipe<?>)entry.getB()));
                  } else {
                     result.add(new SmeltingRecipeRenderer((Recipe<?>)entry.getB()));
                  }
                  break;
               }
            }
         }
      }

      return result;
   }

   @Override
   public List<IRecipeRenderer> getFromOutput(ItemStack stack, boolean ignoreHidden) {
      List<IRecipeRenderer> result = CollectionUtils.<IRecipeRenderer>createList();

      for (Recipe<?> entry : this.outputs.getOrDefault(stack.getItem(), Collections.emptyList())) {
         if (!ignoreHidden || !this.isHidden() || !(entry instanceof RecipeIC2Base recipe) || !recipe.isHidden()) {
            if (this.type == RecipeType.CRAFTING) {
               result.add(new CraftingRecipeRenderer(entry));
            } else {
               result.add(new SmeltingRecipeRenderer(entry));
            }
         }
      }

      return result;
   }
}
