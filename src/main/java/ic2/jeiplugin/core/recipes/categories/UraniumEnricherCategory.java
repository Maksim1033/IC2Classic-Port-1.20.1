package ic2.jeiplugin.core.recipes.categories;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.block.machines.recipes.misc.EnrichRecipe;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.ColorUtils;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UraniumEnricherCategory implements IRecipeCategory<EnrichRecipe> {
   RecipeType<EnrichRecipe> id;
   ItemStack stack;
   IDrawable background;
   IDrawable progress;
   IDrawable subProgress;
   IDrawable fuel;
   IDrawable icon;

   public UraniumEnricherCategory(IGuiHelper helper, RecipeType<EnrichRecipe> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
      this.background = helper.createDrawable(texture, 42, 13, 129, 64);
      this.progress = helper.drawableBuilder(texture, 176, 14, 23, 16).buildAnimated(150, StartDirection.LEFT, false);
      this.subProgress = helper.drawableBuilder(texture, 176, 36, 52, 34).buildAnimated(250, StartDirection.LEFT, false);
      this.fuel = helper.drawableBuilder(texture, 176, 75, 4, 33).build();
   }

   public RecipeType<EnrichRecipe> getRecipeType() {
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

   public void draw(EnrichRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      this.progress.draw(stack, 20, 13);
      this.subProgress.draw(stack, 55, 21);
      RenderSystem.setShaderColor(
         ColorUtils.getFloatR(recipe.getColor()),
         ColorUtils.getFloatG(recipe.getColor()),
         ColorUtils.getFloatB(recipe.getColor()),
         ColorUtils.getFloatA(recipe.getColor())
      );
      this.fuel.draw(stack, 49, 3);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      Font font = Minecraft.getInstance().font;
      stack.drawString(font, Component.translatable("gui.ic2.uranium_enricher.per_ingot"), 63, 0, 4210752, false);
      stack.drawString(font, Component.literal(recipe.getRequiredPoints() + ""), 63, 10, 4210752, false);
      ItemStack fuel = recipeSlotsView.findSlotByName("fuel").flatMap(slot1 -> slot1.getDisplayedIngredient(VanillaTypes.ITEM_STACK)).orElse(ItemStack.EMPTY);
      if (!fuel.isEmpty()) {
         stack.drawString(font, Component.translatable("gui.ic2.uranium_enricher.points"), 0, 35, 4210752, false);
         stack.drawString(font, Component.literal(recipe.getFuelValue(fuel) + ""), 0, 45, 4210752, false);
      }
   }

   public void setRecipe(IRecipeLayoutBuilder layout, EnrichRecipe recipe, IFocusGroup focus) {
      ((IRecipeSlotBuilder)layout.addSlot(RecipeIngredientRole.INPUT, 3, 14).addItemStacks(recipe.getFuels())).setSlotName("fuel");
      List<ItemStack> inputs = CollectionUtils.<ItemStack>createList();

      for (Item item : recipe.getInputs()) {
         inputs.add(new ItemStack(item));
      }

      layout.addSlot(RecipeIngredientRole.INPUT, 39, 45).addItemStacks(inputs);
      layout.addSlot(RecipeIngredientRole.OUTPUT, 110, 30).addItemStack(recipe.getOutput());
   }
}
