package ic2.core.platform.recipes.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.api.items.IRepairable;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.registries.ICannerRecipeRegistry;
import ic2.core.block.machines.recipes.IRecipeList;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class CannerRegistry extends BaseRegistry<ICannerRecipeRegistry> implements ICannerRecipeRegistry, IRecipeList {
   Map<Item, List<Tuple<IInput, Integer>>> repairRegistry = CollectionUtils.<Item, List<Tuple<IInput, Integer>>>createLinkedMap();
   Map<ItemStack, ICannerRecipeRegistry.FuelValue> fuelPoints = CollectionUtils.<ItemStack, ICannerRecipeRegistry.FuelValue>createLinkedMap(
      ItemStackStrategy.INSTANCE
   );
   Map<ItemStack, List<Tuple<IInput, ItemStack>>> cannable = CollectionUtils.<ItemStack, List<Tuple<IInput, ItemStack>>>createLinkedMap(
      ItemStackStrategy.INSTANCE
   );

   public CannerRegistry(Consumer<ICannerRecipeRegistry> listener) {
      this.registerListener(listener);
   }

   @Override
   protected void reloadInternals() {
      this.repairRegistry.clear();
      this.fuelPoints.clear();
      this.cannable.clear();
   }

   @Override
   public String getFolder() {
      return "canner";
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      if (!data.has("recipes")) {
         String type = GsonHelper.getAsString(data, "type");
         if (type.equals("fuel")) {
            ItemStack stack = this.readItem(GsonHelper.getAsJsonObject(data, "input"));
            if (GsonHelper.getAsBoolean(data, "remove", false)) {
               this.removeValue(stack);
               return;
            }

            if (GsonHelper.getAsBoolean(data, "multiplier", false)) {
               this.registerFuelMultiplier(stack, GsonHelper.getAsFloat(data, "value"));
            } else {
               this.registerFuelValue(stack, GsonHelper.getAsInt(data, "value"));
            }
         } else if (type.equals("canning")) {
            ItemStack container = this.readItem(GsonHelper.getAsJsonObject(data, "container"));
            IInput input = IngredientRegistry.INSTANCE.readInput(GsonHelper.getAsJsonObject(data, "input"));
            if (GsonHelper.getAsBoolean(data, "remove", false)) {
               this.removeFillable(container, input.getComponents().get(0));
               return;
            }

            this.registerFillable(container, input, this.readItem(GsonHelper.getAsJsonObject(data, "output")));
         }
      } else {
         for (JsonElement elem : GsonHelper.getAsJsonArray(data, "recipes")) {
            JsonObject obj = elem.getAsJsonObject();
            this.readRecipe(id, obj);
         }
      }
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> map = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();

      for (Entry<ItemStack, ICannerRecipeRegistry.FuelValue> entry : this.fuelPoints.entrySet()) {
         ICannerRecipeRegistry.FuelValue value = entry.getValue();
         JsonObject obj = new JsonObject();
         obj.addProperty("type", "fuel");
         obj.add("input", this.writeItem(entry.getKey()));
         if (value.isMultiplier()) {
            obj.addProperty("multiplier", true);
            obj.addProperty("value", value.getFuelMultiplier());
         } else {
            obj.addProperty("multiplier", false);
            obj.addProperty("value", value.getFuelValue());
         }

         map.put(new ResourceLocation("ic2", "fuel_" + ForgeRegistryWrapper.getItemKey(entry.getKey().getItem()).getPath()), obj);
      }

      for (Entry<ItemStack, List<Tuple<IInput, ItemStack>>> entry : this.cannable.entrySet()) {
         JsonObject container = this.writeItem(entry.getKey());

         for (Tuple<IInput, ItemStack> subEntry : entry.getValue()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "canning");
            obj.add("input", IngredientRegistry.INSTANCE.serializeInput((IInput)subEntry.getA()));
            obj.add("container", container);
            obj.add("output", this.writeItem((ItemStack)subEntry.getB()));
            map.put(
               new ResourceLocation("ic2", "canner_" + ForgeRegistryWrapper.getItemKey(((IInput)subEntry.getA()).getComponents().get(0).getItem()).getPath()),
               obj
            );
         }
      }

      return map;
   }

   private JsonObject writeItem(ItemStack stack) {
      return IngredientRegistry.INSTANCE
         .serializeOutputGenerator((IOutputGenerator)(stack.hasTag() ? new ItemWithNBTGenerator(stack) : new ItemGenerator(stack.getItem(), stack.getCount())));
   }

   private ItemStack readItem(JsonObject obj) {
      ItemStack[] stack = new ItemStack[]{ItemStack.EMPTY};
      IngredientRegistry.INSTANCE.readOutputGenerator(obj).addItems(T -> stack[0] = T);
      return stack[0];
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.repairRegistry.size());

      for (Entry<Item, List<Tuple<IInput, Integer>>> entry : this.repairRegistry.entrySet()) {
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, entry.getKey());
         List<Tuple<IInput, Integer>> list = entry.getValue();
         buffer.writeVarInt(list.size());

         for (Tuple<IInput, Integer> sub : list) {
            RecipeRegistry.INGREDIENTS.writeInput((IInput)sub.getA(), buffer);
            buffer.writeInt((Integer)sub.getB());
         }
      }

      buffer.writeVarInt(this.fuelPoints.size());

      for (ICannerRecipeRegistry.FuelValue entry : this.fuelPoints.values()) {
         entry.write(buffer);
      }

      buffer.writeVarInt(this.cannable.size());

      for (Entry<ItemStack, List<Tuple<IInput, ItemStack>>> entry : this.cannable.entrySet()) {
         buffer.writeItem(entry.getKey());
         List<Tuple<IInput, ItemStack>> list = entry.getValue();
         buffer.writeVarInt(list.size());

         for (Tuple<IInput, ItemStack> subEntry : list) {
            RecipeRegistry.INGREDIENTS.writeInput((IInput)subEntry.getA(), buffer);
            buffer.writeItem((ItemStack)subEntry.getB());
         }
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Map<Item, List<Tuple<IInput, Integer>>> replaceMap = CollectionUtils.<Item, List<Tuple<IInput, Integer>>>createLinkedMap();
      int read = buffer.readVarInt();

      for (int i = 0; i < read; i++) {
         Item item = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
         int size = buffer.readVarInt();
         List<Tuple<IInput, Integer>> list = CollectionUtils.<Tuple<IInput, Integer>>createList();

         for (int j = 0; j < size; j++) {
            IInput input = RecipeRegistry.INGREDIENTS.readInput(buffer);
            list.add(new Tuple(input, buffer.readInt()));
         }

         replaceMap.put(item, list);
      }

      this.repairRegistry = replaceMap;
      read = buffer.readVarInt();
      Map<ItemStack, ICannerRecipeRegistry.FuelValue> replaceFuel = CollectionUtils.<ItemStack, ICannerRecipeRegistry.FuelValue>createLinkedMap(
         ItemStackStrategy.INSTANCE
      );

      for (int i = 0; i < read; i++) {
         ICannerRecipeRegistry.FuelValue value = new ICannerRecipeRegistry.FuelValue(buffer);
         replaceFuel.put(value.getStack(), value);
      }

      this.fuelPoints = replaceFuel;
      Map<ItemStack, List<Tuple<IInput, ItemStack>>> canReplace = CollectionUtils.<ItemStack, List<Tuple<IInput, ItemStack>>>createLinkedMap(
         ItemStackStrategy.INSTANCE
      );
      read = buffer.readVarInt();

      for (int i = 0; i < read; i++) {
         ItemStack container = buffer.readItem();
         List<Tuple<IInput, ItemStack>> list = CollectionUtils.<Tuple<IInput, ItemStack>>createList();
         int size = buffer.readVarInt();

         for (int j = 0; j < size; j++) {
            IInput input = RecipeRegistry.INGREDIENTS.readInput(buffer);
            list.add(new Tuple(input, buffer.readItem()));
         }

         canReplace.put(container, list);
      }

      this.cannable = canReplace;
   }

   @Override
   public void registerRepairable(IRepairable repair, IInput input, int repairAmount) {
      List<Tuple<IInput, Integer>> list = this.repairRegistry.computeIfAbsent(repair.asItem(), k -> CollectionUtils.createList());
      list.add(new Tuple(input, repairAmount));
   }

   @Override
   public Tuple<IInput, Integer> getRepairableItem(ItemStack input, ItemStack repair, boolean compareStack) {
      for (Tuple<IInput, Integer> entry : this.repairRegistry.getOrDefault(input.getItem(), Collections.emptyList())) {
         if (((IInput)entry.getA()).matches(repair)) {
            return entry;
         }
      }

      return null;
   }

   @Override
   public Map<Item, List<Tuple<IInput, Integer>>> getRepairItems() {
      return new Object2ObjectLinkedOpenHashMap(this.repairRegistry);
   }

   @Override
   public void removeRepairable(IRepairable repair) {
      this.repairRegistry.remove(repair.asItem());
   }

   @Override
   public void registerFuelValue(ItemStack stack, int amount) {
      this.fuelPoints.put(stack, new ICannerRecipeRegistry.FuelValue(stack.copy(), amount));
   }

   @Override
   public void registerFuelMultiplier(ItemStack stack, float multiplier) {
      this.fuelPoints.put(stack, new ICannerRecipeRegistry.FuelValue(stack.copy(), multiplier));
   }

   @Override
   public ICannerRecipeRegistry.FuelValue getValueForStack(ItemStack stack) {
      return this.fuelPoints.get(stack);
   }

   @Override
   public List<ICannerRecipeRegistry.FuelValue> getFuels() {
      return new ObjectArrayList(this.fuelPoints.values());
   }

   @Override
   public void removeValue(ItemStack stack) {
      this.fuelPoints.remove(stack);
   }

   @Override
   public void registerFillable(ItemStack container, IInput fillable, ItemStack output) {
      List<Tuple<IInput, ItemStack>> list = this.cannable.computeIfAbsent(container, k -> CollectionUtils.createList());
      list.add(new Tuple(fillable, output.copy()));
   }

   @Override
   public Tuple<IInput, ItemStack> getFillOutput(ItemStack container, ItemStack toFill, boolean compareStack) {
      for (Tuple<IInput, ItemStack> entry : this.cannable.get(container)) {
         if (((IInput)entry.getA()).matches(toFill) && (!compareStack || ((IInput)entry.getA()).getInputSize() <= toFill.getCount())) {
            return entry;
         }
      }

      return null;
   }

   @Override
   public Map<ItemStack, List<Tuple<IInput, ItemStack>>> getFillables() {
      Map<ItemStack, List<Tuple<IInput, ItemStack>>> maps = CollectionUtils.<ItemStack, List<Tuple<IInput, ItemStack>>>createLinkedMap(
         ItemStackStrategy.INSTANCE
      );

      for (Entry<ItemStack, List<Tuple<IInput, ItemStack>>> entry : this.cannable.entrySet()) {
         maps.put(entry.getKey().copy(), entry.getValue());
      }

      return maps;
   }

   @Override
   public boolean hasContainer(ItemStack container) {
      return this.cannable.get(container) != null;
   }

   @Override
   public void removeFillable(ItemStack container) {
      this.cannable.remove(container);
   }

   @Override
   public void removeFillable(ItemStack container, ItemStack fillable) {
      List<Tuple<IInput, ItemStack>> list = this.cannable.get(container);
      if (list != null) {
         Iterator<Tuple<IInput, ItemStack>> iter = list.iterator();

         while (iter.hasNext()) {
            if (((IInput)iter.next().getA()).matches(fillable)) {
               iter.remove();
            }
         }

         if (list.isEmpty()) {
            this.cannable.remove(container);
         }
      }
   }
}
