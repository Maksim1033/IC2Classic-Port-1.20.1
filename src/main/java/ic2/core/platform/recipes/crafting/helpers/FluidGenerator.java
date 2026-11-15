package ic2.core.platform.recipes.crafting.helpers;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class FluidGenerator implements IOutputGenerator {
   Fluid fluid;
   int size;
   boolean cell;

   public FluidGenerator(JsonObject obj) {
      this(
         ForgeRegistryWrapper.getFluidValue(ResourceLocation.tryParse(obj.get("fluid").getAsString())),
         obj.get("size").getAsInt(),
         "cell".equalsIgnoreCase(obj.get("container").getAsString())
      );
   }

   public FluidGenerator(Fluid fluid, int size, boolean cell) {
      this.fluid = fluid;
      this.size = size;
      this.cell = cell;
   }

   @Override
   public void addItems(Consumer<ItemStack> output) {
      output.accept(new ItemStack(this.fluid.getBucket(), this.size));
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("fluid", ForgeRegistryWrapper.getFluidKey(this.fluid).toString());
      obj.addProperty("size", this.size);
      obj.addProperty("container", this.cell ? "cell" : "bucket");
      return obj;
   }
}
