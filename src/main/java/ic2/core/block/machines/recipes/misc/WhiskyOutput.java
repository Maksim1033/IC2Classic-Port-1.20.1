package ic2.core.block.machines.recipes.misc;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.recipes.IFluidRecipeOutput;
import ic2.core.item.food_and_drink.drinks.Whisky;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Fluids;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class WhiskyOutput implements IFluidRecipeOutput {
   Item output;

   public WhiskyOutput(Item output) {
      this.output = output;
   }

   public WhiskyOutput(FriendlyByteBuf buffer) {
      this.output = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
   }

   public WhiskyOutput(JsonObject obj) {
      this.output = ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString()));
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags) {
      return ObjectLists.singleton(new ItemStack(this.output));
   }

   @Override
   public List<ItemStack> getAllOutputs() {
      return ObjectLists.singleton(new ItemStack(this.output));
   }

   @Override
   public CompoundTag getMetadata() {
      return EMPTY_COMPOUND;
   }

   @Override
   public float getExperience() {
      return 0.0F;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.output);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.output).toString());
      return obj;
   }

   @Override
   public List<FluidStack> onFluidRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags, ItemStack input) {
      int year = Whisky.getYear(input);
      return (List<FluidStack>)(year <= 0 ? Collections.emptyList() : ObjectLists.singleton(new FluidStack(IC2Fluids.ALCOHOL, this.getAmountForYear(year))));
   }

   private int getAmountForYear(int year) {
      switch (year) {
         case 1:
            return 10000;
         case 2:
            return 12000;
         case 3:
            return 15000;
         case 4:
            return 25000;
         case 5:
            return 50000;
         default:
            return 0;
      }
   }

   @Override
   public List<FluidStack> getAllFluidOutputs() {
      List<FluidStack> allFluids = new ObjectArrayList();

      for (int i = 1; i < 6; i++) {
         allFluids.add(new FluidStack(IC2Fluids.ALCOHOL, this.getAmountForYear(i)));
      }

      return allFluids;
   }
}
