package ic2.core.entity.explosion;

import ic2.core.IC2;
import ic2.core.entity.renderer.IRenderableItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.math.geometry.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

public class DynamiteEntity extends Entity implements IRenderableItem {
   public static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.ITEM_STACK);
   public static final EntityDataAccessor<Boolean> STUCK = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Boolean> STICKY = SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.BOOLEAN);
   private int fuse = 100;
   private int ticksInGround = 0;
   private Vec3 stuckPosition;
   private BlockPos stuckBlock;

   public DynamiteEntity(EntityType<?> entityTypeIn, Level worldIn) {
      super(entityTypeIn, worldIn);
   }

   public DynamiteEntity(EntityType<?> entityTypeIn, Level worldIn, double x, double y, double z, BlockPos pos) {
      super(entityTypeIn, worldIn);
      this.setPos(x, y, z);
      this.setSticky(pos != null);
      this.setISStuck(pos != null);
      this.stuckPosition = new Vec3(x, y, z);
      this.stuckBlock = pos;
   }

   public DynamiteEntity(EntityType<?> entityTypeIn, Level worldIn, LivingEntity entityLiving) {
      this(entityTypeIn, worldIn);
      this.setPos(
         entityLiving.getX() - (double)(Mth.cos(entityLiving.getYRot() / 180.0F * 3.141593F) * 0.16F),
         entityLiving.getY() + (double)entityLiving.getEyeHeight(),
         entityLiving.getZ() - (double)(Mth.sin(entityLiving.getYRot() / 180.0F * 3.141593F) * 0.16F)
      );
      this.setDeltaMovement(
         (double)(-Mth.sin(entityLiving.getYRot() / 180.0F * 3.141593F) * Mth.cos(entityLiving.getXRot() / 180.0F * 3.141593F)),
         (double)(-Mth.sin(entityLiving.getXRot() / 180.0F * 3.141593F)),
         (double)(Mth.cos(entityLiving.getYRot() / 180.0F * 3.141593F) * Mth.cos(entityLiving.getXRot() / 180.0F * 3.141593F))
      );
      Vec3 motion = this.getDeltaMovement();
      this.shoot(motion.x, motion.y, motion.z, 1.0F, 1.0F);
   }

   protected void readAdditionalSaveData(CompoundTag compound) {
      this.fuse = compound.getInt("fuse");
      this.setISStuck(compound.getBoolean("stuck"));
      this.entityData.set(STICKY, compound.getBoolean("sticky"));
      this.tickCount = compound.getInt("in_ground");
      CompoundTag nbt = compound.getCompound("stuck_at");
      this.stuckPosition = nbt.isEmpty() ? null : new Vec3(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
      this.stuckBlock = this.stuckPosition == null ? null : BlockPos.of(nbt.getLong("pos"));
   }

   protected void addAdditionalSaveData(CompoundTag compound) {
      compound.putInt("fuse", this.fuse);
      compound.putBoolean("stuck", this.isStuck());
      compound.putBoolean("sticky", (Boolean)this.entityData.get(STICKY));
      if (this.stuckPosition != null) {
         CompoundTag nbt = new CompoundTag();
         nbt.putDouble("x", this.stuckPosition.x());
         nbt.putDouble("y", this.stuckPosition.y());
         nbt.putDouble("z", this.stuckPosition.z());
         nbt.putLong("pos", this.stuckBlock.asLong());
         compound.put("stuck_at", nbt);
      }

      compound.putInt("in_ground", this.ticksInGround);
   }

   public DynamiteEntity setSticky(boolean sticky) {
      this.entityData.set(STICKY, sticky);
      return this;
   }

   public DynamiteEntity setItem(ItemStack stack) {
      this.entityData.set(ITEM, stack.copy());
      return this;
   }

   public DynamiteEntity setFuse(int fuse) {
      this.fuse = fuse;
      return this;
   }

   public void setISStuck(boolean isStuck) {
      this.entityData.set(STUCK, isStuck);
   }

   public boolean isStuck() {
      return (Boolean)this.entityData.get(STUCK);
   }

   public boolean isSticky() {
      return (Boolean)this.entityData.get(STICKY);
   }

   @Override
   public ItemStack getStack() {
      return (ItemStack)this.entityData.get(ITEM);
   }

   public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
      float f2 = (float)Math.sqrt(x * x + y * y + z * z);
      float effect = 0.0075F * inaccuracy;
      this.setDeltaMovement(
         (x / (double)f2 + this.random.nextGaussian() * (double)effect) * (double)velocity,
         (y / (double)f2 + this.random.nextGaussian() * (double)effect) * (double)velocity,
         (z / (double)f2 + this.random.nextGaussian() * (double)effect) * (double)velocity
      );
      x = this.getDeltaMovement().x();
      z = this.getDeltaMovement().z();
      float n = (float)(Math.atan2(x, z) * 180.0 / 3.141592653589793);
      this.setYRot(n);
      this.yRotO = n;
      float n2 = (float)(Math.atan2(y, (double)((float)Math.sqrt(x * x + z * z))) * 180.0 / 3.141592653589793);
      this.setXRot(n2);
      this.xRotO = n2;
      this.ticksInGround = 0;
   }

   @OnlyIn(Dist.CLIENT)
   public void lerpMotion(double x, double y, double z) {
      super.lerpMotion(x, y, z);
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         float f = (float)Math.sqrt(x * x + z * z);
         float n = (float)(Math.atan2(x, z) * 180.0 / 3.141592741012573);
         this.setYRot(n);
         this.yRotO = n;
         this.setXRot((float)(Math.atan2(y, (double)f) * 180.0 / 3.141592741012573));
         this.xRotO = this.getXRot();
         this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
         this.ticksInGround = 0;
      }
   }

   protected void defineSynchedData() {
      this.entityData.define(ITEM, new ItemStack(IC2Blocks.DYNAMITE));
      this.entityData.define(STUCK, false);
      this.entityData.define(STICKY, false);
   }

   public void explode() {
      PointExplosion explosion = new PointExplosion(this.level(), this, this.blockPosition(), 1.0F, 1.0F, 0.8F);
      explosion.doExplosionA(new Box(-1, -1, -1, 1, 1, 1));
      explosion.doExplosionB(true);
   }

   public void tick() {
      super.tick();
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         Vec3 motion = this.getDeltaMovement();
         float n = (float)(Math.atan2(motion.x, motion.z) * 180.0 / 3.141592741012573);
         this.setYRot(n);
         this.yRotO = n;
         float n2 = (float)(Math.atan2(motion.y, (double)((float)Math.sqrt(motion.x * motion.x + motion.z * motion.z))) * 180.0 / 3.141592741012573);
         this.setXRot(n2);
         this.xRotO = n2;
      }

      if (this.fuse-- <= 0) {
         this.explode();
         this.remove(RemovalReason.DISCARDED);
      } else if (this.fuse < 100 && this.fuse % 2 == 0) {
         this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
      }

      if (this.isStuck()) {
         this.ticksInGround++;
         if (this.ticksInGround >= 200) {
            this.remove(RemovalReason.DISCARDED);
         }

         if (this.isSticky()) {
            this.fuse -= 3;
            this.setDeltaMovement(0.0, 0.0, 0.0);
            if (IC2.PLATFORM.isRendering()) {
               return;
            }

            if (this.stuckPosition != null) {
               this.setPos(this.stuckPosition.x(), this.stuckPosition.y(), this.stuckPosition.z());
               if (this.level().getBlockState(this.stuckBlock).getBlock() != Blocks.AIR) {
                  return;
               }

               this.stuckBlock = null;
               this.stuckPosition = null;
               this.setISStuck(false);
            }
         }
      }

      Vec3 motion = this.getDeltaMovement();
      Vec3 start = this.position();
      Vec3 end = start.add(motion);
      BlockHitResult raceTrace = this.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
      if (raceTrace.getType() == Type.BLOCK) {
         float remainX = (float)(raceTrace.getLocation().x - this.getX());
         float remainY = (float)(raceTrace.getLocation().y - this.getY());
         float remainZ = (float)(raceTrace.getLocation().z - this.getZ());
         float f2 = Mth.sqrt(remainX * remainX + remainY * remainY + remainZ * remainZ);
         this.stuckBlock = raceTrace.getBlockPos().immutable();
         this.stuckPosition = this.position();
         this.move(
            MoverType.SELF,
            new Vec3((double)(-(remainX / f2 * 0.05F - remainX)), (double)(-(remainY / f2 * 0.05F - remainY)), (double)(-(remainZ / f2 * 0.05F - remainZ)))
         );
         motion = motion.multiply(new Vec3(0.75 - (double)this.random.nextFloat(), -0.3, 0.75 - (double)this.random.nextFloat()));
         this.setISStuck(true);
      } else {
         this.move(MoverType.SELF, motion);
         this.setISStuck(false);
      }

      float f3 = (float)Math.sqrt(motion.x * motion.x + motion.z * motion.z);
      this.setYRot((float)(Math.atan2(motion.x, motion.z) * 180.0 / 3.141592741012573));
      this.setXRot((float)(Math.atan2(motion.y, (double)f3) * 180.0 / 3.141592741012573));
      if (this.getXRot() - this.xRotO < -180.0F) {
         this.xRotO -= 360.0F;
      }

      if (this.getXRot() - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }

      if (this.getYRot() - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }

      if (this.getYRot() - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }

      this.setXRot(this.xRotO + (this.getXRot() - this.xRotO) * 0.2F);
      this.setYRot(this.yRotO + (this.getYRot() - this.yRotO) * 0.2F);
      float f4 = 0.98F;
      float f5 = 0.04F;
      if (this.isInWater()) {
         this.fuse += 2000;

         for (int i1 = 0; i1 < 4; i1++) {
            float f6 = 0.25F;
            this.level()
               .addParticle(
                  ParticleTypes.BUBBLE,
                  this.getX() - motion.x * (double)f6,
                  this.getY() - motion.y * (double)f6,
                  this.getZ() - motion.z * (double)f6,
                  motion.x,
                  motion.y,
                  motion.z
               );
         }

         f4 = 0.75F;
      }

      this.setDeltaMovement(motion.multiply((double)f4, (double)f4, (double)f4).subtract(0.0, (double)f5, 0.0));
      this.setPos(this.getX(), this.getY(), this.getZ());
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
