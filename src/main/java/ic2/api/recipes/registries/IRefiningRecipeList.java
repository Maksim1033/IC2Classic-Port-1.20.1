package ic2.api.recipes.registries;

import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.inputs.EmptyInput;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.api.recipes.ingridients.inputs.ItemStackInput;
import ic2.api.recipes.ingridients.inputs.ItemTagInput;
import ic2.api.recipes.ingridients.recipes.IFluidRecipeOutput;
import ic2.api.recipes.ingridients.recipes.RangeFluidOutput;
import ic2.api.recipes.ingridients.recipes.SimpleFluidOutput;
import ic2.api.recipes.misc.RecipeFlags;
import ic2.api.recipes.misc.RecipeMods;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

public interface IRefiningRecipeList extends IListenableRegistry<IRefiningRecipeList> {
   default IRefiningRecipeList.Builder addIC2Recipe(String id) {
      return new IRefiningRecipeList.Builder(this, new ResourceLocation("ic2", id));
   }

   default IRefiningRecipeList.Builder addRecipe(ResourceLocation id) {
      return new IRefiningRecipeList.Builder(this, id);
   }

   void addRecipe(ResourceLocation var1, FluidStack var2, FluidStack var3, IInput var4, IFluidRecipeOutput var5);

   IRefiningRecipeList.FluidRecipe getRecipe(ResourceLocation var1);

   IRefiningRecipeList.FluidRecipe getRecipe(ItemStack var1, FluidStack var2, FluidStack var3, boolean var4);

   IRefiningRecipeList.FluidRecipe getRecipe(Predicate<IRefiningRecipeList.FluidRecipe> var1);

   IRefiningRecipeList.FluidRecipe removeRecipe(ResourceLocation var1, boolean var2);

   List<IRefiningRecipeList.FluidRecipe> getAllRecipes();

   boolean isValidCatalyst(ItemStack var1);

   boolean isValidFirstFluid(FluidStack var1);

   boolean isValidSecondFluid(FluidStack var1);

   public static class Builder {
      IRefiningRecipeList registry;
      ResourceLocation id;
      FluidStack firstInput = FluidStack.EMPTY;
      FluidStack secondInput = FluidStack.EMPTY;
      IInput catalyst = EmptyInput.INSTANCE;
      CompoundTag data;

      public Builder(IRefiningRecipeList registry, ResourceLocation id) {
         this.registry = registry;
         this.id = id;
      }

      public IRefiningRecipeList.Builder catalyst(ItemLike provider) {
         this.catalyst = new ItemInput(provider);
         return this;
      }

      public IRefiningRecipeList.Builder catalyst(ItemStack stack) {
         this.catalyst = new ItemStackInput(stack);
         return this;
      }

      public IRefiningRecipeList.Builder catalyst(TagKey<Item> tag) {
         this.catalyst = new ItemTagInput(tag);
         return this;
      }

      public IRefiningRecipeList.Builder catalyst(RegistryObject<Item> provider) {
         return this.catalyst((ItemLike)provider.get());
      }

      public IRefiningRecipeList.Builder catalyst(IInput input) {
         this.catalyst = input;
         return this;
      }

      public IRefiningRecipeList.Builder mainInput(RegistryObject<Fluid> fluid, int amount) {
         return this.mainInput((Fluid)fluid.get(), amount);
      }

      public IRefiningRecipeList.Builder mainInput(Fluid fluid, int amount) {
         this.firstInput = new FluidStack(fluid, amount);
         return this;
      }

      public IRefiningRecipeList.Builder mainInput(FluidStack fluid) {
         this.firstInput = fluid;
         return this;
      }

      public IRefiningRecipeList.Builder secondInput(RegistryObject<Fluid> fluid, int amount) {
         return this.secondInput((Fluid)fluid.get(), amount);
      }

      public IRefiningRecipeList.Builder secondInput(Fluid fluid, int amount) {
         this.secondInput = new FluidStack(fluid, amount);
         return this;
      }

      public IRefiningRecipeList.Builder secondInput(FluidStack fluid) {
         this.secondInput = fluid;
         return this;
      }

      private void checkNBT() {
         if (this.data == null) {
            this.data = new CompoundTag();
         }
      }

      public IRefiningRecipeList.Builder addNBT(String id, Tag nbt) {
         this.checkNBT();
         this.data.put(id, nbt);
         return this;
      }

      public IRefiningRecipeList.Builder addNBT(CompoundTag data) {
         this.checkNBT();
         data.merge(data);
         return this;
      }

      public IRefiningRecipeList.Builder setNBT(CompoundTag data) {
         this.data = data;
         return this;
      }

