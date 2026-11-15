package ic2.api.recipes.registries;

import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.INullableInput;
import ic2.api.recipes.ingridients.recipes.ChanceRecipeOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.ingridients.recipes.RangeRecipeOutput;
import ic2.api.recipes.ingridients.recipes.SimpleRecipeOutput;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IMachineRecipeList extends IListenableRegistry<IMachineRecipeList> {
   void addRecipe(IMachineRecipeList.RecipeEntry var1);

   default void addRecipe(ResourceLocation id, IRecipeOutput output, IInput... inputs) {
      this.addRecipe(new IMachineRecipeList.RecipeEntry(id, output, inputs));
   }

   default void addSimpleRecipe(ResourceLocation id, ItemStack output, Object... inputs) {
      this.addRecipe(id, new SimpleRecipeOutput(ObjectLists.singleton(new ItemWithNBTGenerator(output, output.getCount()))), inputs);
   }

   default void addSimpleRecipe(ResourceLocation id, ItemStack output, CompoundTag data, Object... inputs) {
      this.addRecipe(id, new SimpleRecipeOutput(ObjectLists.singleton(new ItemWithNBTGenerator(output, output.getCount())), data), inputs);
   }

   default void addXPRecipe(ResourceLocation id, ItemStack output, float xp, Object... inputs) {
      this.addRecipe(id, new SimpleRecipeOutput(ObjectLists.singleton(new ItemWithNBTGenerator(output, output.getCount())), xp), inputs);
   }

   default void addXPRecipe(ResourceLocation id, ItemStack output, float xp, CompoundTag data, Object... inputs) {
      this.addRecipe(id, new SimpleRecipeOutput(ObjectLists.singleton(new ItemWithNBTGenerator(output, output.getCount())), data, xp), inputs);
   }

   default void addChanceRecipe(ResourceLocation id, ItemStack output, float xp, float chance, Object... inputs) {
      this.addRecipe(id, new ChanceRecipeOutput(ObjectLists.singleton(new ItemWithNBTGenerator(output, output.getCount())), xp, chance), inputs);
   }

   default void addChanceRecipe(ResourceLocation id, ItemStack output, float xp, float chance, CompoundTag data, Object... inputs) {
      this.addRecipe(id, new ChanceRecipeOutput(ObjectLists.singleton(new ItemWithNBTGenerator(output, output.getCount())), xp, data, chance), inputs);
   }

   default void addRangeRecipe(ResourceLocation id, ItemStack output, int minValue, int maxValue, Object... inputs) {
      this.addRecipe(id, new RangeRecipeOutput(new ItemWithNBTGenerator(output, output.getCount()), minValue, maxValue), inputs);
   }

   default void addRangeRecipe(ResourceLocation id, ItemStack output, int minValue, int maxValue, CompoundTag data, Object... inputs) {
      this.addRecipe(id, new RangeRecipeOutput(new ItemWithNBTGenerator(output, output.getCount()), data, minValue, maxValue), inputs);
   }

   default void addRecipe(ResourceLocation id, IRecipeOutput output, Object... inputs) {
      this.addRecipe(id, output, convertInputs(inputs));
   }

   default void addIC2SimpleRecipe(String id, ItemStack output, Object... inputs) {
      this.addSimpleRecipe(new ResourceLocation("ic2", id), output, inputs);
   }

   default void addIC2SimpleRecipe(String id, ItemStack output, CompoundTag data, Object... inputs) {
      this.addSimpleRecipe(new ResourceLocation("ic2", id), output, data, inputs);
   }

   default void addIC2XPRecipe(String id, ItemStack output, float xp, Object... inputs) {
      this.addXPRecipe(new ResourceLocation("ic2", id), output, xp, inputs);
   }

   default void addIC2XPRecipe(String id, ItemStack output, float xp, CompoundTag data, Object... inputs) {
      this.addXPRecipe(new ResourceLocation("ic2", id), output, xp, data, inputs);
   }

   default void addIC2ChanceRecipe(String id, ItemStack output, float xp, float chance, Object... inputs) {
      this.addChanceRecipe(new ResourceLocation("ic2", id), output, xp, chance, inputs);
   }

   default void addIC2ChanceRecipe(String id, ItemStack output, float xp, float chance, CompoundTag data, Object... inputs) {
      this.addChanceRecipe(new ResourceLocation("ic2", id), output, xp, chance, data, inputs);
   }

   default void addIC2RangeRecipe(String id, ItemStack output, int minValue, int maxValue, Object... inputs) {
      this.addRangeRecipe(new ResourceLocation("ic2", id), output, minValue, maxValue, inputs);
   }

   default void addIC2RangeRecipe(String id, ItemStack output, int minValue, int maxValue, CompoundTag data, Object... inputs) {
      this.addRangeRecipe(new ResourceLocation("ic2", id), output, minValue, maxValue, data, inputs);
   }

   default void addIC2Recipe(String id, IRecipeOutput output, Object... inputs) {
      this.addRecipe(new ResourceLocation("ic2", id), output, inputs);
   }

   default boolean hasRecipeForItem(ItemStack stack) {
      return this.getRecipe(stack, false) != null;
   }

   IMachineRecipeList.RecipeEntry getRecipe(ResourceLocation var1);

   IMachineRecipeList.RecipeEntry getRecipe(ItemStack var1, boolean var2);

   IMachineRecipeList.RecipeEntry getRecipe(Predicate<IMachineRecipeList.RecipeEntry> var1);

   IMachineRecipeList.RecipeEntry removeRecipe(ResourceLocation var1);

   List<ResourceLocation> getAllRecipes();

   List<IMachineRecipeList.RecipeEntry> getAllEntries();

   static IInput[] convertInputs(Object... inputs) {
      List<IInput> result = new ObjectArrayList();

      for (Object o : inputs) {
         IInput input = RecipeRegistry.INGREDIENTS.createInputFrom(o);
         if (input != null) {
            result.add(input);
         }
      }

      return result.toArray(IInput[]::new);
   }

   public static class RecipeEntry {
      final ResourceLocation location;
      final IInput[] inputs;
      final IRecipeOutput output;
      final boolean nullInput;

      public RecipeEntry(ResourceLocation location, IRecipeOutput output, IInput... inputs) {
         this.location = location;
         this.inputs = inputs;
         this.output = output;

         for (IInput input : inputs) {
            if (input instanceof INullableInput) {
               this.nullInput = true;
               return;
            }
         }

         this.nullInput = false;
      }

      public ResourceLocation getLocation() {
         return this.location;
      }

      public IInput[] getInputs() {
         return this.inputs;
      }

      public IRecipeOutput getOutput() {
         return this.output;
      }

      public boolean hasNullInput() {
         return this.nullInput;
      }
   }
}
