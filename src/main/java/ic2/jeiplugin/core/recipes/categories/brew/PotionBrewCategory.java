package ic2.jeiplugin.core.recipes.categories.brew;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.api.recipes.registries.IPotionBrewRegistry;
import ic2.core.IC2;
import ic2.core.block.misc.tiles.BarrelTileEntity;
import ic2.core.platform.recipes.misc.GlobalRecipes;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class PotionBrewCategory implements IRecipeCategory<PotionBrewCategory.PotionBrew> {
   RecipeType<PotionBrewCategory.PotionBrew> id;
   IDrawable background;
   IDrawable icon;

   public PotionBrewCategory(IGuiHelper helper, RecipeType<PotionBrewCategory.PotionBrew> location, ResourceLocation texture, ItemLike provider) {
      this.id = location;
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(provider));
      this.background = helper.createDrawable(texture, 40, 10, 100, 65);
   }

   public RecipeType<PotionBrewCategory.PotionBrew> getRecipeType() {
      return this.id;
   }

   public Component getTitle() {
      return Component.literal("Potion Brew");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   @OnlyIn(Dist.CLIENT)
   public void draw(PotionBrewCategory.PotionBrew recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
      Font font = Minecraft.getInstance().font;
      long time = 0L;

      for (int i = 0; i <= recipe.duration; i++) {
         time += (long)(5000.0 * Math.pow(3.0, (double)i) * 2.5);
      }

      String s = DurationFormatUtils.formatDuration(time, "HH:mm:ss");
      stack.drawString(font, s, 75 - font.width(s) / 2, 8, 4210752, false);
   }

   public void setRecipe(IRecipeLayoutBuilder layout, PotionBrewCategory.PotionBrew recipe, IFocusGroup focus) {
      layout.addSlot(RecipeIngredientRole.INPUT, 25, 25).addItemStack(recipe.container);
      layout.addSlot(RecipeIngredientRole.INPUT, 5, 5).addItemStack(recipe.prev);
      layout.addSlot(RecipeIngredientRole.INPUT, 25, 5).addItemStack(recipe.ingredient);
      layout.addSlot(RecipeIngredientRole.INPUT, 5, 45).addItemStack(recipe.getGlowStone());
      layout.addSlot(RecipeIngredientRole.INPUT, 25, 45).addItemStack(recipe.getRedStone());
      layout.addSlot(RecipeIngredientRole.INPUT, 5, 25).addItemStack(recipe.getTime());
      layout.addSlot(RecipeIngredientRole.OUTPUT, 76, 25).addItemStack(recipe.result);
   }

   public static class PotionBrew implements Comparable<PotionBrewCategory.PotionBrew> {
      MobEffect instance;
      ItemStack ingredient;
      ItemStack container;
      ItemStack prev;
      ItemStack result;
      int redstone;
      int glowStone;
      int duration;

      public PotionBrew(
         MobEffect instance, ItemStack ingredient, ItemStack container, ItemStack prev, ItemStack result, int redstone, int glowStone, int duration
      ) {
         this.instance = instance;
         this.ingredient = ingredient;
         this.container = container;
         this.prev = prev;
         this.result = result;
         this.redstone = redstone;
         this.glowStone = glowStone;
         this.duration = duration;
      }

      public int compareTo(PotionBrewCategory.PotionBrew o) {
         int result = Integer.compare(BuiltInRegistries.MOB_EFFECT.getId(this.instance), BuiltInRegistries.MOB_EFFECT.getId(o.instance));
         if (result != 0) {
            return result;
         } else {
            result = Integer.compare(this.redstone, o.redstone);
            if (result != 0) {
               return result;
            } else {
               result = Integer.compare(this.glowStone, o.glowStone);
               return result != 0 ? result : Integer.compare(this.duration, o.duration);
            }
         }
      }

      public boolean hasRedstone() {
         return this.redstone > 0;
      }

      public boolean hasGlowStone() {
         return this.glowStone > 0;
      }

      public ItemStack getRedStone() {
         return new ItemStack(Items.REDSTONE, this.redstone);
      }

      public ItemStack getGlowStone() {
         return new ItemStack(Items.GLOWSTONE_DUST, this.glowStone);
      }

      public ItemStack getTime() {
         return new ItemStack(Items.CLOCK, this.duration + 1);
      }

      public static List<PotionBrewCategory.PotionBrew> createList(GlobalRecipes recipes) {
         List<PotionBrewCategory.PotionBrew> result = CollectionUtils.<PotionBrewCategory.PotionBrew>createList();
         IPotionBrewRegistry registry = recipes.potions;
         Map<Item, IPotionBrewRegistry.PotionContainer> containers = registry.getContainers();
         Map<Item, Map<MobEffect, MobEffect>> effects = registry.getEffects();

         for (int time = 0; time <= 6; time += 6) {
            for (int glow = 0; glow < 3; glow++) {
               for (int red = 0; red < 3; red++) {
                  int redstone = red == 0 ? 0 : (red == 1 ? 10 : 20);
                  int glowStone = glow == 0 ? 0 : (red == 1 ? 10 : 20);
                  int solid = time > 0 && red > 0
                     ? (int)Mth.clamp((float)time * ((float)redstone / 10.0F), 0.0F, (float)(BarrelTileEntity.POTION_DURATIONS.length - 1))
                     : 0;
                  int alc = time > 0 && glow > 0 ? (int)Mth.clamp((float)time * 0.5F * ((float)glowStone / 10.0F), 0.0F, 2.0F) : 0;
                  switch (alc) {
                     case 1:
                        solid = (int)((float)solid * 0.5F);
                        break;
                     case 2:
                        solid = (int)((float)solid * 0.25F);
                  }

                  for (Entry<Item, Map<MobEffect, MobEffect>> entry : effects.entrySet()) {
                     for (Entry<MobEffect, MobEffect> subEntry : entry.getValue().entrySet()) {
                        ItemStack prev = subEntry.getKey() == null
                           ? new ItemStack(Items.NETHER_WART)
                           : createPotions(subEntry.getKey(), 0, 20, new ItemStack(Items.POTION));

                        for (Entry<Item, IPotionBrewRegistry.PotionContainer> containerEntry : containers.entrySet()) {
                           IPotionBrewRegistry.PotionContainer value = containerEntry.getValue();
                           ItemStack output = createPotions(
                              subEntry.getValue(),
                              alc,
                              (int)Math.max(20.0F, (float)BarrelTileEntity.POTION_DURATIONS[10 - solid] * 20.0F * value.getDurationEffectiveness()),
                              new ItemStack(value.getOutput(), 1000 / value.getFluidUsage())
                           );
                           result.add(
                              new PotionBrewCategory.PotionBrew(
                                 subEntry.getValue(),
                                 new ItemStack((ItemLike)entry.getKey()),
                                 new ItemStack((ItemLike)containerEntry.getKey(), 1000 / value.getFluidUsage()),
                                 prev,
                                 output,
                                 redstone,
                                 glowStone,
                                 time
                              )
                           );
                        }
                     }
                  }
               }
            }
         }

         result.sort(null);
         return result;
      }

      public static ItemStack createPotions(MobEffect effect, int level, int time, ItemStack stack) {
         List<MobEffectInstance> effects = ObjectLists.singleton(new MobEffectInstance(effect, time, level));
         PotionUtils.setCustomEffects(stack, effects);
         stack.getOrCreateTag().putInt("CustomPotionColor", PotionUtils.getColor(effects));
         MutableComponent name = Component.translatable(stack.getItem().getDescriptionId() + ".effect." + IC2.RECIPES.get(false).potions.getName(effect));
         name.setStyle(name.getStyle().withItalic(false));
         stack.setHoverName(name);
         return stack;
      }
   }
}
