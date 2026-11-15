package ic2.api.recipes.registries;

import ic2.api.items.IRepairable;
import ic2.api.recipes.ingridients.inputs.IInput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IAdvancedCraftingManager extends IListenableRegistry<IAdvancedCraftingManager> {
   default void addShapedIC2Recipe(String location, ItemStack output, Object... args) {
      this.addShapedRecipe(new ResourceLocation("ic2", location), output, args);
   }

   default void addShapelessIC2Recipe(String location, ItemStack output, Object... args) {
      this.addShapelessRecipe(new ResourceLocation("ic2", location), output, args);
   }

   default void addRepairIC2Recipe(String location, IRepairable repair, IInput repairMaterial, int effect, Object... args) {
      this.addRepairRecipe(new ResourceLocation("ic2", location), repair, repairMaterial, effect, args);
   }

   void addShapedRecipe(ResourceLocation var1, ItemStack var2, Object... var3);

   void addShapelessRecipe(ResourceLocation var1, ItemStack var2, Object... var3);

   void addRepairRecipe(ResourceLocation var1, IRepairable var2, IInput var3, int var4, Object... var5);

   void removeCraftingRecipe(ResourceLocation var1);

   default void addIC2SmeltingRecipe(String location, ItemStack output, Object input, IAdvancedCraftingManager.SmeltingType... types) {
      this.addIC2SmeltingRecipe(location, output, input, 0.0F, types);
   }

   default void addIC2SmeltingRecipe(String location, ItemStack output, Object input, float xp, IAdvancedCraftingManager.SmeltingType... types) {
      this.addIC2SmeltingRecipe(location, output, input, xp, 1.0F, types);
   }

   default void addIC2SmeltingRecipe(String location, ItemStack output, Object input, float xp, float extraTime, IAdvancedCraftingManager.SmeltingType... types) {
      this.addSmeltingRecipe(new ResourceLocation("ic2", location), output, input, xp, extraTime, types);
   }

   default void addSmeltingRecipe(ResourceLocation location, ItemStack output, Object input, IAdvancedCraftingManager.SmeltingType... types) {
      this.addSmeltingRecipe(location, output, input, 0.0F, types);
   }

   default void addSmeltingRecipe(ResourceLocation location, ItemStack output, Object input, float xp, IAdvancedCraftingManager.SmeltingType... types) {
      this.addSmeltingRecipe(location, output, input, xp, 1.0F, types);
   }

   void addSmeltingRecipe(ResourceLocation var1, ItemStack var2, Object var3, float var4, float var5, IAdvancedCraftingManager.SmeltingType... var6);

   void removeSmeltingRecipe(ResourceLocation var1, IAdvancedCraftingManager.SmeltingType... var2);

   void addStoneCutterRecipe(ResourceLocation var1, ItemStack var2, Object var3);

   default void addStoneCutterIC2Recipe(String location, ItemStack output, Object input) {
      this.addStoneCutterRecipe(new ResourceLocation("ic2", location), output, input);
   }

   void removeStoneCutterRecipe(ResourceLocation var1);

   void addSmithingRecipe(ResourceLocation var1, Object var2, Object var3, ItemStack var4);

   default void addSmithingIC2Recipe(String location, Object main, Object secondary, ItemStack output) {
      this.addSmithingRecipe(new ResourceLocation("ic2", location), main, secondary, output);
   }

   void removeSmithingRecipe(ResourceLocation var1);

   public static enum SmeltingType {
      FURNACE(200, "furnace"),
      SMOKER(100, "smelter"),
      BLASTFURNACE(100, "blast"),
      CAMPFIRE(100, "camp");

      final String suffix;
      final int baseTime;

      private SmeltingType(int baseTime, String suffix) {
         this.baseTime = baseTime;
         this.suffix = suffix;
      }

      public int getBaseTime() {
         return this.baseTime;
      }

      public ResourceLocation convert(ResourceLocation location) {
         return new ResourceLocation(location.getNamespace(), location.getPath() + "_" + this.suffix);
      }
   }
}
