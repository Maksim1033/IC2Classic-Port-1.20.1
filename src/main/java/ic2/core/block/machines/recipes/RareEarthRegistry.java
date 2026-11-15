package ic2.core.block.machines.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.registries.IRareEarthRegistry;
import ic2.core.IC2;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

public class RareEarthRegistry implements IRareEarthRegistry, IRecipeList {
   List<Consumer<IRareEarthRegistry>> listeners = ObjectLists.synchronize(CollectionUtils.createList());
   Object2ObjectMap<ResourceLocation, ItemStack> outputs = CollectionUtils.<ResourceLocation, ItemStack>createLinkedMap();
   Object2ObjectMap<ItemStack, IRareEarthRegistry.RareEntry> inputMap = CollectionUtils.<ItemStack, IRareEarthRegistry.RareEntry>createLinkedMap(
      ItemStackStrategy.INSTANCE
   );

   public RareEarthRegistry(Consumer<IRareEarthRegistry> listener) {
      this.registerListener(listener);
   }

   @Override
   public void registerListener(Consumer<IRareEarthRegistry> listener) {
      if (listener != null) {
         this.listeners.add(listener);
      }
   }

   public void reload() {
      this.outputs.clear();
      this.inputMap.clear();

      for (Consumer<IRareEarthRegistry> entry : this.listeners) {
         entry.accept(this);
      }
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.outputs.size());
      ObjectIterator var2 = Object2ObjectMaps.fastIterable(this.outputs).iterator();

      while (var2.hasNext()) {
         Entry<ResourceLocation, ItemStack> entry = (Entry<ResourceLocation, ItemStack>)var2.next();
         buffer.writeResourceLocation((ResourceLocation)entry.getKey());
         buffer.writeItem((ItemStack)entry.getValue());
      }

      buffer.writeVarInt(this.inputMap.size());
      var2 = this.inputMap.values().iterator();

      while (var2.hasNext()) {
         IRareEarthRegistry.RareEntry entry = (IRareEarthRegistry.RareEntry)var2.next();
         buffer.writeResourceLocation(entry.getID());
         buffer.writeItem(entry.getInput());
         buffer.writeFloat(entry.getValue());
         buffer.writeItem(entry.getOutput());
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Object2ObjectMap<ResourceLocation, ItemStack> outputs = CollectionUtils.<ResourceLocation, ItemStack>createLinkedMap();
      Object2ObjectMap<ItemStack, IRareEarthRegistry.RareEntry> inputs = CollectionUtils.<ItemStack, IRareEarthRegistry.RareEntry>createLinkedMap(
         ItemStackStrategy.INSTANCE
      );
      int count = buffer.readVarInt();

      for (int i = 0; i < count; i++) {
         ResourceLocation location = buffer.readResourceLocation();
         ItemStack output = buffer.readItem();
         outputs.put(location, output);
      }

      count = buffer.readVarInt();

      for (int i = 0; i < count; i++) {
         ResourceLocation location = buffer.readResourceLocation();
         ItemStack input = buffer.readItem();
         float value = buffer.readFloat();
         ItemStack output = buffer.readItem();
         inputs.put(input, new IRareEarthRegistry.RareEntry(location, input, value, output));
      }

      this.outputs = outputs;
      this.inputMap = inputs;
   }

   @Override
   public String getFolder() {
      return "rare_earth";
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> map = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();
      Object2ObjectMap<ResourceLocation, List<IRareEarthRegistry.RareEntry>> mapped = CollectionUtils.<ResourceLocation, List<IRareEarthRegistry.RareEntry>>createLinkedMap();
      ObjectIterator var3 = this.inputMap.values().iterator();

      while (var3.hasNext()) {
         IRareEarthRegistry.RareEntry entry = (IRareEarthRegistry.RareEntry)var3.next();
         ((List)mapped.computeIfAbsent(entry.getID(), T -> CollectionUtils.createList())).add(entry);
      }

      var3 = mapped.object2ObjectEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<ResourceLocation, List<IRareEarthRegistry.RareEntry>> entry = (Entry<ResourceLocation, List<IRareEarthRegistry.RareEntry>>)var3.next();
         JsonObject obj = new JsonObject();
         obj.add("output", IngredientRegistry.INSTANCE.serializeOutputGenerator(new ItemWithNBTGenerator((ItemStack)this.outputs.get(entry.getKey()))));
         JsonArray array = new JsonArray();

         for (IRareEarthRegistry.RareEntry rare : entry.getValue()) {
            JsonObject data = new JsonObject();
            data.addProperty("item", ForgeRegistryWrapper.getItemKey(rare.getInput().getItem()).toString());
            data.addProperty("value", rare.getValue());
            array.add(data);
         }

         obj.add("inputs", array);
         map.put((ResourceLocation)entry.getKey(), obj);
      }

