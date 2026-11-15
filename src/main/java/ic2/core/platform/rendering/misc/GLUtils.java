package ic2.core.platform.rendering.misc;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import org.joml.Matrix4f;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;

@OnlyIn(Dist.CLIENT)
public class GLUtils {
   public static void enableHighlight(boolean depth) {
      RenderSystem.enableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      if (depth) {
         RenderSystem.depthMask(true);
      } else {
         RenderSystem.depthMask(false);
      }
   }

   public static void disableHighlight(boolean depth) {
      RenderSystem.disableDepthTest();
      if (depth) {
         RenderSystem.depthMask(false);
      }
   }

   public static void drawTooltip(PoseStack stack, List<Component> textLines, int x, int y) {
      Minecraft mc = Minecraft.getInstance();
      Window main = mc.getWindow();
      int width = main.getScreenWidth();
      int height = main.getScreenHeight();
      drawTooltipRaw(
         stack, ForgeHooksClient.gatherTooltipComponents(ItemStack.EMPTY, textLines, x, width, height, mc.font), x, y, width, height, mc.font
      );
   }

   private static void drawTooltipRaw(PoseStack stack, List<ClientTooltipComponent> textLines, int x, int y, int screenWidth, int screenHeight, Font font) {
      int width = 0;
      int height = textLines.size() == 1 ? -2 : 0;

      for (ClientTooltipComponent comp : textLines) {
         width = Math.max(width, comp.getWidth(font));
         height += comp.getHeight();
      }

      int minX = x + 12;
      int minY = y - 12;
      if (minX + width > screenWidth) {
         minX -= 28 + width;
      }

      if (minY + height + 6 > screenHeight) {
         minY = screenHeight - height - 6;
      }

      if (y - height - 8 < 0) {
         minY = y + 8;
      }

      drawArea(stack, x, y, minX + width + 5, minY + height);
      BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
      stack.pushPose();
      stack.translate(0.0, 0.0, 400.0);
      Matrix4f matrix = stack.last().pose();
      int tooltipTop = minY;

      for (int i2 = 0; i2 < textLines.size(); i2++) {
         ClientTooltipComponent comp = textLines.get(i2);
         comp.renderText(font, minX, tooltipTop, matrix, buffer);
         tooltipTop += comp.getHeight() + (i2 == 0 ? 2 : 0);
      }

      buffer.endBatch();
      stack.popPose();
      stack.pushPose();
      tooltipTop = minY;
      ItemRenderer item = Minecraft.getInstance().getItemRenderer();

      for (int l2 = 0; l2 < textLines.size(); l2++) {
         ClientTooltipComponent comp = textLines.get(l2);
         // comp.renderImage
         tooltipTop += comp.getHeight() + (l2 == 0 ? 2 : 0);
      }

      buffer.endBatch();
      stack.popPose();
   }

   public static void drawArea(PoseStack stack, int x, int y, int width, int height) {
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      Tesselator tes = Tesselator.getInstance();
      BufferBuilder buffer = tes.getBuilder();
      buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      int minX = 4 + x;
      int maxX = minX + width;
      int minY = 4 + y;
      int maxY = minY + height;
      fillArea(stack, minX - 3, minY - 4, maxX + 3, minY - 3, -267386864, buffer);
      fillArea(stack, minX - 3, maxY + 3, maxX + 3, maxY + 4, -267386864, buffer);
      fillArea(stack, minX - 3, minY - 3, maxX + 3, maxY + 3, -267386864, buffer);
      fillArea(stack, minX - 4, minY - 3, minX - 3, maxY + 3, -267386864, buffer);
      fillArea(stack, maxX + 3, minY - 3, maxX + 4, maxY + 3, -267386864, buffer);
      fillArea(stack, minX - 3, minY - 3 + 1, minX - 3 + 1, maxY + 3 - 1, 1347420415, buffer);
      fillArea(stack, maxX + 2, minY - 3 + 1, maxX + 3, maxY + 3 - 1, 1347420415, buffer);
      fillArea(stack, minX - 3, minY - 3, maxX + 3, minY - 3 + 1, 1347420415, buffer);
      fillArea(stack, minX - 3, maxY + 2, maxX + 3, maxY + 3, 1344798847, buffer);
      RenderSystem.enableDepthTest();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      tes.end();
      // GlStateManager.m_84507_
      // GlStateManager.m_84519_
      RenderSystem.disableBlend();
   }

   public static void fillArea(PoseStack stack, int left, int top, int right, int bottom, int color, BufferBuilder buffer) {
      float a = (float)(color >> 24 & 0xFF) / 255.0F;
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      Matrix4f matrix = stack.last().pose();
      buffer.vertex(matrix, (float)right, (float)top, -10.0F).color(r, g, b, a).endVertex();
      buffer.vertex(matrix, (float)left, (float)top, -10.0F).color(r, g, b, a).endVertex();
      buffer.vertex(matrix, (float)left, (float)bottom, -10.0F).color(r, g, b, a).endVertex();
      buffer.vertex(matrix, (float)right, (float)bottom, -10.0F).color(r, g, b, a).endVertex();
   }
}
