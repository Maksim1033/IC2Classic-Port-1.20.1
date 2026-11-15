package ic2.core.platform.recipes.crafting;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.platform.recipes.mods.IRecipeModifier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public abstract class RecipeIC2Base implements CraftingRecipe {
   protected ResourceLocation id;
   protected ItemStack output;
   protected IRecipeModifier mod;
   public boolean hidden;
   protected boolean consume;
   String group;

   public RecipeIC2Base(ResourceLocation id, ItemStack output, boolean hidden, boolean consume, IRecipeModifier mod) {
      this.id = id;
      this.output = output;
      this.mod = mod;
      this.hidden = hidden;
      this.consume = consume;
      this.group = output.getDescriptionId();
   }

   public RecipeIC2Base setCustomGroup(String group) {
      this.group = group;
      return this;
   }

   public String getGroup() {
      return this.group;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public ItemStack getResultItem() {
      return this.mod != null ? this.mod.applyChanges(this.output, true) : this.output.copy();
   }

   @Override
   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return this.getResultItem();
   }

   @Override
   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }

   @Override
   public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
      return this.assemble(inv);
   }

   public ItemStack assemble(CraftingContainer inv) {
      return this.getResultItem();
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return IC2RecipeSerializer.INSTANCE;
   }

   public boolean canBeDrained(ItemStack stack) {
      return !this.consume && stack.hasCraftingRemainingItem();
   }

   public abstract ResourceLocation getMetaSerializer();

   public abstract void serialize(FriendlyByteBuf var1);

   public abstract JsonObject serialize();

   public abstract NonNullList<Ingredient> getIngredients();

   protected static Tuple<ItemStack, IOutputGenerator> generateOutput(JsonObject output) {
      IOutputGenerator generator = IngredientRegistry.INSTANCE.readOutputGenerator(output);
      Tuple<ItemStack, IOutputGenerator> result = new Tuple(ItemStack.EMPTY, generator);
      generator.addItems(result::setA);
      return result;
   }
}
