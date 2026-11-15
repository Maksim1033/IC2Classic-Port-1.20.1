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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PersonalChestModel extends Model {
   ModelPart chest;
   ModelPart door;

   public PersonalChestModel() {
      super(RenderType::entitySolid);
      MeshDefinition mesh = create();
      this.chest = mesh.getRoot().getChild("chest").bake(64, 64);
      this.door = mesh.getRoot().getChild("door").bake(64, 64);
   }

   private static MeshDefinition create() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition fullMesh = mesh.getRoot();
      PartDefinition chest = fullMesh.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.ZERO);
      PartDefinition door = fullMesh.addOrReplaceChild("door", CubeListBuilder.create(), PartPose.ZERO);
      chest.addOrReplaceChild(
         "Right-Wall",
         CubeListBuilder.create().mirror().addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 1.0F),
         PartPose.offsetAndRotation(1.0F, 0.0F, 15.0F, 0.0F, 1.570796F, 0.0F)
      );
      chest.addOrReplaceChild(
         "Left-Wall",
         CubeListBuilder.create().mirror().addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 1.0F),
         PartPose.offsetAndRotation(15.0F, 0.0F, 1.0F, 0.0F, -1.570796F, 0.0F)
      );
      chest.addOrReplaceChild(
         "Back-Wall",
         CubeListBuilder.create().texOffs(1, 1).mirror().addBox(1.0F, 1.0F, 0.0F, 12.0F, 14.0F, 1.0F),
         PartPose.offsetAndRotation(15.0F, 0.0F, 15.0F, 0.0F, 3.141593F, 0.0F)
      );
      chest.addOrReplaceChild(
         "Up-Wall",
         CubeListBuilder.create().texOffs(1, 17).mirror().addBox(1.0F, 0.0F, 0.0F, 12.0F, 14.0F, 1.0F),
         PartPose.offsetAndRotation(1.0F, 0.0F, 15.0F, -1.570796F, 0.0F, 0.0F)
      );
      chest.addOrReplaceChild(
         "Down-Wall",
         CubeListBuilder.create().texOffs(1, 17).mirror().addBox(1.0F, 0.0F, 0.0F, 12.0F, 14.0F, 1.0F),
         PartPose.offsetAndRotation(15.0F, 15.0F, 1.0F, -1.570796F, 3.141593F, 0.0F)
      );
      door.addOrReplaceChild(
         "door", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 12.0F, 14.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return mesh;
   }

   public void renderToBuffer(
      PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha
   ) {
      this.chest.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
      this.door.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
   }

   public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float rotation) {
      this.chest.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
      this.door.yRot = rotation;
      this.door.x = 2.0F;
      this.door.y = 1.0F;
      this.door.z = 2.0F;
      this.door.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
   }
}
