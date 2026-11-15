package ic2.core.platform.rendering;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import java.util.function.BiConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.Font;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {
   public static PoseStack createPlayer(PoseStack matrix) {
      matrix.pushPose();
      Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      matrix.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
      return matrix;
   }

   public static Frustum createClipper(RenderLevelStageEvent event) {
      Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      Frustum helper = new Frustum(event.getPoseStack().last().pose(), event.getProjectionMatrix());
      helper.prepare(playerPos.x(), playerPos.y(), playerPos.z());
      return helper;
   }

   public static Tesselator draw(PoseStack stack, BiConsumer<BufferBuilder, PoseStack> builder) {
      Tesselator tes = Tesselator.getInstance();
      builder.accept(tes.getBuilder(), createPlayer(stack));
      stack.popPose();
      return tes;
   }

   public static void renderGuiItem(ItemRenderer render, PoseStack matrix, ItemStack stack, float x, float y) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      BakedModel model = render.getModel(stack, null, null, 0);
      boolean flag = !model.usesBlockLight();
      if (flag) {
         Lighting.setupForFlatItems();
      }

      BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
      matrix.pushPose();
      matrix.translate((double)(8.0F + x), (double)(8.0F + y), 100.0);
      matrix.scale(16.0F, -16.0F, 16.0F);
      matrix.last().normal().identity();
      render.render(stack, ItemDisplayContext.GUI, false, matrix, buffer, 15728880, OverlayTexture.NO_OVERLAY, model);
      matrix.popPose();
      buffer.endBatch();
      RenderSystem.disableBlend();
      if (flag) {
         Lighting.setupFor3DItems();
      }
   }

   public static void renderGuiItemDecorations(PoseStack matrixStack, Font font, ItemStack stack, int x, int y) {
      renderGuiItemDecorations(matrixStack, font, stack, (float)x, (float)y, null);
   }

   public static void renderGuiItemDecorations(PoseStack matrixStack, Font font, ItemStack stack, float x, float y, String text) {
      if (!stack.isEmpty()) {
         if (stack.getCount() != 1 || text != null) {
            String s = text == null ? String.valueOf(stack.getCount()) : text;
            matrixStack.translate(0.0, 0.0, 200.0);
            BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            font.drawInBatch(s, x + 19.0F - 2.0F - (float)font.width(s), y + 9.0F, 16777215, true, matrixStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, 15728880);
            matrixStack.translate(0.0, 0.0, -200.0);
            buffer.endBatch();
         }

         if (stack.isBarVisible()) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            int i = stack.getBarWidth();
            int j = stack.getBarColor();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            fillRect(buffer, matrixStack.last().pose(), x + 2.0F, y + 13.0F, 13.0F, 2.0F, 0, 0, 0, 255);
            fillRect(buffer, matrixStack.last().pose(), x + 2.0F, y + 13.0F, (float)i, 1.0F, j >> 16 & 0xFF, j >> 8 & 0xFF, j & 0xFF, 255);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            tesselator.end();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
         }
      }
   }

   private static void fillRect(BufferBuilder buffer, Matrix4f matrix, float x, float y, float width, float height, int r, int g, int b, int a) {
      buffer.vertex(matrix, x, y, 0.0F).color(r, g, b, a).endVertex();
      buffer.vertex(matrix, x, y + height, 0.0F).color(r, g, b, a).endVertex();
      buffer.vertex(matrix, x + width, y + height, 0.0F).color(r, g, b, a).endVertex();
      buffer.vertex(matrix, x + width, y, 0.0F).color(r, g, b, a).endVertex();
   }

   @OnlyIn(Dist.CLIENT)
   public static void prepareText(
      PoseStack stack, Direction direction, float textScale, double startX, double startY, double zLevel, double x, double y, double z
   ) {
      stack.translate(x + 0.5, y + 0.5, z + 0.5);
      if (direction.getAxis().isVertical()) {
         stack.mulPose(Axis.XP.rotationDegrees(direction == Direction.DOWN ? 90.0F : 270.0F));
      } else {
         stack.mulPose(Axis.YP.rotationDegrees(direction.toYRot() + 180.0F));
      }

      stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
      stack.translate(-0.5, -0.5, -0.5);
      stack.translate(0.0, 0.0, zLevel);
      stack.scale(0.0039063F, 0.0039063F, -1.0E-4F);
      stack.translate(startX, startY, 0.0);
      stack.scale(textScale, textScale, 1.0F);
   }

   public static Particle addOptionalParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      Minecraft mc = Minecraft.getInstance();
      Camera activeRenderInfo = mc.gameRenderer.getMainCamera();
      if (activeRenderInfo.isInitialized()) {
         if (activeRenderInfo.getPosition().distanceToSqr(x, y, z) > 1024.0) {
            return null;
         } else {
            return calculateParticleStatus(true, mc) == ParticleStatus.MINIMAL
               ? null
               : mc.particleEngine.createParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
         }
      } else {
         return null;
      }
   }

   static ParticleStatus calculateParticleStatus(boolean allowDecrease, Minecraft mc) {
      ParticleStatus particlestatus = (ParticleStatus)mc.options.particles().get();
      if (allowDecrease && particlestatus == ParticleStatus.MINIMAL && mc.level.random.nextInt(10) == 0) {
         particlestatus = ParticleStatus.DECREASED;
      }

      if (particlestatus == ParticleStatus.DECREASED && mc.level.random.nextInt(3) == 0) {
         particlestatus = ParticleStatus.MINIMAL;
      }

      return particlestatus;
   }
}
