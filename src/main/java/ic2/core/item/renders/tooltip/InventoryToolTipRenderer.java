package ic2.core.item.renders.tooltip;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class InventoryToolTipRenderer implements ClientTooltipComponent {
   public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
   private final List<ItemStack> items;

   public InventoryToolTipRenderer(List<ItemStack> items) {
      this.items = items;
   }

   @Override
   public int getHeight() {
      return this.gridSizeY() * 20 + 2 + 4;
   }

   @Override
   public int getWidth(Font font) {
      return this.gridSizeX() * 18 + 2;
   }

   @Override
   public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
      int i = this.gridSizeX();
      int j = this.gridSizeY();
      int k = 0;

      for (int l = 0; l < j; l++) {
         for (int i1 = 0; i1 < i; i1++) {
            int j1 = x + i1 * 18 + 1;
            int k1 = y + l * 20 + 1;
            this.renderSlot(j1, k1, k++, font, guiGraphics);
         }
      }

      this.drawBorder(x, y, i, j, guiGraphics);
   }

   private void renderSlot(int x, int y, int index, Font font, GuiGraphics guiGraphics) {
      if (index >= this.items.size()) {
         this.blit(guiGraphics, x, y, InventoryToolTipRenderer.Texture.SLOT);
      } else {
         ItemStack itemstack = this.items.get(index);
         this.blit(guiGraphics, x, y, InventoryToolTipRenderer.Texture.SLOT);
         guiGraphics.renderItem(itemstack, x + 1, y + 1);
         guiGraphics.renderItemDecorations(font, itemstack, x + 1, y + 1);
      }
   }

   private void drawBorder(int x, int y, int sizeX, int sizeY, GuiGraphics guiGraphics) {
      this.blit(guiGraphics, x, y, InventoryToolTipRenderer.Texture.BORDER_CORNER_TOP);
      this.blit(guiGraphics, x + sizeX * 18 + 1, y, InventoryToolTipRenderer.Texture.BORDER_CORNER_TOP);

      for (int i = 0; i < sizeX; i++) {
         this.blit(guiGraphics, x + 1 + i * 18, y, InventoryToolTipRenderer.Texture.BORDER_HORIZONTAL_TOP);
         this.blit(guiGraphics, x + 1 + i * 18, y + sizeY * 20, InventoryToolTipRenderer.Texture.BORDER_HORIZONTAL_BOTTOM);
      }

      for (int j = 0; j < sizeY; j++) {
         this.blit(guiGraphics, x, y + j * 20 + 1, InventoryToolTipRenderer.Texture.BORDER_VERTICAL);
         this.blit(guiGraphics, x + sizeX * 18 + 1, y + j * 20 + 1, InventoryToolTipRenderer.Texture.BORDER_VERTICAL);
      }

      this.blit(guiGraphics, x, y + sizeY * 20, InventoryToolTipRenderer.Texture.BORDER_CORNER_BOTTOM);
      this.blit(guiGraphics, x + sizeX * 18 + 1, y + sizeY * 20, InventoryToolTipRenderer.Texture.BORDER_CORNER_BOTTOM);
   }

   private void blit(GuiGraphics guiGraphics, int x, int y, InventoryToolTipRenderer.Texture texture) {
      guiGraphics.blit(TEXTURE_LOCATION, x, y, (float)texture.x, (float)texture.y, texture.w, texture.h, 128, 128);
   }

   private int gridSizeX() {
      return Mth.clamp(this.items.size(), 1, 7);
   }

   private int gridSizeY() {
      return (int)Math.ceil((double)this.items.size() / (double)this.gridSizeX());
   }

   static enum Texture {
      SLOT(0, 0, 18, 20),
      BLOCKED_SLOT(0, 40, 18, 20),
      BORDER_VERTICAL(0, 18, 1, 20),
      BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
      BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
      BORDER_CORNER_TOP(0, 20, 1, 1),
      BORDER_CORNER_BOTTOM(0, 60, 1, 1);

      public final int x;
      public final int y;
      public final int w;
      public final int h;

      private Texture(int x, int y, int w, int h) {
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
      }
   }
}
