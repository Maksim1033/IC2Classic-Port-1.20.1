package ic2.jeiplugin.core.recipes.categories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.api.recipes.registries.IRareEarthRegistry;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RareEarthCategory implements IRecipeCategory<IRareEarthRegistry.RareEntry> {
   RecipeType<IRareEarthRegistry.RareEntry> id;
   ItemStack stack;
   IDrawable background;
   IDrawable progress;
   IDrawable charge;
   IDrawable icon;

   public RareEarthCategory(IGuiHelper helper, RecipeType<IRareEarthRegistry.RareEntry> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
      this.background = helper.createDrawable(texture, 50, 15, 90, 60);
      this.progress = helper.drawableBuilder(texture, 176, 14, 23, 16).buildAnimated(150, StartDirection.LEFT, false);
      this.charge = helper.drawableBuilder(texture, 176, 0, 13, 14).buildAnimated(500, StartDirection.TOP, true);
   }

   public RecipeType<IRareEarthRegistry.RareEntry> getRecipeType() {
      return this.id;
   }

   public Component getTitle() {
      return this.stack.getHoverName();
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   @OnlyIn(Dist.CLIENT)
   public void draw(IRareEarthRegistry.RareEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      this.progress.draw(stack, 29, 19);
      this.charge.draw(stack, 6, 21);
      float itemCount = 1000.0F / recipe.getValue();
      Font font = Minecraft.getInstance().font;
      stack.drawString(font, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)recipe.getValue()) + " Points", 25, 43, 4210752, false);
      stack.drawString(font, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)itemCount) + " Items", 25, 53, 4210752, false);
   }

   public void setRecipe(IRecipeLayoutBuilder layout, IRareEarthRegistry.RareEntry recipe, IFocusGroup focus) {
      layout.addSlot(RecipeIngredientRole.INPUT, 6, 2).addItemStack(recipe.getInput());
      layout.addSlot(RecipeIngredientRole.OUTPUT, 66, 20).addItemStack(recipe.getOutput());
   }
}
