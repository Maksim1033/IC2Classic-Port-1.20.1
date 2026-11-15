package ic2.jeiplugin.core.recipes.categories.brew;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.item.food_and_drink.IC2Drink;
import ic2.core.item.food_and_drink.IC2FoodsAndDrinks;
import ic2.core.item.food_and_drink.drinks.Beer;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class BeerBrewCategory implements IRecipeCategory<BeerBrewCategory.Brew> {
   RecipeType<BeerBrewCategory.Brew> id;
   IDrawable background;
   IDrawable icon;

   public BeerBrewCategory(IGuiHelper helper, RecipeType<BeerBrewCategory.Brew> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(provider));
      this.background = helper.createDrawable(texture, 40, 10, 100, 65);
   }

   public RecipeType<BeerBrewCategory.Brew> getRecipeType() {
      return this.id;
   }

   public Component getTitle() {
      return Component.literal("Beer Brewing");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   @OnlyIn(Dist.CLIENT)
   public void draw(BeerBrewCategory.Brew recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      Font font = Minecraft.getInstance().font;
      long time = 0L;

      for (int i = recipe.getTime(); i >= 1; i--) {
         time += (long)(24000.0 * Math.pow(3.0, (double)(i - 1)));
      }

      String s = DurationFormatUtils.formatDuration(Duration.ofSeconds(time / 20L).toMillis(), "HH:mm:ss");
      stack.drawString(font, s, 75 - font.width(s) / 2, 8, 4210752, false);
      s = " <= Ratios";
      stack.drawString(font, s, 23, 50, 4210752, false);
   }

   public void setRecipe(IRecipeLayoutBuilder layout, BeerBrewCategory.Brew recipe, IFocusGroup focus) {
      ItemStack stack = new ItemStack(Items.CLOCK, recipe.getTime() + 1);
      StackUtil.addTooltip(stack, "Brew Time Stage (Accumulated Brew time)");
      layout.addSlot(RecipeIngredientRole.INPUT, 25, 25).addItemStacks(recipe.getInput());
      layout.addSlot(RecipeIngredientRole.INPUT, 5, 5).addItemStack(recipe.getWheat());
      layout.addSlot(RecipeIngredientRole.INPUT, 5, 25).addItemStack(recipe.getHops());
      layout.addSlot(RecipeIngredientRole.INPUT, 5, 45).addItemStack(recipe.getWater());
      layout.addSlot(RecipeIngredientRole.INPUT, 25, 5).addItemStack(stack);
      layout.addSlot(RecipeIngredientRole.OUTPUT, 76, 25).addItemStacks(recipe.getOutput());
   }

   public static class Brew implements Comparable<BeerBrewCategory.Brew> {
      int wheat;
      int hops;
      int water;
      int time;
      List<ItemStack> input;
      List<ItemStack> output;

      public Brew(int wheat, int hops, int water, int time, List<ItemStack> input, List<ItemStack> output) {
         this.wheat = wheat;
         this.hops = hops;
         this.water = water;
         this.time = time;
         this.input = input;
         this.output = output;
      }

      public boolean hasHops() {
         return this.hops > 0;
      }

      public boolean hasWheat() {
         return this.wheat > 0;
      }

      public boolean hasWater() {
         return this.water > 0;
      }

      public ItemStack getHops() {
         return new ItemStack(IC2Items.HOPS, this.hops);
      }

      public ItemStack getWheat() {
         return new ItemStack(Items.WHEAT, this.wheat);
      }

      public ItemStack getWater() {
         return new ItemStack(IC2Items.CELL_WATER, this.water);
      }

      public int getTime() {
         return this.time;
      }

      public List<ItemStack> getInput() {
         return this.input;
      }

      public List<ItemStack> getOutput() {
         return this.output;
      }

      public int compareTo(BeerBrewCategory.Brew o) {
         int result = Integer.compare(this.wheat, o.wheat);
         if (result != 0) {
            return result;
         } else {
            result = Integer.compare(this.hops, o.hops);
            if (result != 0) {
               return result;
            } else {
               result = Integer.compare(this.water, o.water);
               return result != 0 ? result : Long.compare((long)this.time, (long)o.time);
            }
         }
      }

      public static List<BeerBrewCategory.Brew> createBrew() {
         IntSet duplicationCheck = new IntOpenHashSet();
         List<BeerBrewCategory.Brew> result = CollectionUtils.<BeerBrewCategory.Brew>createList();

         for (int time = 0; time < 6; time++) {
            for (int water = 1; water <= 32; water++) {
               for (int hops = 0; hops < 64; hops++) {
                  for (int wheat = 0; wheat < 64; wheat++) {
                     MutableInt quality = new MutableInt(time);
                     MutableInt alc = new MutableInt();
                     MutableInt solid = new MutableInt();
                     createAlcoholLevel(hops, wheat, alc, quality);
                     createSolidLevel(hops, wheat, water * 1000, solid, quality);
                     if (duplicationCheck.add(Beer.createData(quality.getValue(), solid.getValue(), alc.getValue()))) {
                        List<ItemStack> input = CollectionUtils.<ItemStack>createList();
                        List<ItemStack> output = CollectionUtils.<ItemStack>createList();

                        for (IC2Drink drink : IC2FoodsAndDrinks.CONTAINERS) {
                           ItemStack item = drink.fillWith(IC2FoodsAndDrinks.BEER, 1);
                           if (!item.isEmpty()) {
                              output.add(Beer.createData(item, quality.getValue(), solid.getValue(), alc.getValue()));
                              input.add(new ItemStack(drink));
                           }
                        }

                        result.add(new BeerBrewCategory.Brew(wheat, hops, water, quality.getValue(), input, output));
                     }
                  }
               }
            }
         }

         result.sort(null);
         return result;
      }

      private static void createAlcoholLevel(int hopsAmount, int wheatAmount, MutableInt alc, MutableInt quality) {
         float hopsToWheat = wheatAmount > 0 ? (float)hopsAmount / (float)wheatAmount : 10.0F;
         alc.setValue(0);
         if (hopsAmount <= 0 && wheatAmount <= 0) {
            hopsToWheat = 0.0F;
         }

         if (hopsToWheat >= 5.0F) {
            quality.setValue(5);
         } else if (hopsToWheat >= 4.0F) {
            alc.setValue(6);
         } else if (hopsToWheat >= 3.0F) {
            alc.setValue(5);
         } else if (hopsToWheat >= 2.0F) {
            alc.setValue(4);
         } else if (hopsToWheat > 0.5F) {
            alc.setValue(3);
         } else if (hopsToWheat > 0.33333334F) {
            alc.setValue(2);
         } else if (hopsToWheat > 0.25F) {
            alc.setValue(1);
         }
      }

      private static void createSolidLevel(int hopsAmount, int wheatAmount, int fluidAmount, MutableInt solidRatio, MutableInt quality) {
         float solid = fluidAmount > 0 ? (float)(hopsAmount + wheatAmount) / ((float)fluidAmount / 1000.0F) : 10.0F;
         solidRatio.setValue(0);
         if (solid >= 4.0F) {
            quality.setValue(5);
         } else if (solid >= 2.4F) {
            solidRatio.setValue(6);
         } else if (solid >= 2.0F) {
            solidRatio.setValue(5);
         } else if (solid > 1.0F) {
            solidRatio.setValue(4);
         } else if (solid == 1.0F) {
            solidRatio.setValue(3);
         } else if (solid > 0.5F) {
            solidRatio.setValue(2);
         } else if (solid > 0.4166667F) {
            solidRatio.setValue(1);
         }
      }
   }
}
