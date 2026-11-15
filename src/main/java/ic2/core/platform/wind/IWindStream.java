package ic2.core.platform.wind;

import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface IWindStream {
   void onUpdate(Level var1);

   AABB getAffectedArea();

   Tuple<IWindStream.CollisionState, Double> getCollisionState(AABB var1, float var2, float var3);

   float getDirection();

   float getAngle();

   double getSpeed();

   public static enum CollisionState {
      MISSING,
      COLLISION,
      INVERTED_COLLISION;
   }
}
