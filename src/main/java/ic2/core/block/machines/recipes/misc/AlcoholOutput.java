package ic2.core.block.machines.recipes.misc;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.recipes.IFluidRecipeOutput;
import ic2.core.item.food_and_drink.drinks.Beer;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.utils.helpers.StackUtil;
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

public class AlcoholOutput implements IFluidRecipeOutput {
   Item output;

   public AlcoholOutput(Item output) {
      this.output = output;
   }

   public AlcoholOutput(FriendlyByteBuf buffer) {
      this.output = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
   }

   public AlcoholOutput(JsonObject obj) {
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
      int data = StackUtil.getNbtData(input).getInt("data");
      int hops = Beer.getHops(data);
      int time = Beer.getTime(data);
      return (List<FluidStack>)(hops >= 2 && time >= 2 && time < 5
         ? ObjectLists.singleton(new FluidStack(IC2Fluids.ALCOHOL, hops * 10 * (time + 1)))
         : Collections.emptyList());
   }

   @Override
   public List<FluidStack> getAllFluidOutputs() {
      List<FluidStack> allFluids = new ObjectArrayList();

      for (int i = 2; i < 6; i++) {
         for (int j = 2; j < 5; j++) {
            allFluids.add(new FluidStack(IC2Fluids.ALCOHOL, j * 10 * i));
         }
      }

      return allFluids;
   }
}
