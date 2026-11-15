package ic2.core.platform.recipes.misc;

import com.google.gson.JsonObject;
import ic2.api.items.IRepairable;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.registries.IAdvancedCraftingManager;
import ic2.core.platform.recipes.crafting.RecipeIC2Base;
import ic2.core.platform.recipes.crafting.RepairRecipe;
import ic2.core.platform.recipes.crafting.ShapedIC2Recipe;
import ic2.core.platform.recipes.crafting.ShapelessIC2Recipe;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.SimpleRegistry;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.ItemLike;

public class AdvRecipeRegistry extends BaseRegistry<IAdvancedCraftingManager> implements IAdvancedCraftingManager {
   public static final AdvRecipeRegistry INSTANCE = new AdvRecipeRegistry();
   SimpleRegistry<RecipeIC2Base> recipes = CollectionUtils.createRegistry();
   SimpleRegistry<Supplier<AbstractCookingRecipe>> cookingRecipes = CollectionUtils.createRegistry();
   SimpleRegistry<Supplier<SingleItemRecipe>> singleRecipes = CollectionUtils.createRegistry();
   SimpleRegistry<Supplier<SmithingTransformRecipe>> smithingRecipes = CollectionUtils.createRegistry();

   public void init() {
      RecipeRegistry.CRAFTING = this;
   }

   @Override
   protected void reloadInternals() {
      this.recipes.clear();
      this.cookingRecipes.clear();
      this.singleRecipes.clear();
      this.smithingRecipes.clear();
   }

   @Override
   public void addShapedRecipe(ResourceLocation location, ItemStack output, Object... args) {
      this.recipes.register(location, ShapedIC2Recipe.create(location, output, args));
   }

   @Override
   public void addShapelessRecipe(ResourceLocation location, ItemStack output, Object... args) {
      this.recipes.register(location, ShapelessIC2Recipe.create(location, output, args));
   }

   @Override
   public void addRepairRecipe(ResourceLocation id, IRepairable repair, IInput repairMaterial, int effect, Object... args) {
      this.recipes.register(id, RepairRecipe.create(id, repair, repairMaterial, effect, args));
   }

   @Override
   public void addSmeltingRecipe(
      ResourceLocation location, ItemStack output, Object input, float xp, float extraTime, IAdvancedCraftingManager.SmeltingType... types
   ) {
      String group = output.getDescriptionId();

      for (IAdvancedCraftingManager.SmeltingType type : types) {
         ResourceLocation id = type.convert(location);
         switch (type) {
            case BLASTFURNACE:
               this.cookingRecipes
                  .register(id, () -> new BlastingRecipe(id, group, CookingBookCategory.MISC, this.createFrom(input), output.copy(), xp, (int)((float)type.getBaseTime() * extraTime)));
               break;
            case CAMPFIRE:
               this.cookingRecipes
                  .register(
                     id, () -> new CampfireCookingRecipe(id, group, CookingBookCategory.MISC, this.createFrom(input), output.copy(), xp, (int)((float)type.getBaseTime() * extraTime))
                  );
               break;
            case FURNACE:
               this.cookingRecipes
                  .register(id, () -> new SmeltingRecipe(id, group, CookingBookCategory.MISC, this.createFrom(input), output.copy(), xp, (int)((float)type.getBaseTime() * extraTime)));
               break;
            case SMOKER:
               this.cookingRecipes
                  .register(id, () -> new SmokingRecipe(id, group, CookingBookCategory.MISC, this.createFrom(input), output.copy(), xp, (int)((float)type.getBaseTime() * extraTime)));
         }
      }
   }

   @Override
   public void addStoneCutterRecipe(ResourceLocation location, ItemStack output, Object input) {
      this.singleRecipes.register(location, () -> new StonecutterRecipe(location, output.getDescriptionId(), this.createFrom(input), output.copy()));
   }

   @Override
   public void addSmithingRecipe(ResourceLocation location, Object main, Object secondary, ItemStack output) {
      this.smithingRecipes.register(location, () -> new SmithingTransformRecipe(location, Ingredient.EMPTY, this.createFrom(main), this.createFrom(secondary), output));
   }

   @Override
   public void removeCraftingRecipe(ResourceLocation id) {
      this.recipes.remove(id);
   }

   @Override
   public void removeSmeltingRecipe(ResourceLocation id, IAdvancedCraftingManager.SmeltingType... types) {
      for (IAdvancedCraftingManager.SmeltingType type : types) {
         this.cookingRecipes.remove(type.convert(id));
      }
   }

   @Override
   public void removeStoneCutterRecipe(ResourceLocation id) {
      this.singleRecipes.remove(id);
   }

   @Override
   public void removeSmithingRecipe(ResourceLocation id) {
      this.smithingRecipes.remove(id);
   }

   private Ingredient createFrom(Object obj) {
      if (obj instanceof ItemStack) {
         return Ingredient.of(new ItemStack[]{((ItemStack)obj).copy()});
      } else if (obj instanceof ItemLike) {
         return Ingredient.of(new ItemLike[]{(ItemLike)obj});
      } else if (obj instanceof TagKey) {
         JsonObject json = new JsonObject();
         json.addProperty("tag", ((TagKey)obj).location().toString());
         return Ingredient.fromJson(json);
      } else if (obj instanceof Ingredient) {
         return (Ingredient)obj;
      } else {
         throw new IllegalArgumentException("Input has to be either a [Stack/IItemProvider/ItemTag/Ingredient]");
      }
   }

   public SimpleRegistry<RecipeIC2Base> getRecipes() {
      return this.recipes;
   }

   public SimpleRegistry<Supplier<AbstractCookingRecipe>> getCooking() {
      return this.cookingRecipes;
   }

   public SimpleRegistry<Supplier<SingleItemRecipe>> getSingleRecipes() {
      return this.singleRecipes;
   }

   public SimpleRegistry<Supplier<SmithingTransformRecipe>> getSmithingRecipes() {
      return this.smithingRecipes;
   }

   public boolean contains(ResourceLocation location) {
      return this.recipes.contains(location)
         || this.cookingRecipes.contains(location)
         || this.singleRecipes.contains(location)
         || this.smithingRecipes.contains(location);
   }
}
