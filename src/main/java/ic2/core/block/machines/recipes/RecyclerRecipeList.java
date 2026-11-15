package ic2.core.block.machines.recipes;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.ItemStackInput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.recipes.registries.IRecipeFilter;
import ic2.api.recipes.registries.IRecyclerRecipeList;
import ic2.core.block.machines.recipes.misc.ScrapOutput;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags.Items;

public class RecyclerRecipeList extends MachineRecipeList implements IRecyclerRecipeList {
   Map<Item, List<IMachineRecipeList.RecipeEntry>> mappedRecipes = CollectionUtils.<Item, List<IMachineRecipeList.RecipeEntry>>createMap();
   MachineFilter filter = new MachineFilter();
   ScrapOutput scrap;
   ScrapOutput scrapMetal;

   public RecyclerRecipeList(Consumer<IMachineRecipeList> reloader) {
      super("recycler", reloader);
   }

   @Override
   public IRecipeFilter getBlackList() {
      return this.filter;
   }

   @Override
   public void reload() {
      this.filter.clear();
      this.mappedRecipes.clear();
      super.reload();
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      super.writeRecipes(buffer);
      this.filter.writeRecipes(buffer);
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      super.readRecipes(buffer);
      this.filter.readRecipes(buffer);
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> map = super.writeRecipes();
      JsonObject obj = this.filter.serialize();
      obj.addProperty("filter", true);
      map.put(new ResourceLocation("ic2:blacklist"), obj);
      return map;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) throws Exception {
      if (GsonHelper.getAsBoolean(data, "filter", false)) {
         this.filter.read(data);
      } else {
         super.readRecipe(id, data);
      }
   }

   @Override
   protected void mapRecipes() {
      Map<Item, Set<IMachineRecipeList.RecipeEntry>> mapped = CollectionUtils.<Item, Set<IMachineRecipeList.RecipeEntry>>createMap();

      for (IMachineRecipeList.RecipeEntry entry : this.recipes.values()) {
         for (ItemStack stack : this.toStackList(entry.getInputs())) {
            mapped.computeIfAbsent(stack.getItem(), T -> CollectionUtils.createSet()).add(entry);
         }
      }

      mapped.forEach((K, V) -> this.mappedRecipes.put(K, new ObjectArrayList(V)));
   }

   public IMachineRecipeList.RecipeEntry getInternalRecipe(ItemStack stack, boolean hasStackSize) {
      if (!this.mapped) {
         this.mapRecipes();
         this.mapped = true;
      }

      List<IMachineRecipeList.RecipeEntry> list = this.mappedRecipes.get(stack.getItem());
      if (list == null) {
         return null;
      } else {
         for (IMachineRecipeList.RecipeEntry entry : list) {
            if (entry.getInputs()[0].matches(stack) && (!hasStackSize || entry.getInputs()[0].getInputSize() <= stack.getCount())) {
               return entry;
            }
         }

         return null;
      }
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(ItemStack stack, boolean hasStackSize) {
      if (!this.filter.contains(stack) && !stack.isEmpty()) {
         IMachineRecipeList.RecipeEntry entry = this.getInternalRecipe(stack, hasStackSize);
         if (entry == null && this.getInternalRecipe(stack, false) == null) {
            this.loadTemps();
            if (this.isMetal(stack)) {
               entry = new IMachineRecipeList.RecipeEntry(new ResourceLocation("scrap_metal"), this.scrapMetal, new ItemStackInput(stack, 1));
            } else {
               entry = new IMachineRecipeList.RecipeEntry(new ResourceLocation("scrap"), this.scrap, new ItemStackInput(stack, 1));
            }
         }

         return entry;
      } else {
         return null;
      }
   }

   protected boolean isMetal(ItemStack item) {
      return item.is(Items.INGOTS) && !item.is(Items.INGOTS_BRICK) && !item.is(Items.INGOTS_NETHER_BRICK);
   }

   protected void loadTemps() {
      if (this.scrap == null || this.scrapMetal == null) {
         this.scrap = new ScrapOutput(new ItemStack(IC2Items.SCRAP), 4);
         this.scrapMetal = new ScrapOutput(new ItemStack(IC2Items.SCRAP_METAL), 8);
      }
   }
}
