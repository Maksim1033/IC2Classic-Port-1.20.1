package ic2.core.platform.recipes.misc;

import com.electronwill.nightconfig.core.file.NoFormatFoundException;
import com.google.gson.JsonObject;
import ic2.api.crops.BaseSeed;
import ic2.api.crops.ICrop;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.EmptyGenerator;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.ingridients.inputs.ArrayInput;
import ic2.api.recipes.ingridients.inputs.EmptyInput;
import ic2.api.recipes.ingridients.inputs.FluidInput;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.IngredientInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.api.recipes.ingridients.inputs.ItemListInput;
import ic2.api.recipes.ingridients.inputs.ItemStackInput;
import ic2.api.recipes.ingridients.inputs.ItemTagInput;
import ic2.api.recipes.ingridients.inputs.SubItemInput;
import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.ingridients.queue.MultiStackOutput;
import ic2.api.recipes.ingridients.queue.SimpleStackOutput;
import ic2.api.recipes.ingridients.recipes.ChanceRecipeOutput;
import ic2.api.recipes.ingridients.recipes.IFluidRecipeOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.ingridients.recipes.RangeFluidOutput;
import ic2.api.recipes.ingridients.recipes.RangeRecipeOutput;
import ic2.api.recipes.ingridients.recipes.SimpleFluidOutput;
import ic2.api.recipes.ingridients.recipes.SimpleRecipeOutput;
import ic2.api.recipes.registries.IIngredientRegistry;
import ic2.core.block.machines.recipes.misc.AlcoholInput;
import ic2.core.block.machines.recipes.misc.AlcoholOutput;
import ic2.core.block.machines.recipes.misc.ScrapBoxOutput;
import ic2.core.block.machines.recipes.misc.ScrapOutput;
import ic2.core.block.machines.recipes.misc.WhiskyOutput;
import ic2.core.platform.recipes.crafting.helpers.CropGenerator;
import ic2.core.platform.recipes.crafting.helpers.CropInput;
import ic2.core.platform.recipes.crafting.helpers.DamagedInput;
import ic2.core.platform.recipes.crafting.helpers.FluidGenerator;
import ic2.core.platform.recipes.mods.CountingModifier;
import ic2.core.platform.recipes.mods.EmptyInventoryModifier;
import ic2.core.platform.recipes.mods.EnchantmentModifier;
import ic2.core.platform.recipes.mods.FlagModifier;
import ic2.core.platform.recipes.mods.IRecipeModifier;
import ic2.core.platform.recipes.mods.InfoModifier;
import ic2.core.platform.recipes.mods.ModularArmorModifier;
import ic2.core.platform.recipes.mods.NumberModifier;
import ic2.core.platform.recipes.mods.TransferInventoryModifier;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.FunctionRegistry;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class IngredientRegistry implements IIngredientRegistry {
   public static final IngredientRegistry INSTANCE = new IngredientRegistry();
   List<Function<Object, IInput>> creators = CollectionUtils.<Function<Object, IInput>>createList();
   FunctionRegistry.BiFunctionRegistry<IInput, FriendlyByteBuf, JsonObject, Function<FriendlyByteBuf, IInput>, Function<JsonObject, IInput>> inputRegistry = CollectionUtils.createBiFunctionRegistry();
   FunctionRegistry.BiFunctionRegistry<IRecipeOutput, FriendlyByteBuf, JsonObject, Function<FriendlyByteBuf, IRecipeOutput>, Function<JsonObject, IRecipeOutput>> recipeRegistry = CollectionUtils.createBiFunctionRegistry();
   FunctionRegistry.BiFunctionRegistry<IFluidRecipeOutput, FriendlyByteBuf, JsonObject, Function<FriendlyByteBuf, IFluidRecipeOutput>, Function<JsonObject, IFluidRecipeOutput>> fluidOutputRegistry = CollectionUtils.createBiFunctionRegistry();
   FunctionRegistry.BiFunctionRegistry<IRecipeModifier, FriendlyByteBuf, JsonObject, Function<FriendlyByteBuf, IRecipeModifier>, Function<JsonObject, IRecipeModifier>> modRegistry = CollectionUtils.createBiFunctionRegistry();
   FunctionRegistry<IStackOutput, CompoundTag, Function<CompoundTag, IStackOutput>> queueRegistry = CollectionUtils.createFunctionRegistry();
   FunctionRegistry<IOutputGenerator, JsonObject, Function<JsonObject, IOutputGenerator>> outputGenerators = CollectionUtils.createFunctionRegistry();

   public IngredientRegistry() {
      this.registerInput(
         new ResourceLocation("ic2", "item"), ItemInput.class, ItemInput::new, ItemInput::new, T -> T instanceof ItemLike ? new ItemInput((ItemLike)T) : null
      );
      this.registerInput(
         new ResourceLocation("ic2", "tag"),
         ItemTagInput.class,
         ItemTagInput::new,
         ItemTagInput::new,
         T -> T instanceof TagKey ? new ItemTagInput((TagKey<Item>)T) : null
      );
      this.registerInput(
         new ResourceLocation("ic2", "nbt"),
         ItemStackInput.class,
         ItemStackInput::new,
         ItemStackInput::new,
         T -> T instanceof ItemStack ? new ItemStackInput((ItemStack)T) : null
      );
      this.registerInput(new ResourceLocation("ic2", "array"), ArrayInput.class, ArrayInput::new, ArrayInput::new, null);
      this.registerInput(
         new ResourceLocation("ic2", "ingredient"),
         IngredientInput.class,
         IngredientInput::new,
         IngredientInput::new,
         T -> T instanceof Ingredient ? new IngredientInput((Ingredient)T, 1) : null
      );
      this.registerInput(
         new ResourceLocation("ic2", "fluid"),
         FluidInput.class,
         FluidInput::new,
         FluidInput::new,
         T -> T instanceof Fluid ? new FluidInput((Fluid)T) : (T instanceof FluidStack ? new FluidInput((FluidStack)T) : null)
      );
      this.registerInput(
         new ResourceLocation("ic2", "item_list"),
         ItemListInput.class,
         ItemListInput::new,
         ItemListInput::new,
         T -> T instanceof ItemLike[]
               ? ItemListInput.createItemList((ItemLike[])T)
               : (T instanceof ItemStack[] ? ItemListInput.createItemList((ItemStack[])T) : null)
      );
      this.registerInput(
         new ResourceLocation("ic2", "crop"),
         CropInput.class,
         CropInput::new,
         CropInput::new,
         T -> T instanceof ICrop ? new CropInput((ICrop)T) : (T instanceof BaseSeed ? new CropInput((BaseSeed)T, false) : null)
      );
      this.registerInput(new ResourceLocation("ic2", "damaged"), DamagedInput.class, DamagedInput::new, DamagedInput::new, null);
      this.registerInput(new ResourceLocation("ic2", "sub_item"), SubItemInput.class, SubItemInput::new, SubItemInput::new, null);
      this.registerInput(new ResourceLocation("ic2", "empty"), EmptyInput.class, T -> EmptyInput.INSTANCE, T -> EmptyInput.INSTANCE, null);
      this.registerInput(
         new ResourceLocation("ic2", "internal"),
         EmptyInput.InternalInput.class,
         T -> EmptyInput.InternalInput.INSTANCE,
         T -> EmptyInput.InternalInput.INSTANCE,
         null
      );
      this.registerInput(new ResourceLocation("ic2", "alcohol"), AlcoholInput.class, AlcoholInput::new, AlcoholInput::new, null);
      this.registerRecipeOutput(new ResourceLocation("ic2", "simple"), SimpleRecipeOutput.class, SimpleRecipeOutput::new, SimpleRecipeOutput::new);
      this.registerRecipeOutput(new ResourceLocation("ic2", "chance"), ChanceRecipeOutput.class, ChanceRecipeOutput::new, ChanceRecipeOutput::new);
      this.registerRecipeOutput(new ResourceLocation("ic2", "scrap"), ScrapOutput.class, ScrapOutput::new, ScrapOutput::new);
      this.registerRecipeOutput(new ResourceLocation("ic2", "scrapbox"), ScrapBoxOutput.class, ScrapBoxOutput::new, ScrapBoxOutput::new);
      this.registerRecipeOutput(new ResourceLocation("ic2", "range"), RangeRecipeOutput.class, RangeRecipeOutput::new, RangeRecipeOutput::new);
      this.registerFluidOutput(new ResourceLocation("ic2", "simple"), SimpleFluidOutput.class, SimpleFluidOutput::new, SimpleFluidOutput::new);
      this.registerFluidOutput(new ResourceLocation("ic2", "range"), RangeFluidOutput.class, RangeFluidOutput::new, RangeFluidOutput::new);
      this.registerFluidOutput(new ResourceLocation("ic2", "beer"), AlcoholOutput.class, AlcoholOutput::new, AlcoholOutput::new);
      this.registerFluidOutput(new ResourceLocation("ic2", "whisky"), WhiskyOutput.class, WhiskyOutput::new, WhiskyOutput::new);
      this.registerQueue(new ResourceLocation("ic2", "simple"), SimpleStackOutput.class, SimpleStackOutput::new);
      this.registerQueue(new ResourceLocation("ic2", "multi"), MultiStackOutput.class, MultiStackOutput::new);
      this.registerOutputGenerators(new ResourceLocation("ic2", "item"), ItemGenerator.class, ItemGenerator::new);
      this.registerOutputGenerators(new ResourceLocation("ic2", "nbt"), ItemWithNBTGenerator.class, ItemWithNBTGenerator::new);
      this.registerOutputGenerators(new ResourceLocation("ic2", "crop"), CropGenerator.class, CropGenerator::new);
      this.registerOutputGenerators(new ResourceLocation("ic2", "fluid"), FluidGenerator.class, FluidGenerator::new);
      this.registerOutputGenerators(new ResourceLocation("ic2", "empty"), EmptyGenerator.class, EmptyGenerator::new);
      this.modRegistry
         .register(new ResourceLocation("ic2", "armor_module_modifier"), ModularArmorModifier::new, ModularArmorModifier::new, ModularArmorModifier.class);
      this.modRegistry.register(new ResourceLocation("ic2", "flag_modifier"), FlagModifier::new, FlagModifier::new, FlagModifier.class);
      this.modRegistry.register(new ResourceLocation("ic2", "number_modifier"), NumberModifier::new, NumberModifier::new, NumberModifier.class);
      this.modRegistry
         .register(new ResourceLocation("ic2", "enchantment_modifier"), EnchantmentModifier::new, EnchantmentModifier::new, EnchantmentModifier.class);
      this.modRegistry.register(new ResourceLocation("ic2", "info_modifier"), InfoModifier::new, InfoModifier::new, InfoModifier.class);
      this.modRegistry
         .register(
            new ResourceLocation("ic2", "transfer_inventory_modifier"),
            TransferInventoryModifier::new,
            TransferInventoryModifier::new,
            TransferInventoryModifier.class
         );
      this.modRegistry
         .register(
            new ResourceLocation("ic2", "empty_inventory_modifier"), EmptyInventoryModifier::new, EmptyInventoryModifier::new, EmptyInventoryModifier.class
         );
      this.modRegistry.register(new ResourceLocation("ic2", "counter"), CountingModifier::new, CountingModifier::new, CountingModifier.class);
   }

   public void init() {
      RecipeRegistry.INGREDIENTS = this;
   }

   @Override
   public IInput createInputFrom(Object obj) {
      if (obj instanceof IInput) {
         return (IInput)obj;
      } else {
         for (Function<Object, IInput> entry : this.creators) {
            IInput input = entry.apply(obj);
            if (input != null) {
               return input;
            }
         }

         throw new NoFormatFoundException("Object not supported: " + obj);
      }
   }

   @Override
   public JsonObject serializeInput(IInput input) {
      JsonObject obj = input.serialize();
      ResourceLocation location = this.inputRegistry.getKey(input);
      if (location == null) {
         obj = new JsonObject();
         obj.addProperty("type", "ic2:internal");
         return obj;
      } else {
         obj.addProperty("type", location.toString());
         return obj;
      }
   }

   public JsonObject serializeModifier(IRecipeModifier modifier) {
      JsonObject obj = modifier.serialize();
      obj.addProperty("type", this.modRegistry.getKey(modifier).toString());
      return obj;
   }

   @Override
   public JsonObject serializeOutput(IRecipeOutput output) {
      JsonObject obj = output.serialize();
      obj.addProperty("type", this.recipeRegistry.getKey(output).toString());
      return obj;
   }

   @Override
   public JsonObject serializeFluidOutput(IFluidRecipeOutput output) {
      JsonObject obj = output.serialize();
      obj.addProperty("type", this.fluidOutputRegistry.getKey(output).toString());
      return obj;
   }

   @Override
   public JsonObject serializeOutputGenerator(IOutputGenerator generator) {
      JsonObject obj = generator.serialize();
      obj.addProperty("type", this.outputGenerators.getKey(generator.getClass()).toString());
      return obj;
   }

   @Override
   public void registerInput(
      ResourceLocation location,
      Class<? extends IInput> clz,
      Function<FriendlyByteBuf, IInput> creator,
      Function<JsonObject, IInput> deserializer,
      @Nullable Function<Object, IInput> maker
   ) {
      this.inputRegistry.register(location, creator, deserializer, clz);
      if (maker != null) {
         this.creators.add(maker);
      }
   }

   @Override
   public void registerRecipeOutput(
      ResourceLocation location,
      Class<? extends IRecipeOutput> clz,
      Function<FriendlyByteBuf, IRecipeOutput> creator,
      Function<JsonObject, IRecipeOutput> deserializer
   ) {
      this.recipeRegistry.register(location, creator, deserializer, clz);
   }

   @Override
   public void registerFluidOutput(
      ResourceLocation location,
      Class<? extends IFluidRecipeOutput> clz,
      Function<FriendlyByteBuf, IFluidRecipeOutput> creator,
      Function<JsonObject, IFluidRecipeOutput> deserializer
   ) {
      this.fluidOutputRegistry.register(location, creator, deserializer, clz);
   }

   @Override
   public void registerQueue(ResourceLocation location, Class<? extends IStackOutput> clz, Function<CompoundTag, IStackOutput> creator) {
      this.queueRegistry.register(location, creator, clz);
   }

   @Override
   public void registerOutputGenerators(ResourceLocation location, Class<? extends IOutputGenerator> clz, Function<JsonObject, IOutputGenerator> creator) {
      this.outputGenerators.register(location, creator, clz);
   }

   @Override
   public void writeInput(IInput input, FriendlyByteBuf buffer) {
      boolean valid = input != null && this.inputRegistry.containsValue(input);
      buffer.writeBoolean(valid);
      if (valid) {
         buffer.writeResourceLocation(this.inputRegistry.getKey(input));
         input.serialize(buffer);
      }
   }

   @Override
   public void writeRecipeOutput(IRecipeOutput output, FriendlyByteBuf buffer) {
      boolean valid = output != null && this.recipeRegistry.containsValue(output);
      buffer.writeBoolean(valid);
      if (valid) {
         buffer.writeResourceLocation(this.recipeRegistry.getKey(output));
         output.serialize(buffer);
      }
   }

   @Override
   public void writeFluidOutput(IFluidRecipeOutput output, FriendlyByteBuf buffer) {
      boolean valid = output != null && this.fluidOutputRegistry.containsValue(output);
      buffer.writeBoolean(valid);
      if (valid) {
         buffer.writeResourceLocation(this.fluidOutputRegistry.getKey(output));
         output.serialize(buffer);
      }
   }

   @Override
   public CompoundTag writeQueue(IStackOutput queue) {
      boolean flag = queue != null && this.queueRegistry.containsValue(queue);
      CompoundTag nbt = new CompoundTag();
      nbt.putBoolean("exist", flag);
      if (flag) {
         queue.save(nbt);
         nbt.putString("ID", this.queueRegistry.getKey(queue).toString());
      }

      return nbt;
   }

   public void writeMod(IRecipeModifier mod, FriendlyByteBuf buffer) {
      boolean valid = mod != null && this.modRegistry.containsValue(mod);
      buffer.writeBoolean(valid);
      if (valid) {
         buffer.writeResourceLocation(this.modRegistry.getKey(mod));
         mod.serialize(buffer);
      }
   }

   @Override
   public IInput readInput(FriendlyByteBuf buffer) {
      return buffer.readBoolean() ? this.inputRegistry.getValue(buffer.readResourceLocation()).apply(buffer) : null;
   }

   @Override
   public IInput readInput(JsonObject obj) {
      Function<JsonObject, IInput> inputs = this.inputRegistry.getDeserializer(ResourceLocation.tryParse(obj.get("type").getAsString()));
      return inputs != null ? inputs.apply(obj) : null;
   }

   @Override
   public IRecipeOutput readOutput(JsonObject obj) {
      Function<JsonObject, IRecipeOutput> inputs = this.recipeRegistry.getDeserializer(ResourceLocation.tryParse(obj.get("type").getAsString()));
      return inputs != null ? inputs.apply(obj) : null;
   }

   @Override
   public IFluidRecipeOutput readFluidOutput(JsonObject obj) {
      Function<JsonObject, IFluidRecipeOutput> inputs = this.fluidOutputRegistry.getDeserializer(ResourceLocation.tryParse(obj.get("type").getAsString()));
      return inputs != null ? inputs.apply(obj) : null;
   }

   @Override
   public IOutputGenerator readOutputGenerator(JsonObject obj) {
      Function<JsonObject, IOutputGenerator> generator = this.outputGenerators.getValue(ResourceLocation.tryParse(obj.get("type").getAsString()));
      return generator != null ? generator.apply(obj) : null;
   }

   @Override
   public IRecipeOutput createOutput(FriendlyByteBuf buffer) {
      return buffer.readBoolean() ? this.recipeRegistry.getValue(buffer.readResourceLocation()).apply(buffer) : null;
   }

   @Override
   public IFluidRecipeOutput createFluidOutput(FriendlyByteBuf buffer) {
      return buffer.readBoolean() ? this.fluidOutputRegistry.getValue(buffer.readResourceLocation()).apply(buffer) : null;
   }

   @Override
   public IStackOutput readQueue(CompoundTag nbt) {
      return nbt.getBoolean("exist") ? this.queueRegistry.getValue(new ResourceLocation(nbt.getString("ID"))).apply(nbt) : null;
   }

   public IRecipeModifier readMod(FriendlyByteBuf buffer) {
      return buffer.readBoolean() ? this.modRegistry.getValue(buffer.readResourceLocation()).apply(buffer) : null;
   }

   public IRecipeModifier readMod(JsonObject obj) {
      Function<JsonObject, IRecipeModifier> inputs = this.modRegistry.getDeserializer(ResourceLocation.tryParse(obj.get("type").getAsString()));
      return inputs != null ? inputs.apply(obj) : null;
   }
}
