package ic2.core.utils.math.geometry;

import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Vec3f {
   public float x;
   public float y;
   public float z;

   public Vec3f(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getZ() {
      return this.z;
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3f asVec() {
      return new Vector3f(this.x, this.y, this.z);
   }
}
