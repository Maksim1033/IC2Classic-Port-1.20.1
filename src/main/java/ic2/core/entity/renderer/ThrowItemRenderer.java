package ic2.core.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class ThrowItemRenderer extends EntityRenderer<Entity> {
   public ThrowItemRenderer(Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(Entity entity) {
      return InventoryMenu.BLOCK_ATLAS;
   }

   public void render(Entity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
      if (entityIn instanceof IRenderableItem) {
         ItemStack stack = ((IRenderableItem)entityIn).getStack();
         matrixStackIn.pushPose();
         matrixStackIn.scale(0.5F, 0.5F, 0.5F);
         matrixStackIn.mulPose(this.entityRenderDispatcher.cameraOrientation());
         matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0F));
         Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(stack, ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, entityIn.level(), entityIn.getId());
         matrixStackIn.popPose();
      }

      super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
   }
}
