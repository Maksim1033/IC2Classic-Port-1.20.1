package ic2.core.block.machines.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.registries.IRecipeFilter;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class MachineFilter implements IRecipeFilter, IRecipeList {
   Set<Item> simple = CollectionUtils.<Item>createLinkedSet();
   List<IInput> filters = CollectionUtils.<IInput>createList();

   @Override
   public void add(ItemLike... items) {
      for (ItemLike prov : items) {
         this.simple.add(prov.asItem());
      }
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      JsonArray array = new JsonArray();

      for (Item item : this.simple) {
         array.add(ForgeRegistryWrapper.getItemKey(item).toString());
      }

      obj.add("simple", array);
      array = new JsonArray();

      for (IInput input : this.filters) {
         array.add(IngredientRegistry.INSTANCE.serializeInput(input));
      }

      obj.add("special", array);
      return obj;
   }

   @Override
   public void read(JsonObject obj) {
      boolean remove = GsonHelper.getAsBoolean(obj, "remove", false);
      IRecipeList.iteratePrimitive(obj.get("simple"), T -> {
         Item item = CraftingHelper.getItem(T.getAsString(), true);
         if (remove) {
            this.simple.remove(item);
         } else {
            this.simple.add(item);
         }
      });
      IRecipeList.iterateObject(obj.get("special"), T -> {
         IInput input = IngredientRegistry.INSTANCE.readInput(T);
         if (remove) {
            this.remove(input.getComponents().get(0), 2);
         } else {
            this.filters.add(input);
         }
      });
   }

   @Override
   public void add(IInput... inputs) {
      this.filters.addAll(ObjectArrayList.wrap(inputs));
   }

   @Override
   public void remove(ItemStack stack, int flags) {
      if ((flags & 1) != 0) {
         this.simple.remove(stack.getItem());
      }

      if ((flags & 2) != 0) {
         int i = 0;

         for (int m = this.filters.size(); i < m; i++) {
            IInput input = this.filters.get(i);
            if (input.matches(stack)) {
               this.filters.remove(i--);
               m--;
            }
         }
      }
   }

   @Override
   public boolean contains(ItemStack stack) {
      if (this.simple.contains(stack.getItem())) {
         return true;
      } else {
         int i = 0;

         for (int m = this.filters.size(); i < m; i++) {
            if (this.filters.get(i).matches(stack)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void clear() {
      this.simple.clear();
      this.filters.clear();
   }

   @Override
   public List<ItemStack> getFilteredItems() {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();

      for (Item item : this.simple) {
         list.add(new ItemStack(item));
      }

      int i = 0;

      for (int m = this.filters.size(); i < m; i++) {
         list.addAll(this.filters.get(i).getComponents());
      }

      return list;
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.simple.size());

      for (Item item : this.simple) {
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, item);
      }

      buffer.writeVarInt(this.filters.size());
      int i = 0;

      for (int m = this.filters.size(); i < m; i++) {
         RecipeRegistry.INGREDIENTS.writeInput(this.filters.get(i), buffer);
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Set<Item> simpleSync = CollectionUtils.<Item>createLinkedSet();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         Item item = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
         if (item != null && item != Items.AIR) {
            simpleSync.add(item);
         }
      }

      this.simple = simpleSync;
      List<IInput> filtersSync = CollectionUtils.<IInput>createList();
      size = buffer.readVarInt();

      for (int ix = 0; ix < size; ix++) {
         IInput input = RecipeRegistry.INGREDIENTS.readInput(buffer);
         if (input != null) {
            filtersSync.add(input);
         }
      }

      this.filters = filtersSync;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
   }
}
