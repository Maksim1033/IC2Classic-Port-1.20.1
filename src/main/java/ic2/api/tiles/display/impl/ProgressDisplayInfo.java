package ic2.api.tiles.display.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import org.joml.Matrix4f;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.IMonitorRenderer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ProgressDisplayInfo implements IDisplayInfo {
   ProgressDisplayInfo.Progress progress;
   Supplier<ProgressDisplayInfo.Progress> progressProvider;
   BooleanSupplier aliveProvider;

   public ProgressDisplayInfo(FriendlyByteBuf buffer) {
      this.progress = new ProgressDisplayInfo.Progress(buffer);
   }

   public ProgressDisplayInfo(Supplier<ProgressDisplayInfo.Progress> progressProvider, BooleanSupplier aliveProvider) {
      this.progressProvider = progressProvider;
      this.aliveProvider = aliveProvider;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void render(PoseStack stack, int x, int y, int width, int height, IDisplayInfo.Alignment align, IMonitorRenderer helper) {
      Font font = helper.getFont();
      if (height > 9 + 2) {
         Tesselator tes = Tesselator.getInstance();
         BufferBuilder builder = tes.getBuilder();
         builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
         int color = this.progress.mainColor;
         x -= align.getXOffset(width);
         this.drawColorFrame(stack, builder, (float)x, (float)y, 1.0F, (float)height, color);
         this.drawColorFrame(stack, builder, (float)(x + width - 2), (float)y, 1.0F, (float)height, color);
         this.drawColorFrame(stack, builder, (float)x, (float)y, (float)(width - 1), 1.0F, color);
         this.drawColorFrame(stack, builder, (float)x, (float)(y + height), (float)(width - 1), 1.0F, color);
         int size = (int)((double)(width - 3) * this.progress.progress);

         for (int i = 0; i < size; i++) {
            this.drawColorFrame(
               stack, builder, (float)(x + i + 1), (float)(y + 1), 1.0F, (float)(height - 1), i % 2 == 0 ? color : this.progress.secondaryColor
            );
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         tes.end();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void drawColorFrame(PoseStack stack, VertexConsumer builder, float x, float y, float width, float height, int color) {
      Matrix4f matrix = stack.last().pose();
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      builder.vertex(matrix, x, y + height, 0.0F).color(r, g, b, a).endVertex();
      builder.vertex(matrix, x + width, y + height, 0.0F).color(r, g, b, a).endVertex();
      builder.vertex(matrix, x + width, y, 0.0F).color(r, g, b, a).endVertex();
      builder.vertex(matrix, x, y, 0.0F).color(r, g, b, a).endVertex();
   }

   public static int darker(int color, float factor) {
      int r = Math.max(0, (int)((float)(color >> 16 & 0xFF) * factor));
      int g = Math.max(0, (int)((float)(color >> 8 & 0xFF) * factor));
      int b = Math.max(0, (int)((float)(color & 0xFF) * factor));
      return color & 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getHeight(int width, IDisplayInfo.Alignment align) {
      return 9 + 5;
   }

   @Override
   public boolean isValid() {
      return this.aliveProvider.getAsBoolean();
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      this.progressProvider.get().serialize(buffer);
   }

   @Override
   public Tag getServerData() {
      ProgressDisplayInfo.Progress progress = this.progressProvider.get();
      return new LongArrayTag(new long[]{Double.doubleToLongBits(progress.progress), (long)progress.mainColor, (long)progress.secondaryColor});
   }

   public static class Progress {
      double progress;
      int mainColor;
      int secondaryColor;

      public Progress(FriendlyByteBuf buffer) {
         this.progress = buffer.readDouble();
         this.mainColor = buffer.readInt();
         this.secondaryColor = buffer.readInt();
      }

      public Progress(double progress, int mainColor) {
         this(progress, mainColor, mainColor);
      }

      public Progress(double progress, int mainColor, int secondaryColor) {
         this.progress = progress;
         this.mainColor = mainColor;
         this.secondaryColor = secondaryColor;
      }

      public void serialize(FriendlyByteBuf buffer) {
         buffer.writeDouble(this.progress);
         buffer.writeInt(this.mainColor);
         buffer.writeInt(this.secondaryColor);
      }
   }
}
