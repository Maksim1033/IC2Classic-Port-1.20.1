package ic2.core.platform.rendering;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.misc.UVHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderShapes {
   public static void drawSelectionBox(
      PoseStack matrixStackIn, VertexConsumer bufferIn, Entity entityIn, double xIn, double yIn, double zIn, BlockPos blockPosIn, BlockState blockStateIn
   ) {
      drawShape(
         matrixStackIn,
         bufferIn,
         blockStateIn.getShape(entityIn.level(), blockPosIn, CollisionContext.of(entityIn)),
         (double)blockPosIn.getX() - xIn,
         (double)blockPosIn.getY() - yIn,
         (double)blockPosIn.getZ() - zIn,
         0.0F,
         0.0F,
         0.0F,
         0.4F
      );
   }

   public static void drawShape(
      PoseStack matrixStackIn, VertexConsumer bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha
   ) {
      Matrix4f matrix4f = matrixStackIn.last().pose();
      Matrix3f matrix3f = matrixStackIn.last().normal();
      shapeIn.forAllEdges(
         (minX, minY, minZ, maxX, maxY, maxZ) -> {
            Vector3f normal = new Vector3f((float)(maxX - minX), (float)(maxY - minY), (float)(maxZ - minZ));
            normal.normalize();
            bufferIn.vertex(matrix4f, (float)(minX + xIn), (float)(minY + yIn), (float)(minZ + zIn))
               .color(red, green, blue, alpha)
               .normal(matrix3f, normal.x(), normal.y(), normal.z())
               .endVertex();
            bufferIn.vertex(matrix4f, (float)(maxX + xIn), (float)(maxY + yIn), (float)(maxZ + zIn))
               .color(red, green, blue, alpha)
               .normal(matrix3f, normal.x(), normal.y(), normal.z())
               .endVertex();
         }
      );
   }

   public static void renderInvertedColorCube(AABB box, int color, VertexConsumer builder, PoseStack stack) {
      renderInvertedColorCube(DirectionList.ALL, box, color, builder, stack);
   }

   public static void renderInvertedColorCube(DirectionList directions, AABB box, int color, VertexConsumer builder, PoseStack stack) {
      for (Direction dir : directions) {
         renderInvertedColorCube(dir, box, color, builder, stack);
      }
   }

   public static void renderInvertedColorCube(Direction dir, AABB box, int color, VertexConsumer builder, PoseStack stack) {
      renderColorQuad(dir, (float)box.maxX, (float)box.maxY, (float)box.maxZ, (float)box.minX, (float)box.minY, (float)box.minZ, color, builder, stack);
   }

   public static void renderColorCube(AABB box, int color, VertexConsumer builder, PoseStack stack) {
      renderColorCube(DirectionList.ALL, box, color, builder, stack);
   }

   public static void renderColorCube(DirectionList directions, AABB box, int color, VertexConsumer builder, PoseStack stack) {
      for (Direction dir : directions) {
         renderColorQuad(dir, box, color, builder, stack);
      }
   }

   public static void renderColorQuad(Direction dir, AABB box, int color, VertexConsumer builder, PoseStack stack) {
      renderColorQuad(dir, (float)box.minX, (float)box.minY, (float)box.minZ, (float)box.maxX, (float)box.maxY, (float)box.maxZ, color, builder, stack);
   }

   public static void renderColorQuad(
      Direction dir, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, VertexConsumer vertexBuffer, PoseStack stack
   ) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      Matrix4f mat = stack.last().pose();
      switch (dir) {
         case WEST:
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
            break;
         case EAST:
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
            break;
         case DOWN:
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
            break;
         case UP:
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
            break;
         case NORTH:
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
            break;
         case SOUTH:
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
      }
   }

   public static void renderTextureCube(AABB box, UVHelper sprite, VertexConsumer builder, PoseStack stack) {
      renderTextureCube(DirectionList.ALL, box, sprite, builder, stack);
   }

   public static void renderTextureCube(DirectionList directions, AABB box, UVHelper sprite, VertexConsumer builder, PoseStack stack) {
      for (Direction dir : directions) {
         renderTextureQuad(dir, box, sprite, builder, stack);
      }
   }

   public static void renderTextureQuad(Direction dir, AABB box, UVHelper sprite, VertexConsumer builder, PoseStack stack) {
      renderTextureQuad(dir, (float)box.minX, (float)box.minY, (float)box.minZ, (float)box.maxX, (float)box.maxY, (float)box.maxZ, sprite, builder, stack);
   }

   public static void renderTextureQuad(
      Direction dir, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, UVHelper sprite, VertexConsumer vertexBuffer, PoseStack stack
   ) {
      Matrix4f mat = stack.last().pose();
      if (dir == Direction.NORTH) {
         sprite.rotate(180);
      }

      switch (dir) {
         case WEST:
            vertexBuffer.vertex(mat, minX, maxY, maxZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case EAST:
            vertexBuffer.vertex(mat, maxX, maxY, minZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case DOWN:
            vertexBuffer.vertex(mat, maxX, minY, maxZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case UP:
            vertexBuffer.vertex(mat, minX, maxY, minZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case NORTH:
            vertexBuffer.vertex(mat, maxX, minY, minZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case SOUTH:
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
      }

      if (dir == Direction.NORTH) {
         sprite.rotate(180);
      }
   }

   public static void renderColorTextureCube(AABB box, UVHelper sprite, int color, VertexConsumer builder, PoseStack stack) {
      renderColorTextureCube(DirectionList.ALL, box, sprite, color, builder, stack);
   }

   public static void renderColorTextureCube(DirectionList directions, AABB box, UVHelper sprite, int color, VertexConsumer builder, PoseStack stack) {
      for (Direction dir : directions) {
         renderColorTextureQuad(dir, box, sprite, color, builder, stack);
      }
   }

   public static void renderColorTextureQuad(Direction dir, AABB box, UVHelper sprite, int color, VertexConsumer builder, PoseStack stack) {
      renderColorTextureQuad(
         dir, (float)box.minX, (float)box.minY, (float)box.minZ, (float)box.maxX, (float)box.maxY, (float)box.maxZ, sprite, color, builder, stack
      );
   }

   public static void renderColorTextureQuad(
      Direction dir,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      UVHelper sprite,
      int color,
      VertexConsumer vertexBuffer,
      PoseStack stack
   ) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      Matrix4f mat = stack.last().pose();
      if (dir == Direction.NORTH) {
         sprite.rotate(180);
      }

      switch (dir) {
         case WEST:
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case EAST:
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case DOWN:
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case UP:
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case NORTH:
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            break;
         case SOUTH:
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(1), sprite.getVertexV(1)).endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(2), sprite.getVertexV(2)).endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(3), sprite.getVertexV(3)).endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).uv(sprite.getVertexU(0), sprite.getVertexV(0)).endVertex();
      }

      if (dir == Direction.NORTH) {
         sprite.rotate(180);
      }
   }

   public static void renderColorTextureLightCube(AABB box, UVHelper sprite, int color, int overlay, int light, VertexConsumer builder, PoseStack stack) {
      renderColorTextureLightCube(DirectionList.ALL, box, sprite, color, overlay, light, builder, stack);
   }

   public static void renderColorTextureLightCube(
      DirectionList directions, AABB box, UVHelper sprite, int color, int overlay, int light, VertexConsumer builder, PoseStack stack
   ) {
      for (Direction dir : directions) {
         renderColorTextureLightQuad(dir, box, sprite, color, overlay, light, builder, stack);
      }
   }

   public static void renderColorTextureLightQuad(
      Direction dir, AABB box, UVHelper sprite, int color, int overlay, int light, VertexConsumer builder, PoseStack stack
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
      UVHelper sprite,
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
      if (dir == Direction.NORTH) {
         sprite.rotate(180);
      }

      switch (dir) {
         case WEST:
            vertexBuffer.vertex(mat, minX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(1), sprite.getVertexV(1))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(3), sprite.getVertexV(3))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            break;
         case EAST:
            vertexBuffer.vertex(mat, maxX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(1), sprite.getVertexV(1))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(3), sprite.getVertexV(3))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            break;
         case DOWN:
            vertexBuffer.vertex(mat, maxX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(1), sprite.getVertexV(1))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(3), sprite.getVertexV(3))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            break;
         case UP:
            vertexBuffer.vertex(mat, minX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(1), sprite.getVertexV(1))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(3), sprite.getVertexV(3))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            break;
         case NORTH:
            vertexBuffer.vertex(mat, maxX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(1), sprite.getVertexV(1))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(3), sprite.getVertexV(3))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, maxY, minZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            break;
         case SOUTH:
            vertexBuffer.vertex(mat, maxX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(1), sprite.getVertexV(1))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, maxX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(2), sprite.getVertexV(2))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, maxY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(3), sprite.getVertexV(3))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
            vertexBuffer.vertex(mat, minX, minY, maxZ)
               .color(r, g, b, a)
               .uv(sprite.getVertexU(0), sprite.getVertexV(0))
               .overlayCoords(overlay)
               .uv2(light)
               .endVertex();
      }

      if (dir == Direction.NORTH) {
         sprite.rotate(180);
      }
   }
}
