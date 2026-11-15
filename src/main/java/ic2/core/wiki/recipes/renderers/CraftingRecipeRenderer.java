package ic2.core.wiki.recipes.renderers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.wiki.components.IWikiComponent;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class CraftingRecipeRenderer implements IRecipeRenderer {
   Recipe<?> recipe;

   public CraftingRecipeRenderer(Recipe<?> recipe) {
      this.recipe = recipe;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, PoseStack matrix, int x, int y) {
      screen.bindDefaultTexture();
      screen.drawTextureRegion(matrix, (float)x, (float)y, 0.0F, 248.0F, 116.0F, 4.0F);
      screen.drawTextureRegion(matrix, (float)x, (float)(y + 60), 0.0F, 252.0F, 116.0F, 4.0F);
      screen.bindTexture(IWikiComponent.TEXTURE);
      x += 7;
      y += 7;

      for (int i = 0; i < 9; i++) {
         screen.drawTextureRegion(matrix, (float)(x - 1 + i / 3 * 17), (float)(y - 1 + i % 3 * 17), 124.0F, 0.0F, 18.0F, 18.0F, 18.0F, 18.0F);
      }

      screen.drawTextureRegion(matrix, (float)(x + 56), (float)(y + 19), 124.0F, 18.0F, 16.0F, 12.0F, 16.0F, 12.0F);
      screen.drawTextureRegion(matrix, (float)(x + 77), (float)(y + 13), 124.0F, 0.0F, 24.0F, 24.0F, 18.0F, 18.0F);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderItems(IC2Screen screen, PoseStack matrix, int x, int y) {
      x += 7;
      y += 7;
      int time = screen.getClock() / 20;
      NonNullList<Ingredient> list = this.recipe.getIngredients();
      Lighting.setupFor3DItems();
      ItemRenderer item = screen.getRenderItem();
      IShapedRecipe<?> shaped = this.recipe instanceof IShapedRecipe ? (IShapedRecipe)this.recipe : null;
      int xMax = shaped == null ? 3 : shaped.getRecipeWidth();
      int yMax = shaped == null ? 3 : shaped.getRecipeHeight();

      for (int xPos = 0; xPos < xMax; xPos++) {
         for (int yPos = 0; yPos < yMax; yPos++) {
            int index = yPos * xMax + xPos;
            if (list.size() > index) {
               Ingredient entry = (Ingredient)list.get(index);
               if (entry != Ingredient.EMPTY) {
                  int length = entry.getItems().length;
                  if (length != 0) {
                     int posX = x + xPos * 17;
                     int posY = y + yPos * 17;
                     this.renderMultiOutput(matrix, entry.getItems(), posX, posY, time, screen.getFont(), item);
                  }
               }
            }
         }
      }

      ItemStack stack = this.recipe.getResultItem(IC2.PLATFORM.getRegistryAccess());
      RenderUtils.renderGuiItem(item, matrix, stack, (float)(x + 81), (float)(y + 17));
      RenderUtils.renderGuiItemDecorations(matrix, screen.getFont(), stack, x + 81, y + 17);
      Lighting.setupForFlatItems();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Component> getHoverInfo(IC2Screen screen, int mouseX, int mouseY, int x, int y) {
      x += 7;
      y += 7;
      if (screen.isOverArea(mouseX, mouseY, x + 81, y + 17, 16, 16)) {
         return this.getToolTip(this.recipe.getResultItem(IC2.PLATFORM.getRegistryAccess()));
      } else {
         NonNullList<Ingredient> list = this.recipe.getIngredients();
         int time = screen.getClock() / 20;
         IShapedRecipe<?> shaped = this.recipe instanceof IShapedRecipe ? (IShapedRecipe)this.recipe : null;
         int xMax = shaped == null ? 3 : shaped.getRecipeWidth();
         int yMax = shaped == null ? 3 : shaped.getRecipeHeight();

         for (int xPos = 0; xPos < xMax; xPos++) {
            for (int yPos = 0; yPos < yMax; yPos++) {
               int index = yPos * xMax + xPos;
               if (list.size() > index) {
                  Ingredient entry = (Ingredient)list.get(index);
                  if (entry != Ingredient.EMPTY) {
                     int posX = xPos * 18;
                     int posY = yPos * 18;
                     if (screen.isOverArea(mouseX, mouseY, x + posX, y + posY, 16, 16)) {
                        ItemStack[] array = entry.getItems();
                        return this.getToolTip(array[time % array.length]);
                     }
                  }
               }
            }
         }

         return Collections.emptyList();
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ItemStack getHoverStack(IC2Screen screen, int mouseX, int mouseY, int x, int y) {
      x += 7;
      y += 7;
      if (screen.isOverArea(mouseX, mouseY, x + 81, y + 17, 16, 16)) {
         return this.recipe.getResultItem(IC2.PLATFORM.getRegistryAccess()).copy();
      } else {
         NonNullList<Ingredient> list = this.recipe.getIngredients();
         int time = screen.getClock() / 20;
         IShapedRecipe<?> shaped = this.recipe instanceof IShapedRecipe ? (IShapedRecipe)this.recipe : null;
         int xMax = shaped == null ? 3 : shaped.getRecipeWidth();
         int yMax = shaped == null ? 3 : shaped.getRecipeHeight();

         for (int xPos = 0; xPos < xMax; xPos++) {
            for (int yPos = 0; yPos < yMax; yPos++) {
               int index = yPos * xMax + xPos;
               if (list.size() > index) {
                  Ingredient entry = (Ingredient)list.get(index);
                  if (entry != Ingredient.EMPTY) {
                     int posX = xPos * 18;
                     int posY = yPos * 18;
                     if (screen.isOverArea(mouseX, mouseY, x + posX, y + posY, 16, 16)) {
                        ItemStack[] array = entry.getItems();
                        return array[time % array.length].copy();
                     }
                  }
               }
            }
         }

         return ItemStack.EMPTY;
      }
   }
}
