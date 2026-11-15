package ic2.core.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ic2.core.entity.explosion.IC2ExplosiveEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

public class IC2ExplosiveRenderer extends EntityRenderer<IC2ExplosiveEntity> {
   public IC2ExplosiveRenderer(Context context) {
      super(context);
   }

   public void render(IC2ExplosiveEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
      matrixStackIn.pushPose();
      matrixStackIn.translate(0.0, 0.5, 0.0);
      if ((float)entityIn.getFuseTime() - partialTicks + 1.0F < 10.0F) {
         float f = 1.0F - ((float)entityIn.getFuseTime() - partialTicks + 1.0F) / 10.0F;
         f = Mth.clamp(f, 0.0F, 1.0F);
         f *= f;
         f *= f;
         float f1 = 1.0F + f * 0.3F;
         matrixStackIn.scale(f1, f1, f1);
      }

      matrixStackIn.mulPose(Axis.YP.rotationDegrees(-90.0F));
      matrixStackIn.translate(-0.5, -0.5, 0.5);
      matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0F));
      TntMinecartRenderer.renderWhiteSolidBlock(
         Minecraft.getInstance().getBlockRenderer(), entityIn.getState(), matrixStackIn, bufferIn, packedLightIn, entityIn.getFuseTime() / 5 % 2 == 0
      );
      matrixStackIn.popPose();
      super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
   }

   public ResourceLocation getTextureLocation(IC2ExplosiveEntity entity) {
      return InventoryMenu.BLOCK_ATLAS;
   }
}
