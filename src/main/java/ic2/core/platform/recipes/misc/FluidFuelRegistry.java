package ic2.core.platform.recipes.misc;

import com.google.gson.JsonObject;
import ic2.api.recipes.registries.IFluidFuelRegistry;
import ic2.core.block.machines.recipes.IRecipeList;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidFuelRegistry extends BaseRegistry<IFluidFuelRegistry> implements IFluidFuelRegistry, IRecipeList {
   Map<Fluid, IFluidFuelRegistry.FuelEntry> fuels = CollectionUtils.<Fluid, IFluidFuelRegistry.FuelEntry>createLinkedMap();

   public FluidFuelRegistry(Consumer<IFluidFuelRegistry> listener) {
      this.registerListener(listener);
   }

   @Override
   protected void reloadInternals() {
      this.fuels.clear();
   }

   @Override
   public void writeRecipes(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.fuels.size());

      for (IFluidFuelRegistry.FuelEntry entry : this.fuels.values()) {
         entry.write(buffer);
      }
   }

   @Override
   public void readRecipes(FriendlyByteBuf buffer) {
      Map<Fluid, IFluidFuelRegistry.FuelEntry> newMap = CollectionUtils.<Fluid, IFluidFuelRegistry.FuelEntry>createLinkedMap();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         IFluidFuelRegistry.FuelEntry entry = new IFluidFuelRegistry.FuelEntry(buffer);
         if (entry.getFluid() != Fluids.EMPTY) {
            newMap.put(entry.getFluid(), entry);
         }
      }

      this.fuels = newMap;
   }

   @Override
   public String getFolder() {
      return "liquid_fuel";
   }

   @Override
   public Map<ResourceLocation, JsonObject> writeRecipes() {
      Map<ResourceLocation, JsonObject> map = CollectionUtils.<ResourceLocation, JsonObject>createLinkedMap();

      for (IFluidFuelRegistry.FuelEntry entry : this.fuels.values()) {
         JsonObject obj = new JsonObject();
         obj.addProperty("fluid", ForgeRegistryWrapper.getFluidKey(entry.getFluid()).toString());
         obj.addProperty("burn_time", entry.getTicksPerBucket());
         obj.addProperty("production", entry.getEuPerTick());
         map.put(new ResourceLocation("ic2", ForgeRegistryWrapper.getFluidKey(entry.getFluid()).getPath()), obj);
      }

      return map;
   }

   @Override
   public void readRecipe(ResourceLocation id, JsonObject data) {
      Fluid fluid = ForgeRegistryWrapper.getFluidValue(new ResourceLocation(GsonHelper.getAsString(data, "fluid")));
      if (GsonHelper.getAsBoolean(data, "remove", false)) {
         this.removeFuel(fluid);
      } else {
         int burnTime = GsonHelper.getAsInt(data, "burn_time");
         if (burnTime <= 0) {
            throw new RuntimeException("Fuel Burn time isn't allowed to be 0 or less");
         } else {
            int production = GsonHelper.getAsInt(data, "production");
            if (production <= 0) {
               throw new RuntimeException("Fuel Production isn't allowed to be 0 or less");
            } else {
               this.addFuel(fluid, burnTime, production);
            }
         }
      }
   }

   @Override
   public void addFuel(Fluid fluid, int ticksPerBucket, int euPerTick) {
      if (ticksPerBucket >= 1 && euPerTick >= 1 && fluid != Fluids.EMPTY) {
         this.fuels.put(fluid, new IFluidFuelRegistry.FuelEntry(fluid, ticksPerBucket, euPerTick));
      }
   }

   @Override
   public void removeFuel(Fluid fluid) {
      this.fuels.remove(fluid);
   }

   @Override
   public List<IFluidFuelRegistry.FuelEntry> getFuels() {
      return new ObjectArrayList(this.fuels.values());
   }

   @Override
   public IFluidFuelRegistry.FuelEntry getFuel(Fluid fluid) {
      return this.fuels.get(fluid);
   }
}
