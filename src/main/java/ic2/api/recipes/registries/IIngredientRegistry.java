package ic2.api.recipes.registries;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.ingridients.recipes.IFluidRecipeOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IIngredientRegistry {
   void registerInput(
      ResourceLocation var1,
      Class<? extends IInput> var2,
      Function<FriendlyByteBuf, IInput> var3,
      Function<JsonObject, IInput> var4,
      @Nullable Function<Object, IInput> var5
   );

   void registerRecipeOutput(
      ResourceLocation var1, Class<? extends IRecipeOutput> var2, Function<FriendlyByteBuf, IRecipeOutput> var3, Function<JsonObject, IRecipeOutput> var4
   );

   void registerFluidOutput(
      ResourceLocation var1,
      Class<? extends IFluidRecipeOutput> var2,
      Function<FriendlyByteBuf, IFluidRecipeOutput> var3,
      Function<JsonObject, IFluidRecipeOutput> var4
   );

   void registerQueue(ResourceLocation var1, Class<? extends IStackOutput> var2, Function<CompoundTag, IStackOutput> var3);

   void registerOutputGenerators(ResourceLocation var1, Class<? extends IOutputGenerator> var2, Function<JsonObject, IOutputGenerator> var3);

   void writeInput(IInput var1, FriendlyByteBuf var2);

   void writeRecipeOutput(IRecipeOutput var1, FriendlyByteBuf var2);

   void writeFluidOutput(IFluidRecipeOutput var1, FriendlyByteBuf var2);

   CompoundTag writeQueue(IStackOutput var1);

   IInput readInput(FriendlyByteBuf var1);

   IRecipeOutput createOutput(FriendlyByteBuf var1);

   IFluidRecipeOutput createFluidOutput(FriendlyByteBuf var1);

   IStackOutput readQueue(CompoundTag var1);

   IInput readInput(JsonObject var1);

   IRecipeOutput readOutput(JsonObject var1);

   IFluidRecipeOutput readFluidOutput(JsonObject var1);

   IOutputGenerator readOutputGenerator(JsonObject var1);

   IInput createInputFrom(Object var1);

   JsonObject serializeInput(IInput var1);

   JsonObject serializeOutput(IRecipeOutput var1);

   JsonObject serializeFluidOutput(IFluidRecipeOutput var1);

   JsonObject serializeOutputGenerator(IOutputGenerator var1);
}
