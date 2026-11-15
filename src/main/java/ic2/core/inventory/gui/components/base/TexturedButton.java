package ic2.core.inventory.gui.components.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedButton extends ToolTipButton {
   ResourceLocation texture;
   Box2i bounds;
   boolean full = false;

   public TexturedButton(int xPos, int yPos, int width, int height, ResourceLocation texture, Box2i bounds, OnPress handler) {
      super(xPos, yPos, width, height, Component.empty(), handler);
      this.texture = texture;
      this.bounds = bounds;
   }

   public TexturedButton setFull() {
      this.full = true;
      return this;
   }

   @Override
   public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
      super.renderWidget(guiGraphics, mouseX, mouseY, partial);
      if (this.visible) {
         RenderSystem.setShaderTexture(0, this.texture);
         this.drawTextureRegion(
            guiGraphics.pose(),
            (float)(this.getX() + (this.full ? 0 : 2)),
            (float)(this.getY() + (this.full ? 0 : 2)),
            (float)this.bounds.getX(),
            (float)this.bounds.getY(),
            (float)(this.width - (this.full ? 0 : 4)),
            (float)(this.height - (this.full ? 0 : 4)),
            (float)this.bounds.getWidth(),
            (float)this.bounds.getHeight(),
            ColorUtils.darker(-1, this.isHovered ? 0.75F : (this.active ? 1.0F : 0.5F))
         );
      }
   }

   public void drawTextureRegion(
      PoseStack stack, float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight, int color
   ) {
      Matrix4f matrix = stack.last().pose();
      float maxX = x + width;
      float maxY = y + height;
      float t_minX = texX / 256.0F;
      float t_minY = texY / 256.0F;
      float t_maxX = (texX + texWidth) / 256.0F;
      float t_maxY = (texY + texHeight) / 256.0F;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
      bufferbuilder.vertex(matrix, x, maxY, 0.0F)
         .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF)
         .uv(t_minX, t_maxY)
         .endVertex();
      bufferbuilder.vertex(matrix, maxX, maxY, 0.0F)
         .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF)
         .uv(t_maxX, t_maxY)
         .endVertex();
      bufferbuilder.vertex(matrix, maxX, y, 0.0F)
         .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF)
         .uv(t_maxX, t_minY)
         .endVertex();
      bufferbuilder.vertex(matrix, x, y, 0.0F)
         .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF)
         .uv(t_minX, t_minY)
         .endVertex();
      tesselator.end();
   }
}
