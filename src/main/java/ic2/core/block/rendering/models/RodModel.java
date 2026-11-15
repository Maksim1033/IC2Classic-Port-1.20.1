package ic2.core.block.rendering.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.ColorUtils;
import java.util.List;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class RodModel extends Model {
   RodModel.RodInfo[] info = new RodModel.RodInfo[3];

   public RodModel() {
      super(RenderType::entitySolid);
      this.info[0] = create1x1();
      this.info[1] = create2x1();
      this.info[2] = create2x2();
   }

   public static RodModel.RodInfo create1x1() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("frame", addRod(CubeListBuilder.create(), true), PartPose.offset(0.0F, 24.0F, 0.0F));
      root.addOrReplaceChild("uranium", addRod(CubeListBuilder.create(), false), PartPose.offset(0.0F, 24.0F, 0.0F));
      RodModel.RodInfo info = new RodModel.RodInfo();
      ModelPart main = root.bake(32, 16);
      info.setMain(main);
      add(info, main);
      return info;
   }

   public static RodModel.RodInfo create2x1() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition root = meshdefinition.getRoot();
      CubeDeformation formation = CubeDeformation.NONE;
      addRod(root, "left", PartPose.offset(-3.0F, 24.0F, 0.0F));
      addRod(root, "right", PartPose.offset(3.0F, 24.0F, 0.0F));
      root.addOrReplaceChild(
         "connection",
         CubeListBuilder.create()
            .texOffs(20, 4)
            .addBox(-1.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, formation)
            .texOffs(20, 11)
            .addBox(-1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 2.0F, formation),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      RodModel.RodInfo info = new RodModel.RodInfo();
      ModelPart main = root.bake(32, 16);
      info.setMain(main);
      add(info, main.getChild("left"));
      add(info, main.getChild("right"));
      info.addRods(main.getChild("connection"));
      return info;
   }

   public static RodModel.RodInfo create2x2() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition root = meshdefinition.getRoot();
      CubeDeformation formation = CubeDeformation.NONE;
      PartDefinition front = root.addOrReplaceChild("front", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 3.0F));
      addRod(front, "right", PartPose.offset(3.0F, 0.0F, 0.0F));
      addRod(front, "left", PartPose.offset(-3.0F, 0.0F, 0.0F));
      PartDefinition back = root.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, -3.0F));
      addRod(back, "right", PartPose.offset(3.0F, 0.0F, 0.0F));
      addRod(back, "left", PartPose.offset(-3.0F, 0.0F, 0.0F));
      root.addOrReplaceChild(
         "connection",
         CubeListBuilder.create()
            .texOffs(20, 4)
            .addBox(-1.0F, -3.0F, 2.0F, 2.0F, 2.0F, 2.0F, formation)
            .texOffs(20, 11)
            .addBox(-1.0F, -14.0F, 2.0F, 2.0F, 2.0F, 2.0F, formation)
            .texOffs(20, 11)
            .addBox(-1.0F, -14.0F, -4.0F, 2.0F, 2.0F, 2.0F, formation)
            .texOffs(20, 4)
            .addBox(-1.0F, -3.0F, -4.0F, 2.0F, 2.0F, 2.0F, formation)
            .texOffs(20, 6)
            .addBox(2.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, formation)
            .texOffs(20, 6)
            .addBox(-4.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, formation)
            .texOffs(20, 12)
            .addBox(2.0F, -14.0F, -1.0F, 2.0F, 2.0F, 2.0F, formation)
            .texOffs(20, 12)
            .addBox(-4.0F, -14.0F, -1.0F, 2.0F, 2.0F, 2.0F, formation),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      RodModel.RodInfo info = new RodModel.RodInfo();
      ModelPart main = root.bake(32, 16);
      info.setMain(main);
      add(info, main.getChild("front").getChild("left"));
      add(info, main.getChild("front").getChild("right"));
      add(info, main.getChild("back").getChild("left"));
      add(info, main.getChild("back").getChild("right"));
      info.addRods(main.getChild("connection"));
      return info;
   }

   private static void add(RodModel.RodInfo info, ModelPart part) {
      info.addRods(part.getChild("frame"));
      info.addUranium(part.getChild("uranium"));
   }

   private static void addRod(PartDefinition root, String name, PartPose pose) {
      PartDefinition right = root.addOrReplaceChild(name, CubeListBuilder.create(), pose);
      right.addOrReplaceChild("frame", addRod(CubeListBuilder.create(), true), PartPose.ZERO);
      right.addOrReplaceChild("uranium", addRod(CubeListBuilder.create(), false), PartPose.ZERO);
   }

   private static CubeListBuilder addRod(CubeListBuilder builder, boolean frame) {
      CubeDeformation formation = CubeDeformation.NONE;
      if (frame) {
         builder.texOffs(16, 3)
            .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, formation)
            .texOffs(16, 0)
            .addBox(-1.0F, -16.0F, -1.0F, 2.0F, 1.0F, 2.0F, formation)
            .texOffs(16, 4)
            .addBox(-2.0F, -3.0F, -2.0F, 4.0F, 2.0F, 4.0F, formation)
            .texOffs(16, 10)
            .addBox(-2.0F, -15.0F, -2.0F, 4.0F, 2.0F, 4.0F, formation)
            .texOffs(0, 0)
            .addBox(-2.0F, -13.0F, 1.0F, 1.0F, 10.0F, 1.0F, formation)
            .texOffs(0, 0)
            .addBox(-2.0F, -13.0F, -2.0F, 1.0F, 10.0F, 1.0F, formation)
            .texOffs(0, 0)
            .addBox(1.0F, -13.0F, -2.0F, 1.0F, 10.0F, 1.0F, formation)
            .texOffs(0, 0)
            .addBox(1.0F, -13.0F, 1.0F, 1.0F, 10.0F, 1.0F, formation);
      } else {
         builder.texOffs(4, 0)
            .addBox(1.0F, -13.0F, -1.0F, 1.0F, 10.0F, 2.0F, formation)
            .texOffs(10, 0)
            .addBox(-1.0F, -13.0F, -2.0F, 2.0F, 10.0F, 1.0F, formation)
            .texOffs(10, 0)
            .addBox(-1.0F, -13.0F, 1.0F, 2.0F, 10.0F, 1.0F, formation)
            .texOffs(4, 0)
            .addBox(-2.0F, -13.0F, -1.0F, 1.0F, 10.0F, 2.0F, formation);
      }

      return builder;
   }

   public void renderToBuffer(
      PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha
   ) {
      this.info[this.getRods(0)].render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, packedOverlayIn);
   }

   public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color, int rods) {
      this.info[this.getRods(rods)].render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
   }

   private int getRods(int value) {
      switch (value) {
         case 2:
            return 1;
         case 4:
            return 2;
         default:
            return 0;
      }
   }

   public static class RodInfo {
      ModelPart mainModel;
      List<ModelPart> rods = CollectionUtils.<ModelPart>createList();
      List<ModelPart> uranium = CollectionUtils.<ModelPart>createList();

      public void addRods(ModelPart part) {
         this.rods.add(part);
      }

      public void addUranium(ModelPart part) {
         this.uranium.add(part);
      }

      public void setMain(ModelPart part) {
         this.mainModel = part;
      }

      public void render(PoseStack matrix, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {
         this.toggle(this.uranium, false);
         this.mainModel.render(matrix, bufferIn, packedLightIn, packedOverlayIn);
         this.toggle(this.uranium, true);
         this.toggle(this.rods, false);
         this.mainModel
            .render(
               matrix, bufferIn, packedLightIn, packedOverlayIn, ColorUtils.getFloatR(color), ColorUtils.getFloatG(color), ColorUtils.getFloatB(color), 1.0F
            );
         this.toggle(this.rods, true);
      }

      private void toggle(List<ModelPart> parts, boolean value) {
         for (ModelPart part : parts) {
            part.visible = value;
         }
      }
   }
}
