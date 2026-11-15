package ic2.core.block.machines.recipes;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.recipes.IFluidRecipeOutput;
import ic2.api.recipes.registries.IRefiningRecipeList;
import ic2.core.IC2;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class RefiningRecipeList implements IRefiningRecipeList, IRecipeList {
   List<Consumer<IRefiningRecipeList>> listeners = ObjectLists.synchronize(CollectionUtils.createList());
   Object2ObjectMap<ResourceLocation, IRefiningRecipeList.FluidRecipe> allRecipes = CollectionUtils.<ResourceLocation, IRefiningRecipeList.FluidRecipe>createLinkedMap();
   Object2ObjectMap<Item, Map<Fluid, Map<Fluid, IRefiningRecipeList.FluidRecipe>>> mappedRecipe;
   Set<Fluid> validFirstFluid;
   Set<Fluid> validSecondFluid;
   boolean mapped = false;

   public RefiningRecipeList(Consumer<IRefiningRecipeList> listener) {
      this.registerListener(listener);
   }

   @Override
   public void registerListener(Consumer<IRefiningRecipeList> listener) {
      if (listener != null) {
         this.listeners.add(listener);
      }
   }

   @Override
   public void addRecipe(ResourceLocation location, FluidStack input, FluidStack secondInput, IInput catalyst, IFluidRecipeOutput output) {
      if (location != null && input != null && secondInput != null && catalyst != null && output != null) {
         this.allRecipes.put(location, new IRefiningRecipeList.FluidRecipe(location, input, secondInput, catalyst, output));
         this.allRecipes
            .put(
               new ResourceLocation(location.getNamespace(), location.getPath() + "_inverted"),
               new IRefiningRecipeList.FluidRecipe(location, secondInput, input, catalyst, output)
            );
         this.mapped = false;
         this.mappedRecipe = null;
      } else {
         IC2.LOGGER.info("Recipe [{}] is invalid", location);
      }
   }

   public void reload() {
      this.allRecipes.clear();
      this.mappedRecipe = null;
      this.validFirstFluid = null;
      this.validSecondFluid = null;
      this.mapped = false;

      for (Consumer<IRefiningRecipeList> entry : this.listeners) {
         entry.accept(this);
      }
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.allRecipes.size());
      ObjectIterator var2 = this.allRecipes.values().iterator();

      while (var2.hasNext()) {
         IRefiningRecipeList.FluidRecipe entry = (IRefiningRecipeList.FluidRecipe)var2.next();
         buffer.writeResourceLocation(entry.getLocation());
         IngredientRegistry.INSTANCE.writeInput(entry.getItemInput(), buffer);
         buffer.writeFluidStack(entry.getFirstTank());
         buffer.writeFluidStack(entry.getSecondTank());
         IngredientRegistry.INSTANCE.writeFluidOutput(entry.getOutput(), buffer);
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Object2ObjectMap<ResourceLocation, IRefiningRecipeList.FluidRecipe> newAllRecipes = CollectionUtils.<ResourceLocation, IRefiningRecipeList.FluidRecipe>createLinkedMap();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         ResourceLocation location = buffer.readResourceLocation();
         IInput input = IngredientRegistry.INSTANCE.readInput(buffer);
         FluidStack firstTank = buffer.readFluidStack();
         FluidStack secondTank = buffer.readFluidStack();
         IFluidRecipeOutput output = IngredientRegistry.INSTANCE.createFluidOutput(buffer);
         newAllRecipes.put(location, new IRefiningRecipeList.FluidRecipe(location, firstTank, secondTank, input, output));
      }

      this.allRecipes = newAllRecipes;
      this.mappedRecipe = null;
      this.validFirstFluid = null;
      this.validSecondFluid = null;
      this.mapped = false;
   }

   @Override
   public String getFolder() {
      return "refinery";
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> map = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();
      ObjectIterator var2 = this.allRecipes.values().iterator();

      while (var2.hasNext()) {
         IRefiningRecipeList.FluidRecipe entry = (IRefiningRecipeList.FluidRecipe)var2.next();
         JsonObject obj = new JsonObject();
         obj.add("input", IngredientRegistry.INSTANCE.serializeInput(entry.getItemInput()));
         obj.add("first_tank", IInput.writeFluidStack(entry.getFirstTank()));
         obj.add("second_tank", IInput.writeFluidStack(entry.getSecondTank()));
         obj.add("output", IngredientRegistry.INSTANCE.serializeFluidOutput(entry.getOutput()));
         map.put(entry.getLocation(), obj);
      }

      return map;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      if (IRecipeList.isRemover(data)) {
         IRecipeList.getEntriesToRemove(data, this::removeRecipe);
      } else {
         IInput input = IngredientRegistry.INSTANCE.readInput(GsonHelper.getAsJsonObject(data, "input"));
         if (input == null) {
            throw new RuntimeException("Input is null, while Empty is allowed, Null isn't");
         } else {
            FluidStack first = IInput.readFluidStack(GsonHelper.getAsJsonObject(data, "first_tank"));
            FluidStack second = IInput.readFluidStack(GsonHelper.getAsJsonObject(data, "second_tank"));
            IFluidRecipeOutput output = IngredientRegistry.INSTANCE.readFluidOutput(GsonHelper.getAsJsonObject(data, "output"));
            if (output == null) {
               throw new RuntimeException("Output is null");
            } else {
               this.addRecipe(id, first, second, input, output);
            }
         }
      }
   }

   protected void mapRecipes() {
      if (!this.mapped) {
         this.mapped = true;
         this.mappedRecipe = CollectionUtils.createMap();
         this.validFirstFluid = CollectionUtils.<Fluid>createSet();
         this.validSecondFluid = CollectionUtils.<Fluid>createSet();
         ObjectIterator var1 = this.allRecipes.values().iterator();

         while (var1.hasNext()) {
            IRefiningRecipeList.FluidRecipe recipe = (IRefiningRecipeList.FluidRecipe)var1.next();
            this.validFirstFluid.add(recipe.getFirstTank().getFluid());
            this.validSecondFluid.add(recipe.getSecondTank().getFluid());

            for (ItemStack stack : recipe.getItemInput().getComponents()) {
               Map<Fluid, Map<Fluid, IRefiningRecipeList.FluidRecipe>> subMap = (Map<Fluid, Map<Fluid, IRefiningRecipeList.FluidRecipe>>)this.mappedRecipe
                  .get(stack.getItem());
               if (subMap == null) {
                  subMap = CollectionUtils.<Fluid, Map<Fluid, IRefiningRecipeList.FluidRecipe>>createMap();
                  this.mappedRecipe.put(stack.getItem(), subMap);
               }

               this.map(subMap, recipe.getFirstTank(), recipe.getSecondTank(), recipe);
            }
         }

         this.validFirstFluid.remove(Fluids.EMPTY);
         this.validSecondFluid.remove(Fluids.EMPTY);
      }
   }

   private void map(Map<Fluid, Map<Fluid, IRefiningRecipeList.FluidRecipe>> subMap, FluidStack first, FluidStack last, IRefiningRecipeList.FluidRecipe recipe) {
      Map<Fluid, IRefiningRecipeList.FluidRecipe> old = subMap.computeIfAbsent(first.getFluid(), k -> CollectionUtils.createMap());
      old.putIfAbsent(last.getFluid(), recipe);
   }

   @Override
   public IRefiningRecipeList.FluidRecipe getRecipe(ResourceLocation location) {
      return (IRefiningRecipeList.FluidRecipe)this.allRecipes.get(location);
   }

   @Override
   public IRefiningRecipeList.FluidRecipe getRecipe(ItemStack item, FluidStack firstTank, FluidStack secondTank, boolean testAmounts) {
      this.mapRecipes();
      Map<Fluid, Map<Fluid, IRefiningRecipeList.FluidRecipe>> subMap = (Map<Fluid, Map<Fluid, IRefiningRecipeList.FluidRecipe>>)this.mappedRecipe
         .get(item.getItem());
      if (subMap != null && !subMap.isEmpty()) {
         Map<Fluid, IRefiningRecipeList.FluidRecipe> map = subMap.get(firstTank.getFluid());
         if (map != null && !map.isEmpty()) {
            IRefiningRecipeList.FluidRecipe recipe = map.get(secondTank.getFluid());
            if (recipe == null) {
               return null;
            } else if (testAmounts) {
               return recipe.getItemInput().matches(item)
                     && recipe.getItemInput().getInputSize() <= item.getCount()
                     && firstTank.containsFluid(recipe.getFirstTank())
                     && secondTank.containsFluid(recipe.getSecondTank())
                  ? recipe
                  : null;
            } else {
               return recipe.getItemInput().matches(item) && firstTank.isFluidEqual(recipe.getFirstTank()) && secondTank.isFluidEqual(recipe.getSecondTank())
                  ? recipe
                  : null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public IRefiningRecipeList.FluidRecipe getRecipe(Predicate<IRefiningRecipeList.FluidRecipe> checker) {
      ObjectIterator var2 = this.allRecipes.values().iterator();

      while (var2.hasNext()) {
         IRefiningRecipeList.FluidRecipe recipe = (IRefiningRecipeList.FluidRecipe)var2.next();
         if (checker.test(recipe)) {
            return recipe;
         }
      }

      return null;
   }

   private void removeRecipe(ResourceLocation location) {
      this.removeRecipe(location, true);
   }

   @Override
   public IRefiningRecipeList.FluidRecipe removeRecipe(ResourceLocation location, boolean includeInverted) {
      IRefiningRecipeList.FluidRecipe recipe = (IRefiningRecipeList.FluidRecipe)this.allRecipes.remove(location);
      if (recipe != null) {
         this.mapped = false;
         this.mappedRecipe = null;
         this.validFirstFluid = null;
         this.validSecondFluid = null;
         this.allRecipes.remove(new ResourceLocation(location.getNamespace(), location.getPath() + "_inverted"));
      }

      return recipe;
   }

   @Override
   public boolean isValidCatalyst(ItemStack stack) {
      this.mapRecipes();
      return this.mappedRecipe.containsKey(stack.getItem());
   }

   @Override
   public boolean isValidFirstFluid(FluidStack fluid) {
      this.mapRecipes();
      return this.validFirstFluid.contains(fluid.getFluid());
   }

   @Override
   public boolean isValidSecondFluid(FluidStack fluid) {
      this.mapRecipes();
      return this.validSecondFluid.contains(fluid.getFluid());
   }

   @Override
   public List<IRefiningRecipeList.FluidRecipe> getAllRecipes() {
      return new ObjectArrayList(this.allRecipes.values());
   }
}
