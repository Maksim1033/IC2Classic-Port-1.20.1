package ic2.jeiplugin.core.recipes.categories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Collections;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutputChance;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Formatters;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
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

public class BasicMachineCategory implements IRecipeCategory<IMachineRecipeList.RecipeEntry> {
   protected RecipeType<IMachineRecipeList.RecipeEntry> id;
   protected ItemStack stack;
   protected IDrawable background;
   protected IDrawable progress;
   protected IDrawable charge;
   protected IDrawable icon;

   public BasicMachineCategory(IGuiHelper helper, RecipeType<IMachineRecipeList.RecipeEntry> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
      this.background = helper.createDrawable(texture, 50, 15, 90, 55);
      this.progress = helper.drawableBuilder(texture, 176, 14, 23, 16).buildAnimated(150, StartDirection.LEFT, false);
      this.charge = helper.drawableBuilder(texture, 176, 0, 13, 14).buildAnimated(500, StartDirection.TOP, true);
   }

   public BasicMachineCategory(
      IGuiHelper helper, RecipeType<IMachineRecipeList.RecipeEntry> id, ItemStack stack, IDrawable background, IDrawable progress, IDrawable charge
   ) {
      this.id = id;
      this.stack = stack;
      this.background = background;
      this.progress = progress;
      this.charge = charge;
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack);
   }

   public RecipeType<IMachineRecipeList.RecipeEntry> getRecipeType() {
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

   public void draw(IMachineRecipeList.RecipeEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
      this.progress.draw(graphics, 29, 19);
      this.charge.draw(graphics, 6, 21);
      if (recipe.getOutput() instanceof IRecipeOutputChance chance) {
         float luck = chance.getChance();
         Component text = Component.literal(Formatters.XP_FORMAT.format((double)(luck * 100.0F)) + "%");
         Font font = Minecraft.getInstance().font;
         graphics.drawString(font, text, (int)(85.0F - (float)font.width(text)), 45, 4210752, false);
      }
   }

   public List<Component> getTooltipStrings(IMachineRecipeList.RecipeEntry recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      if (recipe.getOutput() instanceof IRecipeOutputChance chance) {
         float luck = chance.getChance();
         Font font = Minecraft.getInstance().font;
         float width = (float)font.width(Component.literal(Formatters.XP_FORMAT.format((double)(luck * 100.0F)) + "%"));
         if (mouseX >= (double)(85.0F - width) && mouseX <= 85.0 && mouseY >= 45.0 && mouseY <= (double)(45.0F + 9.0F)) {
            return ObjectArrayList.of(new Component[]{Component.translatable("jei.ic2.recycler.chance")});
         }
      }

      return Collections.emptyList();
   }

   public void setRecipe(IRecipeLayoutBuilder layout, IMachineRecipeList.RecipeEntry recipe, IFocusGroup focus) {
      layout.addSlot(RecipeIngredientRole.INPUT, 6, 2).addItemStacks(recipe.getInputs()[0].getComponents());
      layout.addSlot(RecipeIngredientRole.OUTPUT, 66, 20).addItemStacks(recipe.getOutput().getAllOutputs());
   }

   public static class AlloySmelterCategory extends BasicMachineCategory {
      public AlloySmelterCategory(IGuiHelper helper, RecipeType<IMachineRecipeList.RecipeEntry> location, ResourceLocation texture, ItemLike provider) {
         super(
            helper,
            location,
            new ItemStack(provider),
            helper.createDrawable(texture, 40, 15, 100, 60),
            helper.drawableBuilder(texture, 176, 14, 23, 16).buildAnimated(150, StartDirection.LEFT, false),
            helper.drawableBuilder(texture, 176, 0, 13, 14).buildAnimated(500, StartDirection.TOP, true)
         );
      }

      @Override
      public void draw(IMachineRecipeList.RecipeEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
         this.progress.draw(graphics, 39, 19);
         this.charge.draw(graphics, 16, 21);
      }

      @Override
      public void setRecipe(IRecipeLayoutBuilder layout, IMachineRecipeList.RecipeEntry recipe, IFocusGroup focus) {
         List<ItemStack> lists = CollectionUtils.<ItemStack>createList();

         for (IInput inputs : recipe.getInputs()) {
            lists.addAll(inputs.getComponents());
         }

         layout.addSlot(RecipeIngredientRole.INPUT, 6, 2).addItemStacks(recipe.getInputs()[0].getComponents());
         layout.addSlot(RecipeIngredientRole.INPUT, 26, 2).addItemStacks(recipe.getInputs()[1].getComponents());
         layout.addSlot(RecipeIngredientRole.OUTPUT, 76, 20).addItemStacks(recipe.getOutput().getAllOutputs());
      }
   }
}
