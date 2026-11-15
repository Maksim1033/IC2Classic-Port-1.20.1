package ic2.core.platform.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import org.joml.Vector3f;
import ic2.core.platform.rendering.misc.ModelTransform;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.FaceInfo.Constants;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

@OnlyIn(Dist.CLIENT)
public class QuadBaker {
   public static final FaceBakery BAKER = new FaceBakery();
   public static final ItemModelGenerator GENERATOR = new ItemModelGenerator();
   static final QuadBaker.Vec3Helper[][] FACES = new QuadBaker.Vec3Helper[][]{
      {new QuadBaker.Vec3Helper(false, false, false), new QuadBaker.Vec3Helper(true, false, true)},
      {new QuadBaker.Vec3Helper(false, true, false), new QuadBaker.Vec3Helper(true, true, true)},
      {new QuadBaker.Vec3Helper(false, false, false), new QuadBaker.Vec3Helper(true, true, false)},
      {new QuadBaker.Vec3Helper(false, false, true), new QuadBaker.Vec3Helper(true, true, true)},
      {new QuadBaker.Vec3Helper(false, false, false), new QuadBaker.Vec3Helper(true, true, true)},
      {new QuadBaker.Vec3Helper(false, false, false), new QuadBaker.Vec3Helper(true, true, true)}
   };

   public static BakedQuad copyWithTint(BakedQuad quad, int tint) {
      return new BakedQuad(quad.getVertices(), tint, quad.getDirection(), quad.getSprite(), quad.isShade());
   }

   public static List<BakedQuad> createQuadsFromTexture(int tint, TextureAtlasSprite sprite, ItemTransform transform) {
      List<BlockElement> parts = GENERATOR.processFrames(tint, "test", sprite.contents());
      if (parts.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<BakedQuad> list = CollectionUtils.<BakedQuad>createList();

         for (BlockElement part : parts) {
            for (Direction dir : part.faces.keySet()) {
               BlockElementFace face = (BlockElementFace)part.faces.get(dir);
               list.add(BAKER.bakeQuad(part.from, part.to, face, sprite, dir, BlockModelRotation.X0_Y0, part.rotation, false, BaseModel.LOCATION));
            }
         }

         return list;
      }
   }

   public static BakedQuad createQuad(
      AABB box, Direction side, BlockElementFace face, TextureAtlasSprite sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade
   ) {
      return BAKER.bakeQuad(getMin(side, box), getMax(side, box), face, sprite, side, rotation, partRotation, shade, BaseModel.LOCATION);
   }

   public static BakedQuad createLockedQuad(
      AABB box, Direction side, BlockElementFace face, TextureAtlasSprite sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade
   ) {
      return BAKER.bakeQuad(
         getMin(side, box), getMax(side, box), face, sprite, side, new ModelTransform(rotation, true), partRotation, shade, BaseModel.LOCATION
      );
   }

   public static float[] getPosArray(AABB box, Direction dir) {
      return getPosArray(getMin(dir, box), getMax(dir, box));
   }

   public static float[] getPosArray(Vector3f min, Vector3f max) {
      float[] data = new float[6];
      data[Constants.MIN_X] = min.x();
      data[Constants.MIN_Y] = min.y();
      data[Constants.MIN_Z] = min.z();
      data[Constants.MAX_X] = max.x();
      data[Constants.MAX_Y] = max.y();
      data[Constants.MAX_Z] = max.z();
      return data;
   }

   public static Vector3f getMin(Direction side, Vector3f min, Vector3f max) {
      return FACES[side.get3DDataValue()][0].applyBoundingBox(min, max);
   }

   public static Vector3f getMax(Direction side, Vector3f min, Vector3f max) {
      return FACES[side.get3DDataValue()][1].applyBoundingBox(min, max);
   }

   public static Vector3f getMin(Direction side, AABB box) {
      return FACES[side.get3DDataValue()][0].applyBoundingBox(box);
   }

