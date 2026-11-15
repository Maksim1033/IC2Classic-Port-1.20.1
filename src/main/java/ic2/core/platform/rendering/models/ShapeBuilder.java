package ic2.core.platform.rendering.models;

import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShapeBuilder {
   List<ShapeBuilder.Quad> quads = new ObjectArrayList();

   public ShapeBuilder newAutoQuad(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return this.newQuad(minX, minY, minZ, maxX, maxY, maxZ).addAuto().finish();
   }

   public ShapeBuilder.QuadBuilder newQuad(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return new ShapeBuilder.QuadBuilder(this, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
   }

   public ShapeBuilder.QuadBuilder newQuad(AABB quad) {
      return this.newQuad(quad.minX, quad.minY, quad.minZ, quad.maxX, quad.maxY, quad.maxZ);
   }

   @OnlyIn(Dist.CLIENT)
   public void buildQuads(TextureAtlasSprite sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade, List<BakedQuad> result) {
      int i = 0;

      for (int m = this.quads.size(); i < m; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result.add(QuadBaker.createQuad(quad.box, quad.dir, quad.getFace(), sprite, rotation, partRotation, shade));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void buildLockedQuads(
      TextureAtlasSprite sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade, List<BakedQuad> result
   ) {
      int i = 0;

      for (int m = this.quads.size(); i < m; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result.add(QuadBaker.createLockedQuad(quad.box, quad.dir, quad.getFace(), sprite, rotation, partRotation, shade));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void buildQuads(
      TextureAtlasSprite sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade, int from, int to, List<BakedQuad> result
   ) {
      int i = from;

      for (int m = this.quads.size(); i < m && i < to; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result.add(QuadBaker.createQuad(quad.box, quad.dir, quad.getFace(), sprite, rotation, partRotation, shade));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void buildLockedQuads(
      TextureAtlasSprite sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade, int from, int to, List<BakedQuad> result
   ) {
      int i = from;

      for (int m = this.quads.size(); i < m && i < to; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result.add(QuadBaker.createLockedQuad(quad.box, quad.dir, quad.getFace(), sprite, rotation, partRotation, shade));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void buildQuads(TextureAtlasSprite sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade, List<BakedQuad>[] result) {
      int i = 0;

      for (int m = this.quads.size(); i < m; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result[quad.cull ? Direction.rotate(rotation.getRotation().getMatrix(), quad.dir).get3DDataValue() : 6]
            .add(QuadBaker.createQuad(quad.box, quad.dir, quad.getFace(), sprite, rotation, partRotation, shade));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void buildLockedQuads(
      TextureAtlasSprite sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade, List<BakedQuad>[] result
   ) {
      int i = 0;

      for (int m = this.quads.size(); i < m; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result[quad.cull ? Direction.rotate(rotation.getRotation().getMatrix(), quad.dir).get3DDataValue() : 6]
            .add(QuadBaker.createLockedQuad(quad.box, quad.dir, quad.getFace(), sprite, rotation, partRotation, shade));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void buildQuads(TextureAtlasSprite[] sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade, List<BakedQuad> result) {
      int i = 0;

      for (int m = this.quads.size(); i < m; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result.add(QuadBaker.createQuad(quad.box, quad.dir, quad.getFace(), sprite[quad.dir.get3DDataValue()], rotation, partRotation, shade));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void buildLockedQuads(
      TextureAtlasSprite[] sprite, BlockModelRotation rotation, BlockElementRotation partRotation, boolean shade, List<BakedQuad> result
   ) {
      int i = 0;

      for (int m = this.quads.size(); i < m; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result.add(QuadBaker.createLockedQuad(quad.box, quad.dir, quad.getFace(), sprite[quad.dir.get3DDataValue()], rotation, partRotation, shade));
      }
   }

   public List<ShapeBuilder.Quad> getQuads() {
      return this.quads;
   }

   public List<ShapeBuilder.Quad>[] getMappedQuads() {
      List<ShapeBuilder.Quad>[] result = CollectionUtils.createLists(6);
      int i = 0;

      for (int m = this.quads.size(); i < m; i++) {
         ShapeBuilder.Quad quad = this.quads.get(i);
         result[quad.dir.get3DDataValue()].add(quad);
      }

      return result;
   }

   public static float[] uvsByFace(Direction side, AABB box) {
      switch (side) {
         case DOWN:
            return new float[]{(float)box.minX, 16.0F - (float)box.maxZ, (float)box.maxX, 16.0F - (float)box.minZ};
         case UP:
            return new float[]{(float)box.minX, (float)box.minZ, (float)box.maxX, (float)box.maxZ};
         case SOUTH:
            return new float[]{(float)box.minX, 16.0F - (float)box.maxY, (float)box.maxX, 16.0F - (float)box.minY};
         case WEST:
            return new float[]{(float)box.minZ, 16.0F - (float)box.maxY, (float)box.maxZ, 16.0F - (float)box.minY};
         case EAST:
            return new float[]{16.0F - (float)box.maxZ, 16.0F - (float)box.maxY, 16.0F - (float)box.minZ, 16.0F - (float)box.minY};
         case NORTH:
         default:
            return new float[]{16.0F - (float)box.maxX, 16.0F - (float)box.maxY, 16.0F - (float)box.minX, 16.0F - (float)box.minY};
      }
   }

   public static ShapeBuilder.Quad buildQuad(AABB box, Direction dir, ShapeBuilder.ColoredUVs face, boolean cull) {
      return new ShapeBuilder.Quad(box, dir, face, cull);
   }

   public static class ColoredUVs {
      int tintedIndex;
      int rotation;
      float[] uvs;

      public ColoredUVs(int tintedIndex, int rotation, float[] uvs) {
         this.tintedIndex = tintedIndex;
         this.rotation = rotation;
         this.uvs = uvs;
      }

      @OnlyIn(Dist.CLIENT)
      public BlockElementFace create() {
         return new BlockElementFace(null, this.tintedIndex, "", new BlockFaceUV(this.uvs, this.rotation));
      }
   }

   public static class Quad {
      AABB box;
      ShapeBuilder.ColoredUVs face;
      Direction dir;
      boolean cull;

      Quad(AABB box, Direction dir, ShapeBuilder.ColoredUVs face, boolean cull) {
         this.box = box;
         this.face = face;
         this.dir = dir;
         this.cull = cull;
      }

      public AABB getBox() {
         return this.box;
      }

      public ShapeBuilder.ColoredUVs getUvs() {
         return this.face;
      }

      @OnlyIn(Dist.CLIENT)
      public BlockElementFace getFace() {
         return this.face.create();
      }

      public Direction getDir() {
         return this.dir;
      }

      public boolean isCull() {
         return this.cull;
      }
   }

   public static class QuadBuilder {
      AABB box;
      ShapeBuilder builder;

      QuadBuilder(ShapeBuilder builder, AABB box) {
         this.builder = builder;
         this.box = box;
      }

      public ShapeBuilder.QuadBuilder addAuto() {
         return this.addAuto(DirectionList.ALL);
      }

      public ShapeBuilder.QuadBuilder addAuto(DirectionList dirs) {
         return this.addAuto(dirs, -1, 0);
      }

      public ShapeBuilder.QuadBuilder addAuto(DirectionList dirs, int tintIndex) {
         return this.addAuto(dirs, tintIndex, 0);
      }

      public ShapeBuilder.QuadBuilder addAuto(DirectionList dirs, int tintIndex, int rotation) {
         for (Direction dir : dirs) {
            this.addFace(dir, tintIndex, rotation, ShapeBuilder.uvsByFace(dir, this.box));
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCube(float... uvs) {
         return this.addCube(-1, uvs);
      }

      public ShapeBuilder.QuadBuilder addCube(float[]... uvs) {
         for (Direction dir : DirectionList.ALL) {
            this.addFace(dir, -1, uvs[dir.get3DDataValue()]);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addHorizontalCube(float[]... uvs) {
         for (Direction dir : DirectionList.HORIZONTAL) {
            this.addFace(dir, -1, uvs[dir.get2DDataValue()]);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCube(int tintedIndex, float... uvs) {
         for (Direction dir : DirectionList.ALL) {
            this.addFace(dir, tintedIndex, uvs);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addHorizontalCube(int tintedIndex, float[]... uvs) {
         for (Direction dir : DirectionList.HORIZONTAL) {
            this.addFace(dir, tintedIndex, uvs[dir.get2DDataValue()]);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCube(int tintedIndex, float[]... uvs) {
         for (Direction dir : DirectionList.ALL) {
            this.addFace(dir, tintedIndex, uvs[dir.get3DDataValue()]);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCulledCube(float... uvs) {
         return this.addCulledCube(-1, uvs);
      }

      public ShapeBuilder.QuadBuilder addCulledCube(float[]... uvs) {
         for (Direction dir : DirectionList.ALL) {
            this.addCulledFace(dir, -1, uvs[dir.get3DDataValue()]);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addHorizontalCulledCube(float[]... uvs) {
         for (Direction dir : DirectionList.HORIZONTAL) {
            this.addCulledFace(dir, -1, uvs[dir.get2DDataValue()]);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCulledCube(int tintedIndex, float... uvs) {
         for (Direction dir : DirectionList.ALL) {
            this.addCulledFace(dir, tintedIndex, uvs);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addHorizontalCulledCube(int tintedIndex, float[]... uvs) {
         for (Direction dir : DirectionList.HORIZONTAL) {
            this.addCulledFace(dir, tintedIndex, uvs[dir.get2DDataValue()]);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCulledCube(int tintedIndex, float[]... uvs) {
         for (Direction dir : DirectionList.ALL) {
            this.addCulledFace(dir, tintedIndex, uvs[dir.get3DDataValue()]);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addFaces(DirectionList dirs, float... uvs) {
         for (Direction dir : dirs) {
            this.addFace(dir, uvs);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addFaces(DirectionList dirs, int tintedIndex, float... uvs) {
         for (Direction dir : dirs) {
            this.addFace(dir, tintedIndex, uvs);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addFaces(DirectionList dirs, int tintedIndex, int textureRotation, float... uvs) {
         for (Direction dir : dirs) {
            this.addFace(dir, tintedIndex, textureRotation, uvs);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCulledFaces(DirectionList dirs, float... uvs) {
         for (Direction dir : dirs) {
            this.addCulledFace(dir, uvs);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCulledFaces(DirectionList dirs, int tintedIndex, float... uvs) {
         for (Direction dir : dirs) {
            this.addCulledFace(dir, tintedIndex, uvs);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addCulledFaces(DirectionList dirs, int tintedIndex, int textureRotation, float... uvs) {
         for (Direction dir : dirs) {
            this.addCulledFace(dir, tintedIndex, textureRotation, uvs);
         }

         return this;
      }

      public ShapeBuilder.QuadBuilder addFace(Direction direction, float... uvs) {
         return this.addFace(direction, -1, 0, uvs);
      }

      public ShapeBuilder.QuadBuilder addFace(Direction direction, int tintedIndex, float... uvs) {
         return this.addFace(direction, tintedIndex, 0, uvs);
      }

      public ShapeBuilder.QuadBuilder addFace(Direction direction, int tintedIndex, int textureRotation, float... uvs) {
         if (uvs.length != 4) {
            throw new IllegalStateException("has to be 4 elements");
         } else {
            this.builder.quads.add(new ShapeBuilder.Quad(this.box, direction, new ShapeBuilder.ColoredUVs(tintedIndex, textureRotation, uvs), false));
            return this;
         }
      }

      public ShapeBuilder.QuadBuilder addCulledFace(Direction direction, float... uvs) {
         return this.addCulledFace(direction, -1, 0, uvs);
      }

      public ShapeBuilder.QuadBuilder addCulledFace(Direction direction, int tintedIndex, float... uvs) {
         return this.addCulledFace(direction, tintedIndex, 0, uvs);
      }

      public ShapeBuilder.QuadBuilder addCulledFace(Direction direction, int tintedIndex, int textureRotation, float... uvs) {
         if (uvs.length != 4) {
            throw new IllegalStateException("has to be 4 elements");
         } else {
            this.builder.quads.add(new ShapeBuilder.Quad(this.box, direction, new ShapeBuilder.ColoredUVs(tintedIndex, textureRotation, uvs), true));
            return this;
         }
      }

      public ShapeBuilder finish() {
         return this.builder;
      }
   }
}
