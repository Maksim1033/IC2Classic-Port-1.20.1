package ic2.core.block.rendering.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class ComparatorModel extends Model {
   ModelPart model = createPart();

   public ComparatorModel() {
      super(RenderType::entitySolid);
   }

   private static ModelPart createPart() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition part = mesh.getRoot();
      part.addOrReplaceChild(
         "base",
         CubeListBuilder.create().mirror().addBox(0.0F, 0.0F, 0.0F, 16.0F, 2.0F, 16.0F),
         PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 3.15F, 0.0F, 0.0F)
      );
      part.addOrReplaceChild(
         "left",
         CubeListBuilder.create().texOffs(0, 20).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F),
         PartPose.offsetAndRotation(4.0F, 6.0F, -3.0F, 3.15F, 0.0F, 0.0F)
      );
      part.addOrReplaceChild(
         "right",
         CubeListBuilder.create().texOffs(10, 20).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F),
         PartPose.offsetAndRotation(10.0F, 6.0F, -3.0F, -3.15F, 0.0F, 0.0F)
      );
      part.addOrReplaceChild(
         "front",
         CubeListBuilder.create().texOffs(20, 20).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(7.0F, 4.0F, -12.0F, -3.15F, 0.0F, 0.0F)
      );
      return part.bake(64, 32);
   }

   public void renderToBuffer(
      PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha
   ) {
      this.model.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
   }

   public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn) {
      this.model.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
   }
}
