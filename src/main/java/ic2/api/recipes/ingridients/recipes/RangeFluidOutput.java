package ic2.api.recipes.ingridients.recipes;

import com.google.gson.JsonObject;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.EmptyGenerator;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class RangeFluidOutput extends BaseRecipeOutput implements IFluidRecipeOutput {
   CompoundTag nbt;
   ItemStack output;
   int minValue;
   int maxValue;
   FluidStack fluidOutput;
   int minFluid;
   int maxFluid;

   public RangeFluidOutput(JsonObject obj) {
      IOutputGenerator generator = RecipeRegistry.INGREDIENTS.readOutputGenerator(obj.getAsJsonObject("output"));
      generator.addItems(T -> this.output = T);
      this.generators.add(generator);
      this.minValue = obj.get("minOut").getAsInt();
      this.maxValue = obj.get("maxOut").getAsInt();
      this.fluidOutput = IInput.readFluidStack(obj.getAsJsonObject("fluidOutput"));
      this.minFluid = obj.get("minFluid").getAsInt();
      this.maxFluid = obj.get("maxFluid").getAsInt();
      if (obj.has("nbt")) {
         this.nbt = IInput.readNBT(obj.get("nbt").getAsString());
      }
   }

   public RangeFluidOutput(FriendlyByteBuf buffer) {
      this.output = buffer.readItem();
      this.minValue = buffer.readByte();
      this.maxValue = buffer.readByte();
      this.fluidOutput = buffer.readFluidStack();
      this.minFluid = buffer.readVarInt();
      this.maxFluid = buffer.readVarInt();
   }

   public RangeFluidOutput(IOutputGenerator output, int minValue, int maxValue) {
      output.addItems(T -> this.output = T);
      this.generators.add(output);
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.fluidOutput = FluidStack.EMPTY;
   }

   public RangeFluidOutput(IOutputGenerator output, int minValue, int maxValue, CompoundTag nbt) {
      output.addItems(T -> this.output = T);
      this.generators.add(output);
      this.nbt = nbt;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.fluidOutput = FluidStack.EMPTY;
   }

   public RangeFluidOutput(FluidStack fluidOutput, int minFluid, int maxFluid) {
      this.output = ItemStack.EMPTY;
      this.generators.add(EmptyGenerator.EMPTY);
      this.fluidOutput = fluidOutput.copy();
      this.minFluid = minFluid;
      this.maxFluid = maxFluid;
   }

   public RangeFluidOutput(FluidStack fluidOutput, int minFluid, int maxFluid, CompoundTag nbt) {
      this.output = ItemStack.EMPTY;
      this.generators.add(EmptyGenerator.EMPTY);
      this.nbt = nbt;
      this.fluidOutput = fluidOutput.copy();
      this.minFluid = minFluid;
      this.maxFluid = maxFluid;
   }

   public RangeFluidOutput(IOutputGenerator output, int minValue, int maxValue, FluidStack fluidOutput, int minFluid, int maxFluid) {
      output.addItems(T -> this.output = T);
      this.generators.add(output);
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.fluidOutput = fluidOutput.copy();
      this.minFluid = minFluid;
      this.maxFluid = maxFluid;
   }

   public RangeFluidOutput(IOutputGenerator output, int minValue, int maxValue, FluidStack fluidOutput, int minFluid, int maxFluid, CompoundTag nbt) {
      output.addItems(T -> this.output = T);
      this.generators.add(output);
      this.nbt = nbt;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.fluidOutput = fluidOutput.copy();
      this.minFluid = minFluid;
      this.maxFluid = maxFluid;
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags) {
      if (this.output.isEmpty()) {
         return ObjectLists.emptyList();
      } else {
         int value = this.minValue + rand.nextInt(this.maxValue - this.minValue + 1);
         if (value <= 0) {
            return ObjectLists.emptyList();
         } else {
            ItemStack stack = this.output.copy();
            stack.setCount(value);
            return ObjectLists.singleton(stack);
         }
      }
   }

   @Override
   public List<ItemStack> getAllOutputs() {
      if (this.output.isEmpty()) {
         return ObjectLists.emptyList();
      } else {
         List<ItemStack> result = new ObjectArrayList();
         int diff = this.maxValue - this.minValue;
         if (diff > 5) {
            ItemStack copy = this.output.copy();
            copy.setCount(Math.max(1, this.minValue));
            result.add(copy);
            copy = this.output.copy();
            copy.setCount(this.maxValue);
            result.add(copy);
         } else {
            for (int i = this.minValue; i <= this.maxValue; i++) {
               if (i != 0) {
                  ItemStack stack = this.output.copy();
                  stack.setCount(i);
                  result.add(stack);
               }
            }
         }

         return result;
      }
   }

   @Override
   public List<FluidStack> onFluidRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags, ItemStack input) {
      if (this.fluidOutput.isEmpty()) {
         return ObjectLists.emptyList();
      } else {
         int value = this.minValue + rand.nextInt(this.maxValue - this.minValue + 1);
         return value <= 0 ? ObjectLists.emptyList() : ObjectLists.singleton(new FluidStack(this.fluidOutput, value));
      }
   }

   @Override
   public List<FluidStack> getAllFluidOutputs() {
      if (this.fluidOutput.isEmpty()) {
         return ObjectLists.emptyList();
      } else {
         int step = Math.min(1, (this.maxFluid - this.minFluid) / 10);
         List<FluidStack> fluids = new ObjectArrayList();

         for (int value = this.minFluid; value <= this.maxFluid; value = Math.min(this.maxFluid, value + step)) {
            fluids.add(new FluidStack(this.fluidOutput, value));
            if (value == this.maxFluid) {
               break;
            }
         }

         return fluids;
      }
   }

   @Override
   public CompoundTag getMetadata() {
      return this.nbt == null ? EMPTY_COMPOUND : this.nbt;
   }

   @Override
   public float getExperience() {
      return 0.0F;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeItem(this.output);
      buffer.writeByte(this.minValue);
      buffer.writeByte(this.maxValue);
      buffer.writeFluidStack(this.fluidOutput);
      buffer.writeVarInt(this.minFluid);
      buffer.writeVarInt(this.maxFluid);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.add("output", RecipeRegistry.INGREDIENTS.serializeOutputGenerator(this.generators.get(0)));
      obj.addProperty("minOut", this.minValue);
      obj.addProperty("maxOut", this.maxValue);
      obj.add("fluidOutput", IInput.writeFluidStack(this.fluidOutput));
      obj.addProperty("minFluid", this.minFluid);
      obj.addProperty("maxFluid", this.maxFluid);
      if (this.nbt != null && this.nbt != EMPTY_COMPOUND) {
         obj.addProperty("nbt", this.nbt.toString());
      }

      return obj;
   }
}