   public static Vector3f getMax(Direction side, AABB box) {
      return FACES[side.get3DDataValue()][1].applyBoundingBox(box);
   }

   public static Tuple<Integer, Integer> getModelRotation(Direction facing) {
      return facing.getAxis().isHorizontal() ? new Tuple(0, facing.get2DDataValue() * 90) : new Tuple(facing == Direction.DOWN ? 270 : 90, 0);
   }

   public static BakedQuad genQuad(
      Transformation transform, float x1, float y1, float x2, float y2, float z, TextureAtlasSprite sprite, Direction facing, int color, int tint
   ) {
      float u1 = sprite.getU(0.0);
      float v1 = sprite.getV(0.0);
      float u2 = sprite.getU(16.0);
      float v2 = sprite.getV(16.0);
      x1 /= 16.0F;
      y1 /= 16.0F;
      x2 /= 16.0F;
      y2 /= 16.0F;
      float var17 = 1.0F - y2;
      y2 = 1.0F - y1;
      return putQuad(transform, facing, sprite, color, tint, x1, var17, x2, y2, z, u1, v1, u2, v2, 0);
   }

   private static BakedQuad putQuad(
      Transformation transform,
      Direction side,
      TextureAtlasSprite sprite,
      int color,
      int tint,
      float x1,
      float y1,
      float x2,
      float y2,
      float z,
      float u1,
      float v1,
      float u2,
      float v2,
      int luminosity
   ) {
      BakedQuad[] result = new BakedQuad[1];
      QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer(T -> result[0] = T);
      builder.setTintIndex(tint);
      builder.setDirection(side);
      builder.setShade(luminosity == 0);
      builder.setSprite(sprite);
      if (side == Direction.SOUTH) {
         putVertex(builder, side, x1, y1, z, u1, v2, color, luminosity);
         putVertex(builder, side, x2, y1, z, u2, v2, color, luminosity);
         putVertex(builder, side, x2, y2, z, u2, v1, color, luminosity);
         putVertex(builder, side, x1, y2, z, u1, v1, color, luminosity);
      } else {
         putVertex(builder, side, x1, y1, z, u1, v2, color, luminosity);
         putVertex(builder, side, x1, y2, z, u1, v1, color, luminosity);
         putVertex(builder, side, x2, y2, z, u2, v1, color, luminosity);
         putVertex(builder, side, x2, y1, z, u2, v2, color, luminosity);
      }

      if (!transform.isIdentity()) {
         QuadTransformers.applying(transform).processInPlace(result[0]);
      }

      return result[0];
   }

   private static void putVertex(VertexConsumer consumer, Direction side, float x, float y, float z, float u, float v, int color, int luminosity) {
      consumer.vertex((double)x, (double)y, (double)z)
         .color(color)
         .uv(u, v)
         .uv2((int)((float)(luminosity << 4) / 32768.0F), (int)((float)(luminosity << 4) / 32768.0F))
         .normal((float)side.getStepX(), (float)side.getStepY(), (float)side.getStepZ())
         .endVertex();
   }

   public static class Vec3Helper {
      boolean x;
      boolean y;
      boolean z;

      public Vec3Helper(boolean par1, boolean par2, boolean par3) {
         this.x = par1;
         this.y = par2;
         this.z = par3;
      }

      public Vector3f applyBoundingBox(Vector3f min, Vector3f max) {
         float xScale = this.x ? max.x() : min.x();
         float yScale = this.y ? max.y() : min.y();
         float zScale = this.z ? max.z() : min.z();
         return new Vector3f(xScale, yScale, zScale);
      }

      public Vector3f applyBoundingBox(AABB box) {
         float xScale = (float)(this.x ? box.maxX : box.minX);
         float yScale = (float)(this.y ? box.maxY : box.minY);
         float zScale = (float)(this.z ? box.maxZ : box.minZ);
         return new Vector3f(xScale, yScale, zScale);
      }
   }
}
