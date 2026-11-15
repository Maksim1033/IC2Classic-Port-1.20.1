package ic2.jeiplugin.core.recipes.categories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.api.recipes.registries.IFluidFuelRegistry;
import ic2.core.utils.helpers.Formatters;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;

public class LiquidFuelGeneratorCategory implements IRecipeCategory<IFluidFuelRegistry.FuelEntry> {
   RecipeType<IFluidFuelRegistry.FuelEntry> id;
   ItemStack stack;
   IDrawable background;
   IDrawable progress;
   IDrawable charge;
   IDrawable icon;

   public LiquidFuelGeneratorCategory(IGuiHelper helper, RecipeType<IFluidFuelRegistry.FuelEntry> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
      this.background = helper.createDrawable(texture, 45, 12, 77, 64);
      this.progress = helper.drawableBuilder(texture, 176, 14, 24, 16).buildAnimated(150, StartDirection.LEFT, false);
      this.charge = helper.drawableBuilder(texture, 176, 0, 14, 14).buildAnimated(500, StartDirection.TOP, true);
   }

   public RecipeType<IFluidFuelRegistry.FuelEntry> getRecipeType() {
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

   public void draw(IFluidFuelRegistry.FuelEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      this.progress.draw(stack, 47, 22);
      this.charge.draw(stack, 25, 24);
      Font font = Minecraft.getInstance().font;
      stack.drawString(font, Component.translatable("gui.ic2.liquid_gen.burntime"), 25, 2, 4210752, false);
      stack.drawString(font, Component.literal(Formatters.EU_FORMAT.format((long)recipe.getTicksPerBucket()) + "t"), 25, 12, 4210752, false);
      stack.drawString(font, Component.translatable("gui.ic2.liquid_gen.eu_ticks"), 25, 42, 4210752, false);
      stack.drawString(font, Component.literal(Formatters.EU_FORMAT.format((long)recipe.getEuPerTick()) + "EU"), 25, 52, 4210752, false);
   }

   public void setRecipe(IRecipeLayoutBuilder layout, IFluidFuelRegistry.FuelEntry recipe, IFocusGroup focus) {
      ((IRecipeSlotBuilder)layout.addSlot(RecipeIngredientRole.INPUT, 3, 3).addIngredient(ForgeTypes.FLUID_STACK, new FluidStack(recipe.getFluid(), 1000)))
         .setFluidRenderer(1L, false, 16, 58);
   }
}
