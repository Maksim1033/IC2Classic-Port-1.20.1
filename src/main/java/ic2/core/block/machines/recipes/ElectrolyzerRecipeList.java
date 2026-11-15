package ic2.core.block.machines.recipes;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.registries.IElectrolyzerRecipeList;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class ElectrolyzerRecipeList implements IElectrolyzerRecipeList, IRecipeList {
   List<Consumer<IElectrolyzerRecipeList>> listeners = ObjectLists.synchronize(CollectionUtils.createList());
   Map<ResourceLocation, IElectrolyzerRecipeList.ElectrolyzerRecipe> allRecipes = CollectionUtils.<ResourceLocation, IElectrolyzerRecipeList.ElectrolyzerRecipe>createMap();
   Map<ItemStack, IElectrolyzerRecipeList.ElectrolyzerRecipe> chargeRecipe = CollectionUtils.<ItemStack, IElectrolyzerRecipeList.ElectrolyzerRecipe>createMap(
      ItemStackStrategy.INSTANCE
   );
   Map<ItemStack, IElectrolyzerRecipeList.ElectrolyzerRecipe> dischargeRecipe = CollectionUtils.<ItemStack, IElectrolyzerRecipeList.ElectrolyzerRecipe>createMap(
      ItemStackStrategy.INSTANCE
   );

   public ElectrolyzerRecipeList(Consumer<IElectrolyzerRecipeList> listener) {
      this.listeners.add(listener);
   }

   public void reload() {
      this.allRecipes.clear();
      this.dischargeRecipe.clear();
      this.chargeRecipe.clear();

      for (Consumer<IElectrolyzerRecipeList> entry : this.listeners) {
         entry.accept(this);
      }
   }

   @Override
   public void registerListener(Consumer<IElectrolyzerRecipeList> listener) {
      this.listeners.add(listener);
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.allRecipes.size());

      for (IElectrolyzerRecipeList.ElectrolyzerRecipe entry : this.allRecipes.values()) {
         entry.serialize(buffer);
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Map<ResourceLocation, IElectrolyzerRecipeList.ElectrolyzerRecipe> recipes = CollectionUtils.<ResourceLocation, IElectrolyzerRecipeList.ElectrolyzerRecipe>createMap();
      Map<ItemStack, IElectrolyzerRecipeList.ElectrolyzerRecipe> tempChargeRecipe = CollectionUtils.<ItemStack, IElectrolyzerRecipeList.ElectrolyzerRecipe>createMap(
         ItemStackStrategy.INSTANCE
      );
      Map<ItemStack, IElectrolyzerRecipeList.ElectrolyzerRecipe> tempDischargeRecipe = CollectionUtils.<ItemStack, IElectrolyzerRecipeList.ElectrolyzerRecipe>createMap(
         ItemStackStrategy.INSTANCE
      );
      int limit = buffer.readVarInt();

      for (int i = 0; i < limit; i++) {
         IElectrolyzerRecipeList.ElectrolyzerRecipe recipe = new IElectrolyzerRecipeList.ElectrolyzerRecipe(buffer);
         recipes.put(recipe.getId(), recipe);
         if (recipe.isChargeRecipe()) {
            tempChargeRecipe.put(recipe.getInput(), recipe);
         }

         if (recipe.isDischargeRecipe()) {
            tempDischargeRecipe.put(recipe.getOutput(), recipe);
         }
      }

      this.allRecipes = recipes;
      this.chargeRecipe = tempChargeRecipe;
      this.dischargeRecipe = tempDischargeRecipe;
   }

   @Override
   public String getFolder() {
      return "electrolyzer";
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> map = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();

      for (IElectrolyzerRecipeList.ElectrolyzerRecipe recipe : this.allRecipes.values()) {
         JsonObject obj = new JsonObject();
         obj.add("input", this.writeItem(recipe.getInput()));
         obj.add("output", this.writeItem(recipe.getOutput()));
         obj.addProperty("energy", recipe.getEnergy());
         obj.addProperty("charging", recipe.isChargeRecipe());
         obj.addProperty("discharging", recipe.isDischargeRecipe());
         map.put(recipe.getId(), obj);
      }

      return map;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      if (IRecipeList.isRemover(data)) {
         IRecipeList.getEntriesToRemove(data, this::removeRecipe);
      } else {
         ItemStack input = this.readItem(data.getAsJsonObject("input"));
         if (input.isEmpty()) {
            throw new RuntimeException("Input is empty");
         } else {
            ItemStack output = this.readItem(data.getAsJsonObject("output"));
            if (output.isEmpty()) {
               throw new RuntimeException("Output is empty");
            } else {
               int flags = (GsonHelper.getAsBoolean(data, "charging", false) ? 1 : 0) | (GsonHelper.getAsBoolean(data, "discharging", false) ? 2 : 0);
               if (flags == 0) {
                  throw new RuntimeException("Recipe at the very least charging/discharging");
               } else {
                  this.addRecipe(id, input, output, flags, GsonHelper.getAsInt(data, "energy"));
               }
            }
         }
      }
   }

   @Override
   public void addRecipe(ResourceLocation id, ItemStack input, ItemStack output, int flags, int energy) {
      if (flags != 0 && energy > 100 && !input.isEmpty() && !output.isEmpty()) {
         IElectrolyzerRecipeList.ElectrolyzerRecipe recipe = new IElectrolyzerRecipeList.ElectrolyzerRecipe(id, input.copy(), output.copy(), flags, energy);
         this.allRecipes.put(id, recipe);
         if (recipe.isChargeRecipe()) {
            this.chargeRecipe.put(input, recipe);
         }

         if (recipe.isDischargeRecipe()) {
            this.dischargeRecipe.put(output, recipe);
         }
      }
   }

   private JsonObject writeItem(ItemStack stack) {
      return IngredientRegistry.INSTANCE
         .serializeOutputGenerator((IOutputGenerator)(stack.hasTag() ? new ItemWithNBTGenerator(stack) : new ItemGenerator(stack.getItem(), stack.getCount())));
   }

   private ItemStack readItem(JsonObject obj) {
      ItemStack[] stack = new ItemStack[]{ItemStack.EMPTY};
      new ItemWithNBTGenerator(obj).addItems(T -> stack[0] = T);
      return stack[0];
   }

   @Override
   public void removeRecipe(ItemStack input, boolean charge) {
      (charge ? this.chargeRecipe : this.dischargeRecipe).remove(input);
   }

   @Override
   public void removeRecipe(ResourceLocation id) {
      IElectrolyzerRecipeList.ElectrolyzerRecipe recipe = this.allRecipes.remove(id);
      if (recipe != null) {
         if (recipe.isChargeRecipe()) {
            this.chargeRecipe.remove(recipe.getInput());
         }

         if (recipe.isDischargeRecipe()) {
            this.dischargeRecipe.remove(recipe.getOutput());
         }
      }
   }

   @Override
   public List<IElectrolyzerRecipeList.ElectrolyzerRecipe> getRecipes() {
      return new ObjectArrayList(this.allRecipes.values());
   }

   @Override
   public IElectrolyzerRecipeList.ElectrolyzerRecipe getRecipe(ItemStack input, boolean charge, boolean compareSize) {
      IElectrolyzerRecipeList.ElectrolyzerRecipe recipe = (charge ? this.chargeRecipe : this.dischargeRecipe).get(input);
      return recipe == null || compareSize && !recipe.matches(input, charge) ? null : recipe;
   }
}
