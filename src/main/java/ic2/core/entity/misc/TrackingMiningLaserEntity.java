package ic2.core.entity.misc;

import ic2.core.IC2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrackingMiningLaserEntity extends MiningLaserEntity {
   Entity tracked;

   public TrackingMiningLaserEntity(EntityType<?> type, Level worldIn) {
      super(type, worldIn);
   }

   public TrackingMiningLaserEntity(EntityType<?> type, Level world, LivingEntity entityLiving, float range, float power, int blockBreaks, Entity toReach) {
      super(type, world, entityLiving, range, power, blockBreaks, false);
      this.tracked = toReach;
   }

   @Override
   public void tick() {
      boolean simulating = IC2.PLATFORM.isSimulating();
      if (simulating) {
         if (this.tracked == null) {
            if (this.explosive) {
               this.explode();
            }

            this.remove(RemovalReason.DISCARDED);
            return;
         }

         Vec3 targetPosition = this.getTargetVector();
         double d0 = targetPosition.x - this.getX();
         double d1 = targetPosition.y - this.getY();
         double d2 = targetPosition.z - this.getZ();
         double d3 = Math.sqrt(d0 * d0 + d2 * d2);
         float f = (float)(Mth.atan2(d2, d0) * 57.29577951308232) - 90.0F;
         float f1 = (float)(-(Mth.atan2(d1, d3) * 57.29577951308232));
         this.setXRot(this.updateRotation(this.getXRot(), f1));
         this.setYRot(this.updateRotation(this.getYRot(), f));
         double yaw = Math.toRadians((double)this.getYRot());
         double pitch = Math.toRadians((double)this.getXRot());
         double startMotionX = -Math.sin(yaw) * Math.cos(pitch);
         double startMotionY = -Math.sin(pitch);
         double startMotionZ = Math.cos(yaw) * Math.cos(pitch);
         this.setLaserHeading(startMotionX, startMotionY, startMotionZ, 1.0);
      }

      super.tick();
   }

   protected float updateRotation(float from, float to) {
      float f = Mth.wrapDegrees(to - from);
      if (f > 360.0F) {
         f = 360.0F;
      }

      if (f < -360.0F) {
         f = -360.0F;
      }

      return from + f;
   }

   public Vec3 getTargetVector() {
      AABB box = this.tracked.getBoundingBox();
      return new Vec3((box.maxX - box.minX) / 2.0, (box.maxY - box.minY) / 2.0, (box.maxZ - box.minZ) / 2.0).add(this.tracked.position());
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (this.level() instanceof ServerLevel) {
         this.tracked = ((ServerLevel)this.level()).getEntity(compound.getUUID("target"));
      }
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putUUID("target", this.tracked.getUUID());
   }
}
