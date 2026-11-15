package ic2.core.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class CrossRenderer extends EntityRenderer<Entity> {
   ResourceLocation texture;

   public CrossRenderer(Context context, ResourceLocation texture) {
      super(context);
      this.texture = texture;
   }

   public void render(Entity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
      matrixStackIn.pushPose();
      matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot()) - 90.0F));
      matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
      matrixStackIn.mulPose(Axis.XP.rotationDegrees(45.0F));
      matrixStackIn.scale(0.05625F, 0.05625F, 0.05625F);
      matrixStackIn.translate(-4.0, 0.0, 0.0);
      VertexConsumer iVertexBuilder = bufferIn.getBuffer(RenderType.entityCutout(this.getTextureLocation(entityIn)));
      Pose matrixStackEntry = matrixStackIn.last();
      Matrix4f matrix4f = matrixStackEntry.pose();
      Matrix3f matrix3f = matrixStackEntry.normal();
      this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, packedLightIn);
      this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, packedLightIn);
      this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, packedLightIn);
      this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, packedLightIn);
      this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, packedLightIn);
      this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, packedLightIn);
      this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, packedLightIn);
      this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, packedLightIn);

      for (int j = 0; j < 4; j++) {
         matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0F));
         this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, packedLightIn);
         this.drawVertex(matrix4f, matrix3f, iVertexBuilder, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, packedLightIn);
         this.drawVertex(matrix4f, matrix3f, iVertexBuilder, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, packedLightIn);
         this.drawVertex(matrix4f, matrix3f, iVertexBuilder, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, packedLightIn);
      }

      matrixStackIn.popPose();
      super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
   }

   public void drawVertex(
      Matrix4f matrix,
      Matrix3f normals,
      VertexConsumer vertexBuilder,
      int offsetX,
      int offsetY,
      int offsetZ,
      float textureX,
      float textureY,
      int p_229039_9_,
      int p_229039_10_,
      int p_229039_11_,
      int packedLightIn
   ) {
      vertexBuilder.vertex(matrix, (float)offsetX, (float)offsetY, (float)offsetZ)
         .color(255, 255, 255, 255)
         .uv(textureX, textureY)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(packedLightIn)
         .normal(normals, (float)p_229039_9_, (float)p_229039_11_, (float)p_229039_10_)
         .endVertex();
   }

   public ResourceLocation getTextureLocation(Entity entity) {
      return this.texture;
   }
}
