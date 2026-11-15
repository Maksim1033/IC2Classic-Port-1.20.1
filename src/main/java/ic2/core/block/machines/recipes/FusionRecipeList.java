package ic2.core.block.machines.recipes;

import com.google.gson.JsonObject;
import ic2.api.recipes.registries.IFusionRecipeList;
import ic2.core.platform.recipes.misc.BaseRegistry;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class FusionRecipeList extends BaseRegistry<IFusionRecipeList> implements IFusionRecipeList, IRecipeList {
   Map<Item, IFusionRecipeList.FusionFuel> fuels = CollectionUtils.<Item, IFusionRecipeList.FusionFuel>createLinkedMap();

   public FusionRecipeList(Consumer<IFusionRecipeList> listener) {
      this.registerListener(listener);
   }

   @Override
   protected void reloadInternals() {
      this.fuels.clear();
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.fuels.size());

      for (IFusionRecipeList.FusionFuel fuel : this.fuels.values()) {
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, fuel.fuel());
         buffer.writeVarInt(fuel.fuelValue());
         buffer.writeFloat(fuel.productionRate());
         buffer.writeBoolean(fuel.consumed());
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Map<Item, IFusionRecipeList.FusionFuel> newFuels = CollectionUtils.<Item, IFusionRecipeList.FusionFuel>createLinkedMap();
      int i = 0;

      for (int m = buffer.readVarInt(); i < m; i++) {
         Item item = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
         newFuels.put(item, new IFusionRecipeList.FusionFuel(item, buffer.readVarInt(), buffer.readFloat(), buffer.readBoolean()));
      }

      this.fuels = newFuels;
   }

   @Override
   public String getFolder() {
      return "fusion";
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> data = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();

      for (IFusionRecipeList.FusionFuel fuel : this.fuels.values()) {
         JsonObject obj = new JsonObject();
         obj.addProperty("input", ForgeRegistryWrapper.getItemKey(fuel.fuel()).toString());
         obj.addProperty("consumed", fuel.consumed());
         obj.addProperty("fuelValue", fuel.fuelValue());
         obj.addProperty("productionRate", fuel.productionRate());
         data.put(new ResourceLocation("ic2", ForgeRegistryWrapper.getItemKey(fuel.fuel()).getPath()), obj);
      }

      return data;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      Item item = CraftingHelper.getItem(GsonHelper.getAsString(data, "input"), true);
      if (GsonHelper.getAsBoolean(data, "remove", false)) {
         this.removeFuel(item);
      } else {
         int fuelValue = GsonHelper.getAsInt(data, "fuelValue");
         if (fuelValue <= 0) {
            throw new RuntimeException("Fuel Value isn't allowed to be 0 or less");
         } else {
            float productionRate = GsonHelper.getAsFloat(data, "productionRate");
            if (productionRate <= 0.0F) {
               throw new RuntimeException("Fuel ProductionRate isn't allowed to be 0 or less");
            } else {
               this.addFuel(item, fuelValue, productionRate, GsonHelper.getAsBoolean(data, "consumed", false));
            }
         }
      }
   }

   @Override
   public void addFuel(Item fuel, int fuelValue, float productionRate, boolean consumed) {
      this.fuels.put(fuel, new IFusionRecipeList.FusionFuel(fuel, fuelValue, productionRate, consumed));
   }

   @Override
   public IFusionRecipeList.FusionFuel getFuel(Item item) {
      return this.fuels.get(item);
   }

   @Override
   public void removeFuel(Item item) {
      this.fuels.remove(item);
   }

   @Override
   public List<IFusionRecipeList.FusionFuel> getFuels() {
      return new ObjectArrayList(this.fuels.values());
   }
}
