package ic2.jeiplugin.core.recipes.categories;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.block.machines.logic.planner.encoder.ReactorSetup;
import ic2.core.block.machines.logic.planner.newLogic.SimulationResult;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.tooltips.ILangHelper;
import it.unimi.dsi.fastutil.objects.AbstractObject2DoubleMap.BasicEntry;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
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
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class ReactorCategory implements IRecipeCategory<ReactorCategory.ReactorRecipe> {
   RecipeType<ReactorCategory.ReactorRecipe> id;
   ItemStack stack;
   IDrawable background;
   IDrawable icon;
   IDrawable slot;

   public ReactorCategory(IGuiHelper helper, RecipeType<ReactorCategory.ReactorRecipe> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.stack = new ItemStack(provider);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.stack);
      this.slot = helper.createDrawable(texture, 165, 0, 18, 18);
      this.background = helper.createDrawable(texture, 0, 8, 165, 186);
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public RecipeType<ReactorCategory.ReactorRecipe> getRecipeType() {
      return this.id;
   }

   public Component getTitle() {
      return this.stack.getDisplayName();
   }

   public void draw(ReactorCategory.ReactorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics matrixStack, double mouseX, double mouseY) {
      int size = recipe.setup.chambersRequired + 3;
      int startX = 83 - 9 * size;
      int startY = -1;

      for (int i = 0; i < 54; i++) {
         int x = i % 9;
         if (x < size) {
            this.slot.draw(matrixStack, startX + 18 * x, startY + 18 * (i / 9));
         }
      }

      Font font = Minecraft.getInstance().font;
      int y = 110;

      for (Component process : recipe.text) {
         matrixStack.drawString(font, process, 0, y, 4210752, false);
         y += 10;
      }

      matrixStack.drawString(
         font,
         Component.translatable("jei.ic2.reactor.clicked")
            .withStyle(Style.EMPTY.withUnderlined(mouseX >= 99.0 && mouseX <= 166.0 && mouseY >= 108.0 && mouseY <= 118.0)),
         100,
         110,
         4210752,
         false
      );
   }

   public List<Component> getTooltipStrings(ReactorCategory.ReactorRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      Style style = IC2Screen.getComponentNew(Lists.transform(recipe.text, Component::getVisualOrderText), (int)mouseX, (int)mouseY - 110);
      if (style != null && style.getHoverEvent() != null) {
         Component comp = (Component)style.getHoverEvent().getValue(Action.SHOW_TEXT);
         if (comp != null) {
            return CollectionUtils.<Component>asList(comp);
         }
      }

      return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
   }

   public boolean handleInput(ReactorCategory.ReactorRecipe recipe, double mouseX, double mouseY, Key mouseButton) {
      if (mouseX >= 99.0 && mouseX <= 166.0 && mouseY >= 108.0 && mouseY <= 118.0) {
         Minecraft.getInstance().keyboardHandler.setClipboard(recipe.data);
         Minecraft.getInstance().player.displayClientMessage(Component.translatable("jei.ic2.reactor.copied"), false);
         return true;
      } else {
         return false;
      }
   }

   public void setRecipe(IRecipeLayoutBuilder layout, ReactorCategory.ReactorRecipe recipe, IFocusGroup focus) {
      int size = recipe.setup.chambersRequired + 3;
      int startX = 84 - 9 * size;
      int startY = 0;

      for (int i = 0; i < 54; i++) {
         int x = i % 9;
         if (x < size) {
            layout.addSlot(RecipeIngredientRole.INPUT, startX + 18 * x, startY + 18 * (i / 9)).addItemStack(recipe.setup.items.getStackInSlot(i));
         }
      }
   }

   public static class ReactorRecipe implements ILangHelper {
      public static final String[] EU_SUFFIX = new String[]{"EU", "k EU", "m EU", "b EU"};
      public static final String[] BUCKET_SUFFIX = new String[]{"mB", "B", "kB", "MB"};
      public final String name;
      public final String data;
      public final ReactorSetup setup;
      public final List<Component> text = CollectionUtils.<Component>createList();

      public ReactorRecipe(String name, ReactorSetup setup) {
         this.name = name;
         this.data = setup.setup;
         this.setup = setup;
         this.createText();
      }

      private void createText() {
         SimulationResult result = this.setup.simulation;
         this.text.add(this.translate("jei.ic2.reactor.by", new Object[]{this.name}));
         this.text
            .add(
               this.translate(
                  "jei.ic2.reactor.stats", new Object[]{this.bool(result != null ? result.isStable : this.setup.isStable), this.bool(this.setup.breeder)}
               )
            );
         this.text
            .add(
               this.translate(
                  "jei.ic2.reactor.efficiency",
                  new Object[]{Formatters.XP_FORMAT.format((double)this.setup.efficiency), Formatters.XP_FORMAT.format((double)this.setup.totalEfficiency)}
               )
            );
         this.text.add(this.translate("jei.ic2.reactor.heat", new Object[]{Formatters.EU_FORMAT.format((long)this.setup.startingHeat)}));
         if (this.setup.isSteam) {
            this.text
               .add(
                  this.translate(
                     "jei.ic2.reactor.steam", new Object[]{this.getValue(this.setup.totalOutput, true), Formatters.XP_FORMAT.format((double)this.setup.output)}
                  )
               );
            if (result != null) {
               MutableComponent effective = this.translate(
                  "jei.ic2.reactor.steam.effective",
                  new Object[]{
                     this.getValue((long)((float)this.setup.totalOutput * result.effectiveProduction), true),
                     Formatters.XP_FORMAT.format((double)(this.setup.output * result.effectiveProduction))
                  }
               );
               this.text.add(effective.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, this.translate("jei.ic2.reactor.effective")))));
            }
         } else {
            this.text
               .add(
                  this.translate(
                     "jei.ic2.reactor.output",
                     new Object[]{this.getValue(this.setup.totalOutput, false), Formatters.EU_FORMAT.format((double)this.setup.output)}
                  )
               );
            if (result != null) {
               MutableComponent effective = this.translate(
                  "jei.ic2.reactor.output.effective",
                  new Object[]{
                     this.getValue((long)((float)this.setup.totalOutput * result.effectiveProduction), false),
                     Formatters.EU_FORMAT.format((double)(this.setup.output * result.effectiveProduction))
                  }
               );
               this.text.add(effective.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, this.translate("jei.ic2.reactor.effective")))));
            }
         }

         if (result != null) {
            this.text
               .add(
                  this.translate(
                     "Gen/Cool Time: %s/%s",
                     new Object[]{
                        DurationFormatUtils.formatDuration((long)result.ticksSimulated * (long)result.tickRate * 50L, "HH:mm:ss"),
                        DurationFormatUtils.formatDuration((long)result.ticksCooled * 50L, "HH:mm:ss")
                     }
                  )
               );
         }
      }

      private String getValue(long value, boolean bucket) {
         Entry<String> entry = this.getSuffices(value, bucket);
         return Formatters.EU_FORMAT.format((double)this.setup.totalOutput / entry.getDoubleValue()) + (String)entry.getKey();
      }

      private Entry<String> getSuffices(long input, boolean bucket) {
         double value = 1.0;
         int index = 0;

         while (input > 1000L && index + 1 < BUCKET_SUFFIX.length) {
            index++;
            value *= 1000.0;
            input /= 1000L;
         }

         return new BasicEntry(bucket ? BUCKET_SUFFIX[index] : EU_SUFFIX[index], value);
      }

      public boolean isValid() {
         return this.setup != null;
      }
   }
}
