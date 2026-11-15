package ic2.jeiplugin.core.recipes.categories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.api.recipes.registries.IRefiningRecipeList;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.mutable.MutableInt;

public class RefineryCategory implements IRecipeCategory<IRefiningRecipeList.FluidRecipe> {
   RecipeType<IRefiningRecipeList.FluidRecipe> id;
   ItemStack stack;
   IDrawable background;
   IDrawable progress;
   IDrawable charge;
   IDrawable icon;

   public RefineryCategory(IGuiHelper helper, RecipeType<IRefiningRecipeList.FluidRecipe> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
      this.background = helper.createDrawable(texture, 13, 15, 137, 64);
      this.progress = helper.drawableBuilder(texture, 176, 14, 23, 16).buildAnimated(150, StartDirection.LEFT, false);
      this.charge = helper.drawableBuilder(texture, 176, 0, 13, 14).buildAnimated(500, StartDirection.TOP, true);
   }

   public RecipeType<IRefiningRecipeList.FluidRecipe> getRecipeType() {
      return this.id;
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public Component getTitle() {
      return this.stack.getHoverName();
   }

   public void draw(IRefiningRecipeList.FluidRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      this.progress.draw(stack, 66, 23);
      this.charge.draw(stack, 44, 25);
   }

   private List<List<ItemStack>> getOutputs(IRefiningRecipeList.FluidRecipe recipe) {
      List<List<ItemStack>> lists = CollectionUtils.<List<ItemStack>>createList();

      for (int i = 0; i < 3; i++) {
         lists.add(CollectionUtils.<ItemStack>createList());
      }

      MutableInt index = new MutableInt();
      Object2IntMap<Item> itemMap = new Object2IntOpenHashMap();

      for (ItemStack stack : recipe.getOutput().getAllOutputs()) {
         lists.get(itemMap.computeIfAbsent(stack.getItem(), T -> index.getAndIncrement() % 3)).add(stack);
      }

      return lists;
   }

   public void setRecipe(IRecipeLayoutBuilder layout, IRefiningRecipeList.FluidRecipe recipe, IFocusGroup focus) {
      List<List<ItemStack>> outputs = this.getOutputs(recipe);
      layout.addSlot(RecipeIngredientRole.INPUT, 44, 6).addItemStacks(recipe.getItemInput().getComponents());
      layout.addSlot(RecipeIngredientRole.OUTPUT, 97, 6).addItemStacks(outputs.get(0));
      layout.addSlot(RecipeIngredientRole.OUTPUT, 97, 24).addItemStacks(outputs.get(1));
      layout.addSlot(RecipeIngredientRole.OUTPUT, 97, 42).addItemStacks(outputs.get(2));
      if (!recipe.getFirstTank().isEmpty()) {
         ((IRecipeSlotBuilder)layout.addSlot(RecipeIngredientRole.INPUT, 3, 3).addIngredient(ForgeTypes.FLUID_STACK, recipe.getFirstTank()))
            .setFluidRenderer(1L, false, 16, 58);
      }

      if (!recipe.getSecondTank().isEmpty()) {
         ((IRecipeSlotBuilder)layout.addSlot(RecipeIngredientRole.INPUT, 23, 3).addIngredient(ForgeTypes.FLUID_STACK, recipe.getSecondTank()))
            .setFluidRenderer(1L, false, 16, 58);
      }

      ((IRecipeSlotBuilder)layout.addSlot(RecipeIngredientRole.OUTPUT, 118, 3).addIngredients(ForgeTypes.FLUID_STACK, recipe.getOutput().getAllFluidOutputs()))
         .setFluidRenderer(1L, false, 16, 58);
   }
}
