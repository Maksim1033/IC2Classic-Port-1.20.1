package ic2.jeiplugin.core.recipes.categories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.api.recipes.registries.IElectrolyzerRecipeList;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ElectrolyzerCategory implements IRecipeCategory<IElectrolyzerRecipeList.ElectrolyzerRecipe> {
   RecipeType<IElectrolyzerRecipeList.ElectrolyzerRecipe> id;
   ItemStack stack;
   IDrawable background;
   IDrawable charge;
   IDrawable icon;

   public ElectrolyzerCategory(IGuiHelper helper, RecipeType<IElectrolyzerRecipeList.ElectrolyzerRecipe> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.background = helper.drawableBuilder(texture, 45, 30, 90, 45).addPadding(0, 0, 20, 20).build();
      this.charge = helper.drawableBuilder(texture, 176, 14, 24, 16).buildAnimated(150, StartDirection.LEFT, false);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
   }

   public RecipeType<IElectrolyzerRecipeList.ElectrolyzerRecipe> getRecipeType() {
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

   public void draw(IElectrolyzerRecipeList.ElectrolyzerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics matrixStack, double mouseX, double mouseY) {
      this.charge.draw(matrixStack, 54, 4);
      Font font = Minecraft.getInstance().font;
      boolean dual = recipe.isChargeRecipe() && recipe.isDischargeRecipe();
      Component comp = Component.translatable("gui.ic2.electrolyzer." + (dual ? "dual" : (recipe.isChargeRecipe() ? "charging" : "discharging")));
      matrixStack.drawString(font, comp, -(font.width(comp.getVisualOrderText()) / 2) + 65, 25, 4210752, false);
      comp = Component.translatable("gui.ic2.electrolyzer.energy", new Object[]{recipe.getEnergy()});
      matrixStack.drawString(font, comp, -(font.width(comp.getVisualOrderText()) / 2) + 65, 36, 4210752, false);
   }

   public void setRecipe(IRecipeLayoutBuilder layout, IElectrolyzerRecipeList.ElectrolyzerRecipe recipe, IFocusGroup focus) {
      layout.addSlot(RecipeIngredientRole.INPUT, 29, 5).addItemStack(recipe.getInput());
      layout.addSlot(RecipeIngredientRole.OUTPUT, 87, 5).addItemStack(recipe.getOutput());
   }
}
