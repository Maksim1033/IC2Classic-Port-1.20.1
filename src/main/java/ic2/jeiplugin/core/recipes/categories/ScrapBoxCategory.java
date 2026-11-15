package ic2.jeiplugin.core.recipes.categories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.api.recipes.registries.IScrapBoxRegistry;
import ic2.core.platform.registries.IC2Items;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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

public class ScrapBoxCategory implements IRecipeCategory<IScrapBoxRegistry.IDrop> {
   RecipeType<IScrapBoxRegistry.IDrop> id;
   ItemStack stack;
   IDrawable background;
   IDrawable icon;

   public ScrapBoxCategory(IGuiHelper helper, RecipeType<IScrapBoxRegistry.IDrop> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
      this.background = helper.createDrawable(texture, 50, 25, 90, 45);
   }

   public RecipeType<IScrapBoxRegistry.IDrop> getRecipeType() {
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
   public void draw(IScrapBoxRegistry.IDrop recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      String s = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)recipe.getChance()) + "%";
      Font font = Minecraft.getInstance().font;
      stack.drawString(font, s, (int)(75.0F - (float)font.width(s) / 2.0F), 35, 4210752, false);
   }

   public void setRecipe(IRecipeLayoutBuilder layout, IScrapBoxRegistry.IDrop recipe, IFocusGroup focus) {
      layout.addSlot(RecipeIngredientRole.INPUT, 6, 10).addItemStack(new ItemStack(IC2Items.SCRAPBOX));
      layout.addSlot(RecipeIngredientRole.OUTPUT, 66, 10).addItemStack(recipe.getDrop());
   }
}
