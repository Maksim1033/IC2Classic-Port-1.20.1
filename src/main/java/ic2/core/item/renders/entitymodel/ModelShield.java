package ic2.core.item.renders.entitymodel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class ModelShield extends Model {
   ModelPart shield = create();

   private static ModelPart create() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition part = mesh.getRoot();
      part.addOrReplaceChild("Shield1", CubeListBuilder.create().texOffs(4, 0).mirror().addBox(-5.0F, -12.0F, -2.0F, 10.0F, 1.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Shield2", CubeListBuilder.create().texOffs(2, 2).mirror().addBox(-6.0F, -11.0F, -2.0F, 12.0F, 1.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Shield3", CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-7.0F, -10.0F, -2.0F, 14.0F, 10.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Shield4", CubeListBuilder.create().texOffs(2, 15).mirror().addBox(-6.0F, 0.0F, -2.0F, 12.0F, 6.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Shield5", CubeListBuilder.create().texOffs(4, 22).mirror().addBox(-5.0F, 6.0F, -2.0F, 10.0F, 4.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Shield6", CubeListBuilder.create().texOffs(6, 27).mirror().addBox(-4.0F, 10.0F, -2.0F, 8.0F, 2.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Shield7", CubeListBuilder.create().texOffs(8, 30).mirror().addBox(-3.0F, 12.0F, -2.0F, 6.0F, 2.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Shield8", CubeListBuilder.create().texOffs(10, 33).mirror().addBox(-2.0F, 14.0F, -2.0F, 4.0F, 1.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Shield9", CubeListBuilder.create().texOffs(12, 35).mirror().addBox(-1.0F, 15.0F, -2.0F, 2.0F, 1.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Handle1", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(-1.0F, -3.0F, 0.0F, 2.0F, 1.0F, 4.0F), PartPose.ZERO);
      part.addOrReplaceChild("Handle2", CubeListBuilder.create().texOffs(34, 12).mirror().addBox(-1.0F, 2.0F, 0.0F, 2.0F, 1.0F, 4.0F), PartPose.ZERO);
      part.addOrReplaceChild("Handle3", CubeListBuilder.create().texOffs(34, 5).mirror().addBox(-1.0F, -3.0F, 4.0F, 2.0F, 6.0F, 1.0F), PartPose.ZERO);
      part.addOrReplaceChild("Handle4", CubeListBuilder.create().texOffs(34, 5).mirror().addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 1.0F), PartPose.ZERO);
      return part.bake(64, 64);
   }

   public ModelShield() {
      super(RenderType::entitySolid);
   }

   public void renderToBuffer(
      PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha
   ) {
      this.shield.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
   }

   public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn) {
      this.shield.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
   }
}
