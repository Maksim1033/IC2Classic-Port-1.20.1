package ic2.core.entity.rockets;

import ic2.api.items.readers.IWrenchTool;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.machines.tiles.hv.RocketMinerTileEntity;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class MiningRocketEntity extends Entity {
   protected static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(MiningRocketEntity.class, EntityDataSerializers.BLOCK_POS);
   public double speed = 0.0;
   public boolean descending = false;
   public CompoundTag nbt = new CompoundTag();
   public double landingY;
   public boolean triggerChunk;

   public MiningRocketEntity(EntityType<?> entityTypeIn, Level worldIn) {
      super(entityTypeIn, worldIn);
      this.setInvulnerable(true);
   }

   public MiningRocketEntity(EntityType<?> entityTypeIn, Level worldIn, BlockPos pos, boolean triggerChunk) {
      super(entityTypeIn, worldIn);
      this.setPos((double)((float)pos.getX() + 0.5F), (double)pos.getY(), (double)((float)pos.getZ() + 0.5F));
      this.triggerChunk = triggerChunk;
      this.setInvulnerable(true);
   }

   protected void defineSynchedData() {
      this.entityData.define(TARGET, BlockPos.ZERO);
   }

   public void setTarget(BlockPos pos) {
      this.entityData.set(TARGET, pos);
   }

   public BlockPos getTarget() {
      return (BlockPos)this.entityData.get(TARGET);
   }

   public void tick() {
      super.tick();
      if (!this.descending) {
         if (this.speed < 5.0) {
            this.speed = this.speed + MathUtils.easeInBack((double)this.tickCount / 100.0) * 0.01;
            this.speed = Mth.clamp(this.speed, -0.01, 5.0);
         }

         double movementLeft = this.speed;

         while (movementLeft != 0.0) {
            this.move(MoverType.SELF, new Vec3(0.0, Mth.clamp(movementLeft, -1.0, 1.0), 0.0));
            if (this.speed < 0.0) {
               movementLeft = Math.min(0.0, movementLeft + 1.0);
            } else {
               movementLeft = Math.max(0.0, movementLeft - 1.0);
            }

            if (this.getY() >= (double)this.level().getMaxBuildHeight()
               || this.isSolidBlock(this.blockPosition().above())
               || this.isSolidBlock(this.blockPosition().above(2))) {
               this.moveToTarget();
            }
         }

         this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0, -0.5, 0.0);
      } else {
         double diff = this.getY() - this.landingY;
         if (diff > 30.0) {
            if (this.speed > -5.0) {
               this.speed -= 0.2;
            }
         } else if (diff > 10.0 && this.speed < -1.0) {
            this.speed = Math.min(this.speed + 0.75, -0.9);
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() - 0.5, this.getZ(), 0.0, -1.0, 0.0);
         } else if (this.speed < -0.2 && diff > 2.0) {
            this.speed = Math.min(this.speed + 0.05, -0.1);
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() - 0.5, this.getZ(), 0.0, -0.5, 0.0);
         } else if (this.speed < -0.02) {
            this.speed += 0.002;
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() - 0.5, this.getZ(), 0.0, -0.5, 0.0);
         }

         double movementLeft = this.speed;

         while (movementLeft < 0.0) {
            this.move(MoverType.SELF, new Vec3(0.0, Mth.clamp(movementLeft, -1.0, 0.0), 0.0));
            movementLeft++;
            BlockPos pos = this.getTarget();
            BlockPos result = new BlockPos(
               pos.getX(), (int)Math.max((long)this.level().getMinBuildHeight(), Math.round(this.getY())), pos.getZ()
            );
            if (result.getY() <= this.level().getMinBuildHeight()
               || this.isSolidBlock(result.below())
               || this.isSolidBlock(result)
               || this.verticalCollisionBelow) {
               if (IC2.PLATFORM.isSimulating()) {
                  if (!this.triggerChunk && result.getY() < this.getTarget().getY()) {
                     result = this.getTarget();

                     while (!this.level().isEmptyBlock(result) && result.getY() < this.level().getMaxBuildHeight()) {
                        result = result.above();
                     }
                  } else {
                     while (this.isSolidBlock(result) && result.getY() - this.level().getMinBuildHeight() < 5) {
                        result = result.above();
                     }
                  }

                  this.level().setBlockAndUpdate(result, IC2Blocks.ROCKET_MINER.defaultBlockState());
                  if (!(this.level().getBlockEntity(result) instanceof RocketMinerTileEntity rocket)) {
                     return;
                  }

                  rocket.load(this.nbt);
                  rocket.energy = 0;
                  rocket.onLanded(this.triggerChunk);
                  rocket.notifyChanges(true, DirectionList.ALL);
                  this.remove(RemovalReason.DISCARDED);
               }

               return;
            }
         }
      }
   }

   public boolean isPickable() {
      return true;
   }

   public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
      if (player.getItemInHand(hand).getItem() instanceof IWrenchTool && !this.level().isClientSide()) {
         this.level().setBlockAndUpdate(this.blockPosition(), IC2Blocks.ROCKET_MINER.defaultBlockState());
         if (this.level().getBlockEntity(this.blockPosition()) instanceof RocketMinerTileEntity rocket) {
            rocket.load(this.nbt);
            rocket.energy = 0;
            rocket.state = RocketMinerTileEntity.MinerState.RESET;
            rocket.onLanded(this.triggerChunk);
         }

         this.remove(RemovalReason.DISCARDED);
      }

      return InteractionResult.SUCCESS;
   }

   private boolean isSolidBlock(BlockPos pos) {
      return !this.level().isEmptyBlock(pos) && this.level().getBlockState(pos).blocksMotion();
   }

   public void moveToTarget() {
      this.descending = true;
      BlockPos pos = this.getTarget();
      this.speed = -0.5;
      MutableBlockPos mutable = new MutableBlockPos((double)pos.getX(), this.getY(), (double)pos.getZ());
      int startY = (int)this.getY();

      while (mutable.getY() >= this.level().getMinBuildHeight() && this.isSolidBlock(mutable) && startY - mutable.getY() < 32) {
         mutable.move(Direction.DOWN);
      }

      this.teleportTo(
         (double)((float)pos.getX() + 0.5F), startY - mutable.getY() < 32 ? (double)mutable.getY() : this.getY(), (double)((float)pos.getZ() + 0.5F)
      );
      mutable.setY((int)this.getY());

      while (mutable.getY() >= this.level().getMinBuildHeight() && !this.isSolidBlock(mutable)) {
         mutable.move(Direction.DOWN);
      }

      this.landingY = (double)(mutable.move(Direction.UP).getY() + 1);
   }

   protected void readAdditionalSaveData(CompoundTag compound) {
      this.speed = compound.getDouble("speed");
      this.descending = compound.getBoolean("descending");
      this.triggerChunk = compound.getBoolean("trigger_chunk");
      this.setTarget(BlockPos.of(compound.getLong("targetPos")));
      this.nbt = compound.getCompound("data");
   }

   protected void addAdditionalSaveData(CompoundTag compound) {
      compound.putDouble("speed", this.speed);
      compound.putBoolean("descending", this.descending);
      compound.putBoolean("trigger_chunk", this.triggerChunk);
      compound.putLong("targetPos", this.getTarget().asLong());
      compound.put("data", this.nbt);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
