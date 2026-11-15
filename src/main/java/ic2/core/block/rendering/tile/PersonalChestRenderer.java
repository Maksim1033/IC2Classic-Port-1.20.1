package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import ic2.core.block.personal.PersonalBlock;
import ic2.core.block.personal.tile.PersonalChestTileEntity;
import ic2.core.block.rendering.models.PersonalChestModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class PersonalChestRenderer implements BlockEntityRenderer<PersonalChestTileEntity> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/models/tile/personal_safe.png");
   PersonalChestModel model = new PersonalChestModel();

   public PersonalChestRenderer(Context context) {
   }

   public void render(
      PersonalChestTileEntity tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      tile.animation.tick();
      matrixStackIn.pushPose();
      matrixStackIn.translate(0.0, 1.0, 1.0);
      matrixStackIn.scale(1.0F, -1.0F, -1.0F);
      matrixStackIn.translate(0.5, 0.5, 0.5);
      matrixStackIn.mulPose(
         com.mojang.math.Axis.YP.rotationDegrees((tile.hasLevel() ? (Direction)tile.getBlockState().getValue(PersonalBlock.FACING) : tile.getFacing()).toYRot())
      );
      matrixStackIn.translate(-0.5, -0.5, -0.5);
      float moveAngle = tile.animation.prevLidAngle + (tile.animation.lidAngle - tile.animation.prevLidAngle) * partialTicks;
      moveAngle = 1.0F - moveAngle;
      moveAngle = 1.0F - moveAngle * moveAngle * moveAngle;
      this.model.render(matrixStackIn, bufferIn.getBuffer(RenderType.entityCutout(TEXTURE)), combinedLightIn, combinedOverlayIn, moveAngle * 3.141593F * 0.65F);
      matrixStackIn.popPose();
   }
}
