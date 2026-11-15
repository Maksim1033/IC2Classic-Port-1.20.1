package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import ic2.core.block.cables.AdvancedComparatorBlock;
import ic2.core.block.cables.AdvancedComparatorTileEntity;
import ic2.core.block.rendering.models.ComparatorModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;

public class AdvancedComparatorRenderer implements BlockEntityRenderer<AdvancedComparatorTileEntity> {
   private static final ResourceLocation COMPARE_OFF = new ResourceLocation("ic2", "textures/models/comparator/compare_off.png");
   private static final ResourceLocation COMPARE_ON = new ResourceLocation("ic2", "textures/models/comparator/compare_on.png");
   private static final ResourceLocation SUBTRACT_OFF = new ResourceLocation("ic2", "textures/models/comparator/subtract_off.png");
   private static final ResourceLocation SUBTRACT_ON = new ResourceLocation("ic2", "textures/models/comparator/subtract_on.png");
   ComparatorModel model = new ComparatorModel();

   public AdvancedComparatorRenderer(Context context) {
   }

   public ResourceLocation getTexture(boolean on, ComparatorMode mode) {
      if (mode == ComparatorMode.COMPARE) {
         return on ? COMPARE_ON : COMPARE_OFF;
      } else {
         return on ? SUBTRACT_ON : SUBTRACT_OFF;
      }
   }

   private static Quaternionf rotDeg(float x, float y, float z) {
      return new Quaternionf().rotationXYZ((float)Math.toRadians(x), (float)Math.toRadians(y), (float)Math.toRadians(z));
   }

   public void render(
      AdvancedComparatorTileEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      ComparatorMode mode;
      boolean active;
      BlockState state = te.getBlockState();
      mode = (ComparatorMode)state.getValue(AdvancedComparatorBlock.MODE);
      Direction facing = (Direction)state.getValue(AdvancedComparatorBlock.FACING);
      Direction rotation = (Direction)state.getValue(AdvancedComparatorBlock.ROTATION);
      active = (Boolean)state.getValue(AdvancedComparatorBlock.POWERED);
      matrixStackIn.pushPose();
      label49:
      switch (facing) {
         case DOWN:
            matrixStackIn.mulPose(rotDeg(0.0F, -rotation.toYRot(), 0.0F));
            switch (rotation.get2DDataValue()) {
               case 0:
                  matrixStackIn.translate(0.0, 0.0, 1.0);
               case 1:
               default:
                  break label49;
               case 2:
                  matrixStackIn.translate(-1.0, 0.0, 0.0);
                  break label49;
               case 3:
                  matrixStackIn.translate(-1.0, 0.0, 1.0);
                  break label49;
            }
         case UP:
            matrixStackIn.mulPose(rotDeg(-180.0F, 0.0F, 0.0F));
            matrixStackIn.translate(0.0, -1.0, -1.0);
            matrixStackIn.mulPose(rotDeg(0.0F, -rotation.toYRot(), 0.0F));
            switch (rotation.get2DDataValue()) {
               case 0:
                  matrixStackIn.translate(1.0, 0.0, 0.0);
                  matrixStackIn.mulPose(rotDeg(0.0F, -180.0F, 0.0F));
               case 1:
               default:
                  break label49;
               case 2:
                  matrixStackIn.translate(0.0, 0.0, -1.0);
                  matrixStackIn.mulPose(rotDeg(0.0F, -180.0F, 0.0F));
                  break label49;
               case 3:
                  matrixStackIn.translate(-1.0, 0.0, 1.0);
                  break label49;
            }
         case NORTH:
            matrixStackIn.mulPose(rotDeg(90.0F, 0.0F, 0.0F));
            switch (rotation.get3DDataValue()) {
               case 1:
                  matrixStackIn.mulPose(rotDeg(0.0F, 180.0F, 0.0F));
                  matrixStackIn.translate(-1.0, 0.0, 1.0);
               case 2:
               case 3:
               default:
                  break label49;
               case 4:
                  matrixStackIn.mulPose(rotDeg(0.0F, -90.0F, 0.0F));
                  matrixStackIn.translate(-1.0, 0.0, 0.0);
                  break label49;
               case 5:
                  matrixStackIn.mulPose(rotDeg(0.0F, 90.0F, 0.0F));
                  matrixStackIn.translate(0.0, 0.0, 1.0);
                  break label49;
            }
         case SOUTH:
            matrixStackIn.mulPose(rotDeg(-90.0F, 0.0F, 0.0F));
            matrixStackIn.translate(0.0, -1.0, 1.0);
            switch (rotation.get3DDataValue()) {
               case 0:
                  matrixStackIn.mulPose(rotDeg(0.0F, 180.0F, 0.0F));
                  matrixStackIn.translate(-1.0, 0.0, 1.0);
                  break label49;
               case 4:
                  matrixStackIn.mulPose(rotDeg(0.0F, -90.0F, 0.0F));
                  matrixStackIn.translate(-1.0, 0.0, 0.0);
                  break label49;
               case 5:
                  matrixStackIn.mulPose(rotDeg(0.0F, 90.0F, 0.0F));
                  matrixStackIn.translate(0.0, 0.0, 1.0);
               default:
                  break label49;
            }
         case EAST:
            matrixStackIn.mulPose(rotDeg(0.0F, 0.0F, 90.0F));
            matrixStackIn.translate(1.0, -1.0, 0.0);
            switch (rotation.get3DDataValue()) {
               case 0:
                  matrixStackIn.mulPose(rotDeg(0.0F, 180.0F, 0.0F));
                  matrixStackIn.translate(1.0, 0.0, -1.0);
               case 1:
                  matrixStackIn.mulPose(rotDeg(0.0F, 90.0F, 0.0F));
                  matrixStackIn.translate(-1.0, 0.0, 0.0);
                  break label49;
               case 2:
                  matrixStackIn.mulPose(rotDeg(0.0F, 180.0F, 0.0F));
                  break label49;
               case 3:
                  matrixStackIn.translate(-1.0, 0.0, 1.0);
               default:
                  break label49;
            }
         case WEST:
            matrixStackIn.mulPose(rotDeg(0.0F, 0.0F, -90.0F));
            switch (rotation.get3DDataValue()) {
               case 0:
                  matrixStackIn.mulPose(rotDeg(0.0F, 180.0F, 0.0F));
                  matrixStackIn.translate(1.0, 0.0, -1.0);
               case 1:
                  matrixStackIn.mulPose(rotDeg(0.0F, -90.0F, 0.0F));
                  matrixStackIn.translate(0.0, 0.0, 1.0);
                  break;
               case 2:
                  matrixStackIn.mulPose(rotDeg(0.0F, 180.0F, 0.0F));
                  break;
               case 3:
                  matrixStackIn.translate(-1.0, 0.0, 1.0);
            }
      }

      this.model.render(matrixStackIn, bufferIn.getBuffer(RenderType.entitySolid(this.getTexture(active, mode))), combinedLightIn, combinedOverlayIn);
      matrixStackIn.popPose();
   }
}
