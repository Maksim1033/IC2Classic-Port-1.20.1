package ic2.core.wiki.recipes.renderers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.wiki.components.IWikiComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SmeltingRecipeRenderer implements IRecipeRenderer {
   Recipe<?> recipe;

   public SmeltingRecipeRenderer(Recipe<?> recipe) {
      this.recipe = recipe;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, PoseStack matrix, int x, int y) {
      int timer = screen.getClock() / 2 % 22;
      screen.bindDefaultTexture();
      screen.drawTextureRegion(matrix, (float)x, (float)y, 0.0F, 248.0F, 116.0F, 4.0F);
      screen.drawTextureRegion(matrix, (float)x, (float)(y + 60), 0.0F, 252.0F, 116.0F, 4.0F);
      screen.bindTexture(IWikiComponent.TEXTURE);
      x += 33;
      y += 20;
      screen.drawTextureRegion(matrix, (float)x, (float)y, 158.0F, 0.0F, 20.0F, 20.0F);
      screen.drawTextureRegion(matrix, (float)(x + 26), (float)(y + 2), 212.0F, 0.0F, 22.0F, 15.0F);
      screen.drawTextureRegion(matrix, (float)(x + 26), (float)(y + 2), 234.0F, 0.0F, (float)(22 - (22 - timer)), 15.0F);
      screen.drawTextureRegion(matrix, (float)(x + 56), (float)(y - 3), 124.0F, 0.0F, 24.0F, 24.0F, 18.0F, 18.0F);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderItems(IC2Screen screen, PoseStack matrix, int x, int y) {
      x += 33;
      y += 20;
      int time = screen.getClock() / 20;
      Lighting.setupFor3DItems();
      ItemRenderer item = screen.getRenderItem();
      RenderUtils.renderGuiItem(item, matrix, this.recipe.getToastSymbol(), (float)(x - 26), (float)(y + 2));
      this.renderMultiOutput(
         matrix, ObjectArrayList.wrap(((Ingredient)this.recipe.getIngredients().get(0)).getItems()), x + 2, y + 2, time, screen.getFont(), item
      );
      RenderUtils.renderGuiItem(item, matrix, this.recipe.getResultItem(IC2.PLATFORM.getRegistryAccess()), (float)(x + 60), (float)(y + 1));
      RenderUtils.renderGuiItemDecorations(matrix, screen.getFont(), this.recipe.getResultItem(IC2.PLATFORM.getRegistryAccess()), x + 60, y + 1);
      Lighting.setupForFlatItems();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Component> getHoverInfo(IC2Screen screen, int mouseX, int mouseY, int x, int y) {
      x += 33;
      y += 20;
      if (screen.isOverArea(mouseX, mouseY, x + 2, y + 2, 16, 16)) {
         int time = screen.getClock() / 20;
         ItemStack[] array = ((Ingredient)this.recipe.getIngredients().get(0)).getItems();
         if (array.length > 0) {
            return this.getToolTip(array[time % array.length]);
         }
      }

      if (screen.isOverArea(mouseX, mouseY, x + 60, y + 1, 16, 16)) {
         return this.getToolTip(this.recipe.getResultItem(IC2.PLATFORM.getRegistryAccess()));
      } else {
         return screen.isOverArea(mouseX, mouseY, x - 26, y + 1, 16, 16) ? this.getToolTip(this.recipe.getToastSymbol()) : Collections.emptyList();
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ItemStack getHoverStack(IC2Screen screen, int mouseX, int mouseY, int x, int y) {
      x += 33;
      y += 20;
      if (screen.isOverArea(mouseX, mouseY, x + 2, y + 2, 16, 16)) {
         int time = screen.getClock() / 20;
         ItemStack[] array = ((Ingredient)this.recipe.getIngredients().get(0)).getItems();
         if (array.length > 0) {
            return array[time % array.length].copy();
         }
      }

      if (screen.isOverArea(mouseX, mouseY, x + 60, y + 1, 16, 16)) {
         return this.recipe.getResultItem(IC2.PLATFORM.getRegistryAccess()).copy();
      } else {
         return screen.isOverArea(mouseX, mouseY, x - 26, y + 1, 16, 16) ? this.recipe.getToastSymbol().copy() : ItemStack.EMPTY;
      }
   }
}
