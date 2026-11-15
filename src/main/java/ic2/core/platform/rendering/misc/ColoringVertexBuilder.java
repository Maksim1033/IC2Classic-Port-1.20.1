package ic2.core.platform.rendering.misc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class ColoringVertexBuilder implements VertexConsumer {
   VertexConsumer builder;
   int customAlpha;

   public ColoringVertexBuilder(VertexConsumer builder, int customAlpha) {
      this.builder = builder;
      this.customAlpha = customAlpha;
   }

   public VertexConsumer vertex(double p_225582_1_, double p_225582_3_, double p_225582_5_) {
      this.builder.vertex(p_225582_1_, p_225582_3_, p_225582_5_);
      return this;
   }

   public VertexConsumer color(int p_225586_1_, int p_225586_2_, int p_225586_3_, int p_225586_4_) {
      this.builder.color(p_225586_1_, p_225586_2_, p_225586_3_, this.customAlpha);
      return this;
   }

   public VertexConsumer uv(float p_225583_1_, float p_225583_2_) {
      this.builder.uv(p_225583_1_, p_225583_2_);
      return this;
   }

   public VertexConsumer overlayCoords(int p_225585_1_, int p_225585_2_) {
      this.builder.overlayCoords(p_225585_1_, p_225585_2_);
      return this;
   }

   public VertexConsumer uv2(int p_225587_1_, int p_225587_2_) {
      this.builder.uv2(p_225587_1_, p_225587_2_);
      return this;
   }

   public VertexConsumer normal(float p_225584_1_, float p_225584_2_, float p_225584_3_) {
      this.builder.normal(p_225584_1_, p_225584_2_, p_225584_3_);
      return this;
   }

   public void endVertex() {
      this.builder.endVertex();
   }

   @Override
   public void putBulkData(
      Pose p_227890_1_,
      BakedQuad p_227890_2_,
      float[] p_227890_3_,
      float p_227890_4_,
      float p_227890_5_,
      float p_227890_6_,
      int[] p_227890_7_,
      int p_227890_8_,
      boolean p_227890_9_
   ) {
      this.builder.putBulkData(
         p_227890_1_, p_227890_2_, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, p_227890_4_, p_227890_5_, p_227890_6_, (float)this.customAlpha / 255.0F, p_227890_7_, p_227890_8_, p_227890_9_
      );
   }

   @Override
   public void putBulkData(
      Pose p_227890_1_,
      BakedQuad p_227890_2_,
      float[] p_227890_3_,
      float p_227890_4_,
      float p_227890_5_,
      float p_227890_6_,
      float alpha,
      int[] p_227890_7_,
      int p_227890_8_,
      boolean p_227890_9_
   ) {
      this.builder.putBulkData(
         p_227890_1_, p_227890_2_, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, p_227890_4_, p_227890_5_, p_227890_6_, (float)this.customAlpha / 255.0F, p_227890_7_, p_227890_8_, p_227890_9_
      );
   }

   public void defaultColor(int r, int g, int b, int a) {
      this.builder.defaultColor(r, g, b, a);
   }

   public void unsetDefaultColor() {
      this.builder.unsetDefaultColor();
   }
}
