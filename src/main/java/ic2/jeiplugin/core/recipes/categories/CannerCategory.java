package ic2.jeiplugin.core.recipes.categories;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.api.recipes.registries.ICannerRecipeRegistry;
import ic2.core.platform.recipes.misc.GlobalRecipes;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map.Entry;
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
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class CannerCategory implements IRecipeCategory<CannerCategory.CannerRecipe> {
   RecipeType<CannerCategory.CannerRecipe> id;
   ItemStack stack;
   IDrawable background;
   IDrawable progress;
   IDrawable icon;

   public CannerCategory(IGuiHelper helper, RecipeType<CannerCategory.CannerRecipe> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
      this.background = helper.createDrawable(texture, 50, 15, 90, 63);
      this.progress = helper.drawableBuilder(texture, 176, 14, 23, 16).buildAnimated(150, StartDirection.LEFT, false);
   }

   public RecipeType<CannerCategory.CannerRecipe> getRecipeType() {
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
   public void draw(CannerCategory.CannerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      this.progress.draw(stack, 24, 20);
      Font font = Minecraft.getInstance().font;
      if (recipe.isRepair()) {
         Component comp = Component.literal("+" + recipe.getRepair().getB());
         stack.drawString(font, comp, 90 - font.width(comp), 2, 4210752, false);
      } else if (recipe.isFuel()) {
         ICannerRecipeRegistry.FuelValue value = recipe.getFuels();
         Component comp = Component.literal(
            "+"
               + (value.isMultiplier() ? ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)(value.getFuelMultiplier() * 100.0F)) + "%" : value.getFuelValue())
         );
         stack.drawString(font, comp, 90 - font.width(comp), 2, 4210752, false);
      }
   }

   public void setRecipe(IRecipeLayoutBuilder layout, CannerCategory.CannerRecipe recipe, IFocusGroup focus) {
      if (recipe.isFilling()) {
         List<ItemStack> extra = CollectionUtils.<ItemStack>createList();

         for (ItemStack out : ((IInput)recipe.getFilling().getA()).getComponents()) {
            if (out.hasCraftingRemainingItem()) {
               extra.add(out.getCraftingRemainingItem());
            }
         }

         layout.addSlot(RecipeIngredientRole.INPUT, 19, 38).addItemStack(recipe.getContainer());
         layout.addSlot(RecipeIngredientRole.INPUT, 19, 2).addItemStacks(((IInput)recipe.getFilling().getA()).getComponents());
         layout.addSlot(RecipeIngredientRole.OUTPUT, 69, 20).addItemStack((ItemStack)recipe.getFilling().getB());
         layout.addSlot(RecipeIngredientRole.OUTPUT, 69, 44).addItemStacks(extra);
      } else if (recipe.isRepair()) {
         List<ItemStack> extra = CollectionUtils.<ItemStack>createList();

         for (ItemStack outx : ((IInput)recipe.getRepair().getA()).getComponents()) {
            if (outx.hasCraftingRemainingItem()) {
               extra.add(outx.getCraftingRemainingItem());
            }
         }

         layout.addSlot(RecipeIngredientRole.INPUT, 19, 38).addItemStack(recipe.getContainer());
         layout.addSlot(RecipeIngredientRole.INPUT, 19, 2).addItemStacks(((IInput)recipe.getRepair().getA()).getComponents());
         layout.addSlot(RecipeIngredientRole.OUTPUT, 69, 20).addItemStack(recipe.getContainer());
         layout.addSlot(RecipeIngredientRole.OUTPUT, 69, 44).addItemStacks(extra);
      } else if (recipe.isFuel()) {
         ICannerRecipeRegistry.FuelValue result = recipe.getFuels();
         layout.addSlot(RecipeIngredientRole.INPUT, 19, 38).addItemStack(recipe.getContainer());
         layout.addSlot(RecipeIngredientRole.INPUT, 19, 2).addItemStack(result.getStack());
         layout.addSlot(RecipeIngredientRole.OUTPUT, 69, 20).addItemStack(recipe.getContainer());
         layout.addSlot(RecipeIngredientRole.OUTPUT, 69, 44).addItemStack(result.getStack().getCraftingRemainingItem());
      }
   }

   public static class CannerRecipe {
      ItemStack container;
      Object recipe;
      int type;

      public CannerRecipe(ItemStack container, Object recipe, int type) {
         this.container = container;
         this.recipe = recipe;
         this.type = type;
      }

      public boolean isRepair() {
         return this.type == 0;
      }

      public boolean isFuel() {
         return this.type == 1;
      }

      public boolean isFilling() {
         return this.type == 2;
      }

      public ItemStack getContainer() {
         return this.container;
      }

      public ICannerRecipeRegistry.FuelValue getFuels() {
         return (ICannerRecipeRegistry.FuelValue)this.recipe;
      }

      public Tuple<IInput, Integer> getRepair() {
         return (Tuple<IInput, Integer>)this.recipe;
      }

      public Tuple<IInput, ItemStack> getFilling() {
         return (Tuple<IInput, ItemStack>)this.recipe;
      }

      public static List<CannerCategory.CannerRecipe> createRecipes(GlobalRecipes recipe) {
         List<CannerCategory.CannerRecipe> result = new ObjectArrayList();
         ICannerRecipeRegistry registry = recipe.canner;

         for (Entry<Item, List<Tuple<IInput, Integer>>> entry : registry.getRepairItems().entrySet()) {
            ItemStack stack = new ItemStack((ItemLike)entry.getKey());

            for (Tuple<IInput, Integer> subEntry : entry.getValue()) {
               result.add(new CannerCategory.CannerRecipe(stack, subEntry, 0));
            }
         }

         result.add(new CannerCategory.CannerRecipe(new ItemStack(IC2Items.ELECTRIC_SPRAYER), new Tuple(new ItemInput(IC2Items.CF_PELLET), 26), 0));
         result.add(new CannerCategory.CannerRecipe(new ItemStack(IC2Items.CF_PACK), new Tuple(new ItemInput(IC2Items.CF_PELLET), 26), 0));

         for (ICannerRecipeRegistry.FuelValue entry : registry.getFuels()) {
            result.add(new CannerCategory.CannerRecipe(new ItemStack(IC2Items.FUEL_CAN), entry, 1));
         }

         for (Entry<ItemStack, List<Tuple<IInput, ItemStack>>> entry : registry.getFillables().entrySet()) {
            for (Tuple<IInput, ItemStack> subEntry : entry.getValue()) {
               result.add(new CannerCategory.CannerRecipe(entry.getKey(), subEntry, 2));
            }
         }

         for (Item item : ForgeRegistries.ITEMS) {
            if (item.isEdible()) {
               FoodProperties prop = item.getFoodProperties(new ItemStack(item), null);
               if (prop != null) {
                  int amount = Math.max(1, Mth.ceil((double)prop.getNutrition() / 2.0));
                  ItemStack input = new ItemStack(IC2Items.TIN_CAN, amount);
                  Item foodResult = RecipeRegistry.CAN_EFFECTS.getItemForFood(new ItemStack(item));
                  ItemStack output = new ItemStack(foodResult == null ? IC2Items.TIN_CAN_FILLED : foodResult, amount);
                  result.add(new CannerCategory.CannerRecipe(input, new Tuple(new ItemInput(item), output), 2));
               }
            }
         }

         return result;
      }
   }
}
