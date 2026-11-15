package ic2.core.entity.explosion;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.MovementEmission;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

public class IC2ExplosiveEntity extends Entity {
   private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.defineId(IC2ExplosiveEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<BlockState> BLOCK = SynchedEntityData.defineId(IC2ExplosiveEntity.class, EntityDataSerializers.BLOCK_STATE);
   protected DamageSource damageSource;
   protected GameProfile igniter;
   protected int fuse = 80;
   protected float power = 4.0F;
   protected float dropRate = 0.3F;

   public IC2ExplosiveEntity(EntityType<?> entityTypeIn, Level worldIn) {
      super(entityTypeIn, worldIn);
      this.blocksBuilding = true;
   }

   public IC2ExplosiveEntity(
      EntityType<?> entityTypeIn, Level worldIn, double x, double y, double z, int fuse, float power, float dropRate, BlockState state, DamageSource source
   ) {
      this(entityTypeIn, worldIn);
      this.setPos(x, y, z);
      this.setFuse(fuse);
      this.power = power;
      this.dropRate = dropRate;
      if (state != null) {
         this.entityData.set(BLOCK, state);
      }

      this.damageSource = source;
   }

   public IC2ExplosiveEntity setIgniter(Player player) {
      return player == null ? this : this.setIgniter(player.getGameProfile());
   }

   public IC2ExplosiveEntity setIgniter(GameProfile pro) {
      this.igniter = pro;
      return this;
   }

   protected void defineSynchedData() {
      this.entityData.define(FUSE, 80);
      this.entityData.define(BLOCK, Blocks.DIRT.defaultBlockState());
   }

   protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
      return 0.0F;
   }

   protected MovementEmission getMovementEmission() {
      return MovementEmission.NONE;
   }

   public boolean isPickable() {
      return this.isAlive();
   }

   public void tick() {
      super.tick();
      this.xo = this.getX();
      this.yo = this.getY();
      this.zo = this.getZ();
      if (!this.isNoGravity()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
      }

      this.move(MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
      if (this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
      }

      this.fuse--;
      if (this.fuse <= 0) {
         this.remove(RemovalReason.DISCARDED);
         if (!this.level().isClientSide) {
            this.explode();
         }
      } else {
         this.updateSwimming();
         this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
      }
   }

   private void explode() {
      IC2Explosion explosion = new IC2Explosion(this.level(), this, this.position(), this.power, this.dropRate, this.damageSource).setIgniter(this.igniter);
      BlockState state = this.entityData.get(BLOCK);
      explosion.isBeacon = state != null && state.getBlock() == Blocks.BEACON;
      explosion.doExplosion();
   }

   protected void readAdditionalSaveData(CompoundTag compound) {
      this.setFuse(compound.getInt("fuse"));
      this.dropRate = compound.getFloat("drop");
      this.power = compound.getFloat("power");
   }

   protected void addAdditionalSaveData(CompoundTag compound) {
      compound.putInt("fuse", this.getFuseTime());
      compound.putFloat("power", this.power);
      compound.putFloat("drop", this.dropRate);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (key.equals(FUSE)) {
         this.fuse = (Integer)this.entityData.get(FUSE);
      }
   }

   public int getFuseTime() {
      return this.fuse;
   }

   public void setFuse(int fuse) {
      this.entityData.set(FUSE, fuse);
      this.fuse = fuse;
   }

   public BlockState getState() {
      return this.entityData.get(BLOCK);
   }

   @Override
   public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
