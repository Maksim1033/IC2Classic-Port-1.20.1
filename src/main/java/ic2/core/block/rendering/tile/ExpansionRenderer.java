package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import ic2.core.IC2;
import ic2.core.block.machines.tiles.nv.TankExpansionTileEntity;
import ic2.core.fluid.IC2Tank;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.misc.IC2RenderTypes;
import ic2.core.utils.helpers.FluidHelper;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class ExpansionRenderer implements BlockEntityRenderer<TankExpansionTileEntity> {
   public ExpansionRenderer(Context context) {
   }

   public void render(
      TankExpansionTileEntity tank, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      if (IC2.CONFIG.fancyFluidExpansion.get()) {
         int lightValue = LevelRenderer.getLightColor(tank.getLevel(), tank.getBlockPos().above());
         VertexConsumer builder = bufferIn.getBuffer(IC2RenderTypes.FLUID_RENDER);
         BlockState state = tank.getBlockState();
         this.renderCross(builder, ((IBlockModel)state.getBlock()).getSpriteForState(state, Direction.DOWN), lightValue, matrixStackIn);

         for (int m = 0; m < 4; m++) {
            IC2Tank subTank = tank.getTank(m);
            this.render(
               builder, subTank.getFluid(), subTank.getCapacity(), Direction.from2DDataValue(m + tank.getFacing().get2DDataValue()), lightValue, matrixStackIn
            );
         }
      }
   }

   protected void renderCross(VertexConsumer buffer, TextureAtlasSprite sprite, int light, PoseStack stack) {
      float min = 0.002F;
      float max = 0.998F;
      Matrix4f mat = stack.last().pose();
      buffer.vertex(mat, min, min, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, max, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, max, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, min, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, min, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, max, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, min, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, max, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, max, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, min, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, max, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, min, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, min, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, max, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, min, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, min, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, max, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, max, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, min, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, min, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, max, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, min, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV0())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, max, max, min)
         .color(255, 255, 255, 255)
         .uv(sprite.getU1(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
      buffer.vertex(mat, min, max, max)
         .color(255, 255, 255, 255)
         .uv(sprite.getU0(), sprite.getV1())
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .endVertex();
   }

   protected void render(VertexConsumer buffer, FluidStack stack, int max, Direction dir, int light, PoseStack matrixStackIn) {
      if (!stack.isEmpty()) {
         float minY = 0.02F;
         float maxY = 0.02F + (float)stack.getAmount() / (float)max * 0.96F;
         TextureAtlasSprite sprite = FluidHelper.getTexture(stack, false);
         int color = FluidHelper.getFluidColor(stack);
         int alpha = color >> 24 & 0xFF;
         int red = color >> 16 & 0xFF;
         int green = color >> 8 & 0xFF;
         int blue = color & 0xFF;
         float minBounds = 0.001F;
         float maxBounds = 0.999F;
         Matrix4f mat = matrixStackIn.last().pose();
         switch (dir) {
            case EAST:
               buffer.vertex(mat, 0.5F, maxY, 0.5F)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU(8.0), sprite.getV(8.0))
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, minY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, minY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, minY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               break;
            case NORTH:
               buffer.vertex(mat, minBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, 0.5F, maxY, 0.5F)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU(8.0), sprite.getV(8.0))
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, minY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, minY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, minY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               break;
            case SOUTH:
               buffer.vertex(mat, 0.5F, maxY, 0.5F)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU(8.0), sprite.getV(8.0))
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, minY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, minY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, minY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, maxBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               break;
            case WEST:
               buffer.vertex(mat, minBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, 0.5F, maxY, 0.5F)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU(8.0), sprite.getV(8.0))
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, minY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, maxY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, minY, minBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV0())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, minY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU0(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
               buffer.vertex(mat, minBounds, maxY, maxBounds)
                  .color(red, green, blue, alpha)
                  .uv(sprite.getU1(), sprite.getV1())
                  .overlayCoords(OverlayTexture.NO_OVERLAY)
                  .uv2(light)
                  .endVertex();
         }
      }
   }
}
