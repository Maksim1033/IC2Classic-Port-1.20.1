package ic2.api.recipes.registries;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IRareEarthRegistry extends IListenableRegistry<IRareEarthRegistry> {
   void registerOutput(ResourceLocation var1, ItemStack var2);

   void registerInput(ResourceLocation var1, float var2, ItemStack... var3);

   IRareEarthRegistry.RareEntry getOutputFor(ItemStack var1);

   ItemStack getOutputFor(ResourceLocation var1);

   IRareEarthRegistry.RareEntry removeInput(ItemStack var1);

   void removeOutput(ItemStack var1);

   void remove(ResourceLocation var1);

   List<IRareEarthRegistry.RareEntry> getAllRecipes();

   public static class RareEntry {
      ResourceLocation id;
      ItemStack input;
      float value;
      ItemStack output;

      public RareEntry(ResourceLocation id, ItemStack input, float value, ItemStack output) {
         this.id = id;
         this.input = input;
         this.value = value;
         this.output = output;
      }

      public ResourceLocation getID() {
         return this.id;
      }

      public ItemStack getInput() {
         return this.input;
      }

      public ItemStack getOutput() {
         return this.output;
      }

      public float getValue() {
         return this.value;
      }

      public boolean matches(ResourceLocation location) {
         return this.id.equals(location);
      }
   }
}