      return map;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      if (IRecipeList.isRemover(data)) {
         IRecipeList.getEntriesToRemove(data, this::remove);
      } else {
         ItemStack output = this.read(GsonHelper.getAsJsonObject(data, "output"));
         if (output.isEmpty()) {
            throw new RuntimeException("Output is Empty");
         } else {
            this.registerOutput(id, output);
            IRecipeList.iterateObject(data.get("inputs"), T -> {
               Item item = CraftingHelper.getItem(GsonHelper.getAsString(T, "item"), true);
               float value = GsonHelper.getAsFloat(T, "value");
               if (value <= 0.0F) {
                  throw new RuntimeException("Value needs to be bigger then 0");
               } else {
                  this.registerInput(id, value, new ItemStack(item));
               }
            });
         }
      }
   }

   private ItemStack read(JsonObject obj) {
      ItemStack[] data = new ItemStack[]{ItemStack.EMPTY};
      IOutputGenerator generator = IngredientRegistry.INSTANCE.readOutputGenerator(obj);
      generator.addItems(T -> data[0] = T);
      return data[0];
   }

   @Override
   public void registerOutput(ResourceLocation id, ItemStack output) {
      if (!output.isEmpty() && id != null) {
         this.outputs.put(id, output.copy());
      }
   }

   @Override
   public void registerInput(ResourceLocation id, float value, ItemStack... inputs) {
      ItemStack output = (ItemStack)this.outputs.get(id);
      if (output == null) {
         IC2.LOGGER.info("No Output found for [{}] ignoring recipes", id);
      } else {
         for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
               this.inputMap.put(stack, new IRareEarthRegistry.RareEntry(id, stack.copy(), value, output.copy()));
            }
         }
      }
   }

   @Override
   public IRareEarthRegistry.RareEntry getOutputFor(ItemStack input) {
      return (IRareEarthRegistry.RareEntry)this.inputMap.get(input);
   }

   @Override
   public ItemStack getOutputFor(ResourceLocation id) {
      return (ItemStack)this.outputs.getOrDefault(id, ItemStack.EMPTY);
   }

   @Override
   public IRareEarthRegistry.RareEntry removeInput(ItemStack input) {
      return (IRareEarthRegistry.RareEntry)this.inputMap.remove(input);
   }

   @Override
   public List<IRareEarthRegistry.RareEntry> getAllRecipes() {
      return new ObjectArrayList(this.inputMap.values());
   }

   @Override
   public void removeOutput(ItemStack output) {
      Iterator<Entry<ResourceLocation, ItemStack>> iter = Object2ObjectMaps.fastIterator(this.outputs);

      while (iter.hasNext()) {
         Entry<ResourceLocation, ItemStack> entry = iter.next();
         if (StackUtil.isStackEqual(output, (ItemStack)entry.getValue())) {
            this.removeInputs((ResourceLocation)entry.getKey());
            iter.remove();
         }
      }
   }

   @Override
   public void remove(ResourceLocation id) {
      this.outputs.remove(id);
      this.removeInputs(id);
   }

   private void removeInputs(ResourceLocation location) {
      Iterator<IRareEarthRegistry.RareEntry> iter = this.inputMap.values().iterator();

      while (iter.hasNext()) {
         if (iter.next().matches(location)) {
            iter.remove();
         }
      }
   }
}
