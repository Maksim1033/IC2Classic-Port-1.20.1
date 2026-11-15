package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import ic2.api.util.DirectionList;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.storage.tiles.tank.TankTileEntity;
import ic2.core.block.storage.tiles.tank.ValveTileEntity;
import ic2.core.fluid.IC2Tank;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.platform.rendering.misc.IC2RenderTypes;
import ic2.core.platform.rendering.misc.UVHelper;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.math.geometry.Box;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class TankRenderer implements BlockEntityRenderer<TankTileEntity> {
   public TankRenderer(Context context) {
   }

   public boolean shouldRenderOffScreen(TankTileEntity p_188185_1_) {
      return true;
   }

   public void render(TankTileEntity tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
      if (tile.isValid) {
         IC2Tank tank = tile.tank;
         if (tank.getCapacity() > 0 && tank.getFluidAmount() > 0) {
            FluidStack stack = tank.getFluid();
            if (!stack.isEmpty()) {
               VertexConsumer builder = bufferIn.getBuffer(IC2RenderTypes.FLUID_RENDER);
               int lightValue = LevelRenderer.getLightColor(tile.getLevel(), tile.getCenter());
               float filled = (float)tank.getFluidAmount() / (float)tank.getCapacity();
               UVHelper notFlowing = UVHelper.fromSprite(FluidHelper.getTexture(stack, false));
               if (stack.getFluid().is(Fluids.GASEOUS)) {
                  int color = FluidHelper.getFluidColor(stack);
                  int alpha = color >> 24 & 0xFF;
                  int sub = (int)((float)alpha * 0.5F);
                  color = color & 16777215 | sub + (int)((float)(alpha - sub) * filled) << 24;
                  RenderShapes.renderColorTextureLightCube(tile.getBox(1.0F), notFlowing, color, OverlayTexture.NO_OVERLAY, lightValue, builder, matrixStackIn);
               } else {
                  int color = FluidHelper.getFluidColor(stack);
                  AABB renderBounds = tile.getBox(filled);
                  Box box = tile.getBox();
                  float minimum = (float)renderBounds.maxY;
                  RenderShapes.renderColorTextureLightCube(renderBounds, notFlowing, color, OverlayTexture.NO_OVERLAY, lightValue, builder, matrixStackIn);
                  UVHelper flowing = UVHelper.fromSpriteScaled(FluidHelper.getTexture(stack, true), 3.0F, 0.0F, 90);

                  for (BaseLinkingTileEntity subTile : tile.children.getClientTiles()) {
                     if (subTile instanceof ValveTileEntity) {
                        ValveTileEntity valve = (ValveTileEntity)subTile;
                        if (!(minimum >= (float)subTile.getBlockPos().getY() + 0.5F) && valve.render.shouldRender()) {
                           Direction dir = this.getDir(valve.getBlockPos(), box);
                           if (dir != null) {
                              BlockPos valvePos = valve.getBlockPos().relative(dir);
                              float minX = (float)(valvePos.getX() - tile.getBlockPos().getX()) + 0.25F;
                              float minY = (float)(valvePos.getY() - tile.getBlockPos().getY()) + 0.25F;
                              float minZ = (float)(valvePos.getZ() - tile.getBlockPos().getZ()) + 0.25F;
                              AABB subBox = new AABB(
                                    (double)minX, renderBounds.maxY, (double)minZ, (double)(minX + 0.5F), (double)(minY + 0.5F), (double)(minZ + 0.5F)
                                 )
                                 .move(Vec3.atLowerCornerOf(dir.getOpposite().getNormal()).multiply(0.25, 0.25, 0.25));
                              this.render(
                                 subBox,
                                 (T, D) -> {
                                    if (D.contains(Direction.UP)) {
                                       D = D.remove(Direction.UP);
                                       RenderShapes.renderColorTextureLightQuad(
                                          Direction.UP, T, notFlowing, color, OverlayTexture.NO_OVERLAY, lightValue, builder, matrixStackIn
                                       );
                                    }

                                    if (D.contains(Direction.DOWN)) {
                                       D = D.remove(Direction.DOWN);
                                       RenderShapes.renderColorTextureLightQuad(
                                          Direction.DOWN, T, notFlowing, color, OverlayTexture.NO_OVERLAY, lightValue, builder, matrixStackIn
                                       );
                                    }

                                    RenderShapes.renderColorTextureLightCube(
                                       D, T, flowing, color, OverlayTexture.NO_OVERLAY, lightValue, builder, matrixStackIn
                                    );
                                 }
                              );
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void render(AABB box, BiConsumer<AABB, DirectionList> listener) {
      float min = (float)box.minY;
      float max = (float)box.maxY;
      int start = Mth.floor(box.minY);
      int end = (int)box.maxY;
      int i = Mth.floor(box.minY);

      for (int m = Mth.ceil(max); i < m; i++) {
         float minY = Mth.clamp((float)i, min, max);
         float maxY = Mth.clamp((float)(i + 1), min, max);
         if (Float.compare(minY, maxY) == 0) {
            return;
         }

         listener.accept(
            new AABB(box.minX, (double)minY, box.minZ, box.maxX, (double)maxY, box.maxZ),
            i == start ? DirectionList.UP.invert() : (i == end ? DirectionList.DOWN.invert() : DirectionList.HORIZONTAL)
         );
      }
   }

   public Direction getDir(BlockPos pos, Box box) {
      for (Direction dir : DirectionList.DOWN.invert()) {
         if (!box.intersectsWith(pos.relative(dir))) {
            return dir.getOpposite();
         }
      }

      return null;
   }

   public static void renderColorTextureLightCube(
      DirectionList directions, AABB box, TextureAtlasSprite sprite, int color, int overlay, int light, VertexConsumer builder, PoseStack stack
   ) {
      for (Direction dir : directions) {
         renderColorTextureLightQuad(
            dir,
            (float)box.minX,
            (float)box.minY,
            (float)box.minZ,
            (float)box.maxX,
            (float)box.maxY,
            (float)box.maxZ,
            sprite,
            color,
            overlay,
            light,
            builder,
            stack
         );
      }
   }

   public static void renderColorTextureLightQuad(
      Direction dir, AABB box, TextureAtlasSprite sprite, int color, int overlay, int light, VertexConsumer builder, PoseStack stack
   ) {
      renderColorTextureLightQuad(
         dir,
         (float)box.minX,
         (float)box.minY,
         (float)box.minZ,
         (float)box.maxX,
         (float)box.maxY,
         (float)box.maxZ,
         sprite,
         color,
         overlay,
         light,
         builder,
         stack
      );
   }

   public static void renderColorTextureLightQuad(
      Direction dir,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      TextureAtlasSprite sprite,
      int color,
      int overlay,
      int light,
      VertexConsumer vertexBuffer,
      PoseStack stack
   ) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      Matrix4f mat = stack.last().pose();
      switch (dir) {
         case WEST:
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            break;
         case EAST:
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            break;
         case DOWN:
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            break;
         case UP:
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            break;
         case NORTH:
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            break;
         case SOUTH:
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay).uv2(light).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).overlayCoords(overlay).uv2(light).endVertex();
      }
   }
}
