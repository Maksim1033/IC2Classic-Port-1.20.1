package ic2.jeiplugin.core.recipes.categories.brew;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import java.time.Duration;
import java.util.List;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class RumBrewCategory implements IRecipeCategory<RumBrewCategory.CustomBrew> {
   RecipeType<RumBrewCategory.CustomBrew> id;
   IDrawable background;
   IDrawable icon;

   public RumBrewCategory(IGuiHelper helper, RecipeType<RumBrewCategory.CustomBrew> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(provider));
      this.background = helper.createDrawable(texture, 20, 26, 120, 40);
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public RecipeType<RumBrewCategory.CustomBrew> getRecipeType() {
      return this.id;
   }

   public Component getTitle() {
      return Component.literal("Rum Brewing");
   }

   public void draw(RumBrewCategory.CustomBrew recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      Font font = Minecraft.getInstance().font;
      String s = DurationFormatUtils.formatDuration(Duration.ofSeconds((long)(recipe.calculateTimeRequired() / 20)).toMillis(), "HH:mm:ss");
      stack.drawString(font, "Brew Time: " + s, 3, 32, 4210752, false);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, RumBrewCategory.CustomBrew rum, IFocusGroup group) {
      builder.addSlot(RecipeIngredientRole.INPUT, 4, 8).addItemStack(rum.input);
      builder.addSlot(RecipeIngredientRole.CATALYST, 43, 8).addItemStack(rum.brewer);
      builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 8).addItemStack(rum.output);
   }

   public static class CustomBrew {
      ItemStack input;
      ItemStack brewer;
      ItemStack output;
      int amount;

      public CustomBrew(Item input, int amount, Item brewer, ItemStack output) {
         this.amount = amount;
         this.input = new ItemStack(input, amount);
         this.brewer = new ItemStack(brewer);
         this.output = output.copy();
         this.output.setCount(amount);
      }

      public int calculateTimeRequired() {
         return (int)(1200.0 * (double)Math.max(1, this.amount) * Math.pow(0.95, (double)Math.max(0, this.amount - 1)));
      }

      public static List<RumBrewCategory.CustomBrew> create() {
         List<RumBrewCategory.CustomBrew> list = CollectionUtils.<RumBrewCategory.CustomBrew>createList();
         list.add(new RumBrewCategory.CustomBrew(Items.SUGAR_CANE, 1, IC2Blocks.BARREL.asItem(), create(IC2Items.MUG_RUM)));
         list.add(new RumBrewCategory.CustomBrew(Items.SUGAR_CANE, 16, IC2Blocks.BARREL.asItem(), create(IC2Items.MUG_RUM)));
         list.add(new RumBrewCategory.CustomBrew(Items.SUGAR_CANE, 32, IC2Blocks.BARREL.asItem(), create(IC2Items.MUG_RUM)));
         list.add(new RumBrewCategory.CustomBrew(Items.SUGAR_CANE, 1, IC2Blocks.BARREL.asItem(), create(IC2Items.GLASS_RUM)));
         list.add(new RumBrewCategory.CustomBrew(Items.SUGAR_CANE, 16, IC2Blocks.BARREL.asItem(), create(IC2Items.GLASS_RUM)));
         list.add(new RumBrewCategory.CustomBrew(Items.SUGAR_CANE, 32, IC2Blocks.BARREL.asItem(), create(IC2Items.GLASS_RUM)));
         return list;
      }

      private static ItemStack create(Item item) {
         ItemStack stack = new ItemStack(item);
         stack.getOrCreateTag().putByte("progress", (byte)100);
         return stack;
      }
   }
}
