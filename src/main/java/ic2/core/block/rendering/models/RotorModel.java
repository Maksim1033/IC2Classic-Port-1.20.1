package ic2.core.block.rendering.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class RotorModel extends Model {
   ModelPart shaft;
   ModelPart blade;

   public RotorModel(int size) {
      super(RenderType::entityCutout);
      size *= 8;
      size += 2;
      MeshDefinition mesh = create(size);
      this.shaft = mesh.getRoot().getChild("shaft").bake(64, 128);
      this.blade = mesh.getRoot().getChild("blade").bake(64, 128);
   }

   private static MeshDefinition create(int size) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition part = mesh.getRoot();
      PartDefinition shaft = part.addOrReplaceChild("shaft", CubeListBuilder.create(), PartPose.ZERO);
      PartDefinition blade = part.addOrReplaceChild("blade", CubeListBuilder.create(), PartPose.ZERO);
      shaft.addOrReplaceChild(
         "shaft", CubeListBuilder.create().texOffs(29, 0).addBox(0.0F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F), PartPose.offset(-14.0F, 0.0F, 0.0F)
      );
      blade.addOrReplaceChild(
         "wing", CubeListBuilder.create().texOffs(48, 14).addBox(-2.0F, 2.0F, -2.0F, 4.0F, (float)size + 12.0F, 4.0F), PartPose.offset(-12.0F, 0.0F, 0.0F)
      );
      blade.addOrReplaceChild("fan", CubeListBuilder.create().addBox(-1.0F, 14.0F, -18.0F, 2.0F, (float)size, 16.0F), PartPose.offset(-12.0F, 0.0F, 0.0F));
      return mesh;
   }

   public void renderToBuffer(
      PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha
   ) {
      this.shaft.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

      for (int i = 0; i < 4; i++) {
         float value = (float)(90 * i);
         matrixStackIn.mulPose(com.mojang.math.Axis.XP.rotationDegrees(value));
         this.blade.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
      }
   }
}
