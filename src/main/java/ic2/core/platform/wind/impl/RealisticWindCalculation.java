package ic2.core.platform.wind.impl;

import ic2.core.platform.wind.IWindCalculation;
import ic2.core.platform.wind.IWindStream;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.SmoothDouble;
import ic2.core.utils.math.SmoothFloat;
import java.util.List;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class RealisticWindCalculation implements IWindCalculation {
   List<IWindStream> streams = CollectionUtils.<IWindStream>createList();

   @Override
   public void update(Level world) {
      int i = 0;

      for (int m = this.streams.size(); i < m; i++) {
         this.streams.get(i).onUpdate(world);
      }
   }

   @Override
   public double getWindSpeed(AABB box, float rotation, float angle) {
      double resultSpeed = 0.0;
      int totalMatches = 0;

      for (IWindStream stream : this.streams) {
         Tuple<IWindStream.CollisionState, Double> state = stream.getCollisionState(box, rotation, angle);
         if (state.getA() != IWindStream.CollisionState.MISSING) {
            totalMatches++;
            resultSpeed += (state.getA() == IWindStream.CollisionState.INVERTED_COLLISION ? -stream.getSpeed() : stream.getSpeed()) * state.getB();
         }
      }

      return totalMatches > 1 ? resultSpeed / (double)totalMatches : resultSpeed;
   }

   @Override
   public List<IWindStream> getStreams() {
      return this.streams;
   }

   public abstract static class HorizontalStream implements IWindStream {
      protected SmoothFloat rotation = new SmoothFloat();
      protected SmoothDouble speed = new SmoothDouble();
      protected SmoothFloat yLevel = new SmoothFloat();
      protected SmoothFloat thickness = new SmoothFloat();

      @Override
      public void onUpdate(Level world) {
         this.rotation.update(0.1F);
         this.speed.update(0.1);
         this.yLevel.update(0.1F);
         this.thickness.update(0.1F);
      }

      @Override
      public AABB getAffectedArea() {
         return new AABB(
            -2.147483648E9,
            (double)(this.yLevel.getValue() - this.thickness.getValue() - 5.0F),
            -2.147483648E9,
            2.147483647E9,
            (double)(this.yLevel.getValue() + this.thickness.getValue() + 5.0F),
            2.147483647E9
         );
      }

      @Override
      public Tuple<IWindStream.CollisionState, Double> getCollisionState(AABB hitBox, float direction, float angle) {
         AABB box = this.getAffectedArea();
         if (!(hitBox.minY > box.maxY) && !(hitBox.maxY < box.minY) && !this.isVertical(angle, 30.0F)) {
            double minDot = this.dotProduct(this.rotation.getValue(), direction);
            boolean inverted = minDot >= 90.0;
            double result = Math.max(0.0, 1.0 - (inverted ? 180.0 - minDot : minDot) / 30.0);
            return new Tuple(inverted ? IWindStream.CollisionState.INVERTED_COLLISION : IWindStream.CollisionState.COLLISION, result);
         } else {
            return new Tuple(IWindStream.CollisionState.MISSING, 0.0);
         }
      }

      public boolean isVertical(float angle, float limit) {
         return angle >= 360.0F - limit || angle <= limit || angle >= 180.0F - limit && angle <= 180.0F - limit;
      }

      @Override
      public float getDirection() {
         return this.rotation.getValue();
      }

      @Override
      public float getAngle() {
         return 0.0F;
      }

      @Override
      public double getSpeed() {
         return this.speed.getValue();
      }

      public double dotProduct(float alpha, float beta) {
         double d = (double)Math.abs(alpha - beta);
         return Math.min(360.0 - d, d);
      }
   }
}
