package ic2.core.block.machines.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.registries.IListenableRegistry;
import ic2.core.block.machines.recipes.misc.EnrichRecipe;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

public class EnricherRecipeList implements IRecipeList, IListenableRegistry<EnricherRecipeList> {
   List<Consumer<EnricherRecipeList>> listeners = ObjectLists.synchronize(CollectionUtils.createList());
   Map<ResourceLocation, EnrichRecipe> recipes = CollectionUtils.<ResourceLocation, EnrichRecipe>createLinkedMap();
   Set<Item> validInputs = CollectionUtils.<Item>createSet();

   public EnricherRecipeList(Consumer<EnricherRecipeList> listener) {
      if (listener != null) {
         this.listeners.add(listener);
      }
   }

   public void reload() {
      this.recipes.clear();
      int i = 0;

      for (int m = this.listeners.size(); i < m; i++) {
         this.listeners.get(i).accept(this);
      }
   }

   public void registerRecipe(EnrichRecipe recipe) {
      if (this.recipes.containsKey(recipe.getId())) {
         this.recipes.get(recipe.getId()).addInputs(recipe.getInputs());
      } else {
         this.recipes.put(recipe.getId(), recipe);
      }

      this.validInputs.addAll(recipe.getInputs());
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.recipes.size());

      for (EnrichRecipe recipe : this.recipes.values()) {
         recipe.write(buffer);
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Map<ResourceLocation, EnrichRecipe> newRecipes = CollectionUtils.<ResourceLocation, EnrichRecipe>createLinkedMap();
      Set<Item> newValidInputs = CollectionUtils.<Item>createSet();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         EnrichRecipe recipe = new EnrichRecipe(buffer);
         newRecipes.put(recipe.getId(), recipe);
         newValidInputs.addAll(recipe.getInputs());
      }

      this.recipes = newRecipes;
      this.validInputs = newValidInputs;
   }

   @Override
   public String getFolder() {
      return "enricher";
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> map = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();

      for (EnrichRecipe recipe : this.recipes.values()) {
         JsonObject obj = new JsonObject();
         JsonArray inputs = new JsonArray();

         for (Item i : recipe.getInputs()) {
            inputs.add(ForgeRegistryWrapper.getItemKey(i).toString());
         }

         obj.add("inputs", inputs);
         JsonArray array = new JsonArray();
         ObjectIterator var11 = recipe.getFuelValues().object2IntEntrySet().iterator();

         while (var11.hasNext()) {
            Entry<IInput> entry = (Entry<IInput>)var11.next();
            JsonObject value = new JsonObject();
            value.add("input", IngredientRegistry.INSTANCE.serializeInput((IInput)entry.getKey()));
            value.addProperty("value", entry.getIntValue());
            array.add(value);
         }

         obj.add("fuels", array);
         obj.add("output", IInput.writeItemStack(recipe.getOutput(), true));
         obj.addProperty("points", recipe.getRequiredPoints());
         obj.addProperty("color", recipe.getColor());
         obj.addProperty("energy_usage", recipe.getEnergyCost());
         map.put(recipe.getId(), obj);
      }

      return map;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      if (IRecipeList.isRemover(data)) {
         IRecipeList.getEntriesToRemove(data, this::removeRecipe);
      } else {
         ObjectSet<Item> inputs = ObjectLinkedOpenHashSet.of();
         if (data.has("input")) {
            inputs.add(CraftingHelper.getItem(GsonHelper.getAsString(data, "input"), true));
         } else {
            if (!data.has("inputs")) {
               throw new RuntimeException("input is Empty");
            }

            IRecipeList.iterateObject(data.get("inputs"), T -> inputs.add(CraftingHelper.getItem(T.getAsString(), true)));
         }

         Object2IntMap<IInput> fuels = new Object2IntLinkedOpenHashMap();
         IRecipeList.iterateObject(
            data.get("fuels"), T -> fuels.put(IngredientRegistry.INSTANCE.readInput(GsonHelper.getAsJsonObject(T, "input")), GsonHelper.getAsInt(T, "value"))
         );
         ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(data, "output"), true);
         if (output.isEmpty()) {
            throw new RuntimeException("output is Empty");
         } else {
            int points = GsonHelper.getAsInt(data, "points");
            if (points <= 0) {
               throw new RuntimeException("There needs to be at least 1 point to be consumed");
            } else {
               int energy = GsonHelper.getAsInt(data, "energy_usage");
               if (energy <= 0) {
                  throw new RuntimeException("energy consumption has to be at least 1");
               } else {
                  this.registerRecipe(new EnrichRecipe(id, inputs, fuels, output, points, GsonHelper.getAsInt(data, "color"), energy));
               }
            }
         }
      }
   }

   @Override
   public void registerListener(Consumer<EnricherRecipeList> listener) {
      this.listeners.add(listener);
   }

   public List<EnrichRecipe> getRecipes() {
      return new ObjectArrayList(this.recipes.values());
   }

   public void removeRecipe(ResourceLocation location) {
      EnrichRecipe recipe = this.recipes.remove(location);
      if (recipe != null) {
         this.validInputs.clear();

         for (EnrichRecipe r : this.recipes.values()) {
            this.validInputs.addAll(r.getInputs());
         }
      }
   }

   public EnrichRecipe getRecipe(ResourceLocation location) {
      return this.recipes.get(location);
   }

   public boolean hasRecipeForInput(ItemStack input) {
      return this.validInputs.contains(input.getItem());
   }

   public EnrichRecipe getRecipeFromFuel(ItemStack fuel) {
      for (EnrichRecipe recipe : this.recipes.values()) {
         if (recipe.getFuelValue(fuel) > 0) {
            return recipe;
         }
      }

      return null;
   }
}