      public IRefiningRecipeList.Builder addFlag(RecipeFlags flag, boolean value) {
         this.checkNBT();
         flag.applyFlag(this.data, value);
         return this;
      }

      public IRefiningRecipeList.Builder addMod(RecipeMods mod, int value) {
         this.checkNBT();
         mod.create(this.data, value);
         return this;
      }

      public IRefiningRecipeList.Builder addMod(RecipeMods mod, double value) {
         this.checkNBT();
         mod.create(this.data, value);
         return this;
      }

      public IRefiningRecipeList.Builder addMod(RecipeMods mod, int exactValue, double modValue) {
         this.checkNBT();
         mod.create(this.data, modValue, exactValue);
         return this;
      }

      public void buildSimple(IOutputGenerator output) {
         this.registry.addRecipe(this.id, this.firstInput, this.secondInput, this.catalyst, new SimpleFluidOutput(ObjectLists.singleton(output), this.data));
      }

      public void buildSimple(IOutputGenerator output, FluidStack... fluidOutputs) {
         this.registry
            .addRecipe(
               this.id,
               this.firstInput,
               this.secondInput,
               this.catalyst,
               new SimpleFluidOutput(ObjectLists.singleton(output), ObjectArrayList.wrap(fluidOutputs), this.data)
            );
      }

      public void buildSimple(IOutputGenerator... itemOutputs) {
         this.registry
            .addRecipe(this.id, this.firstInput, this.secondInput, this.catalyst, new SimpleFluidOutput(ObjectArrayList.wrap(itemOutputs), this.data));
      }

      public void buildSimple(FluidStack... fluidOutputs) {
         this.registry
            .addRecipe(
               this.id,
               this.firstInput,
               this.secondInput,
               this.catalyst,
               new SimpleFluidOutput(Collections.emptyList(), ObjectArrayList.wrap(fluidOutputs), this.data)
            );
      }

      public void buildSimple(List<IOutputGenerator> itemOutputs, List<FluidStack> fluidOutputs) {
         this.registry.addRecipe(this.id, this.firstInput, this.secondInput, this.catalyst, new SimpleFluidOutput(itemOutputs, fluidOutputs, this.data));
      }

      public void buildRange(IOutputGenerator itemOutput, int minItem, int maxItem) {
         this.registry.addRecipe(this.id, this.firstInput, this.secondInput, this.catalyst, new RangeFluidOutput(itemOutput, minItem, maxItem, this.data));
      }

      public void buildRange(FluidStack fluidOutput, int minFluid, int maxFluid) {
         this.registry.addRecipe(this.id, this.firstInput, this.secondInput, this.catalyst, new RangeFluidOutput(fluidOutput, minFluid, maxFluid, this.data));
      }

      public void buildRange(IOutputGenerator itemOutput, int minItem, int maxItem, FluidStack fluidOutput, int minFluid, int maxFluid) {
         this.registry
            .addRecipe(
               this.id,
               this.firstInput,
               this.secondInput,
               this.catalyst,
               new RangeFluidOutput(itemOutput, minItem, maxItem, fluidOutput, minFluid, maxFluid, this.data)
            );
      }

      public void buildCustom(Function<CompoundTag, IFluidRecipeOutput> mapper) {
         this.registry.addRecipe(this.id, this.firstInput, this.secondInput, this.catalyst, mapper.apply(this.data));
      }
   }

   public static class FluidRecipe {
      ResourceLocation location;
      FluidStack firstTank;
      FluidStack secondTank;
      IInput itemInput;
      IFluidRecipeOutput output;

      public FluidRecipe(ResourceLocation location, FluidStack firstTank, FluidStack secondTank, IInput itemInput, IFluidRecipeOutput output) {
         this.location = location;
         this.firstTank = firstTank;
         this.secondTank = secondTank;
         this.itemInput = itemInput;
         this.output = output;
      }

      public ResourceLocation getLocation() {
         return this.location;
      }

      public FluidStack getFirstTank() {
         return this.firstTank;
      }

      public FluidStack getSecondTank() {
         return this.secondTank;
      }

      public IInput getItemInput() {
         return this.itemInput;
      }

      public IFluidRecipeOutput getOutput() {
         return this.output;
      }

      public IRefiningRecipeList.FluidRecipe invert() {
         return new IRefiningRecipeList.FluidRecipe(
            new ResourceLocation(this.location.getNamespace(), this.location.getNamespace() + "_inverted"),
            this.secondTank,
            this.firstTank,
            this.itemInput,
            this.output
         );
      }
   }
}
