package ic2.core.platform.rendering.misc;

import ic2.api.events.RetextureEvent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UVHelper {
   final float[] uvs;
   int rotation;

   public UVHelper(float[] uvsIn, int rotationIn) {
      this.uvs = uvsIn;
      this.rotation = rotationIn;
   }

   public static UVHelper fromSprite(TextureAtlasSprite sprite) {
      return new UVHelper(new float[]{sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1()}, 0);
   }

   public static UVHelper fromSprite(TextureAtlasSprite sprite, int rotation) {
      return new UVHelper(new float[]{sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1()}, rotation);
   }

   public static UVHelper fromSpriteScaled(TextureAtlasSprite sprite, float maxX, float maxY, int rotation) {
      return new UVHelper(
         new float[]{sprite.getU((double)maxX), sprite.getV((double)maxY), sprite.getU((double)(16.0F - maxX)), sprite.getV((double)(16.0F - maxY))}, rotation
      );
   }

   public float getVertexU(int indexIn) {
      if (this.uvs == null) {
         throw new NullPointerException("uvs");
      } else {
         int i = this.getVertexRotated(indexIn);
         return this.uvs[i != 0 && i != 1 ? 2 : 0];
      }
   }

   public float getVertexV(int indexIn) {
      if (this.uvs == null) {
         throw new NullPointerException("uvs");
      } else {
         int i = this.getVertexRotated(indexIn);
         return this.uvs[i != 0 && i != 3 ? 3 : 1];
      }
   }

   public static float[] rotateUVs(float[] uvs, RetextureEvent.Rotation rotation) {
      float[] array = new float[4];
      switch (rotation) {
         case ROTATION_180:
         case ROTATION_0:
            System.arraycopy(uvs, 0, array, 0, 4);
            break;
         case ROTATION_270:
         case ROTATION_90:
            array[0] = uvs[1];
            array[1] = uvs[0];
            array[2] = uvs[3];
            array[3] = uvs[2];
      }

      return array;
   }

   private int getVertexRotated(int indexIn) {
      return (indexIn + this.rotation / 90) % 4;
   }

   public int getRotation() {
      return this.rotation;
   }

   public void rotate(int rotations) {
      this.rotation = (this.rotation + rotations) % 360;
   }
}
