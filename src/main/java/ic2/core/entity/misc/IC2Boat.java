package ic2.core.entity.misc;

import ic2.core.platform.corehacks.mixins.server.BoatMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Boat.Status;
import net.minecraft.world.entity.vehicle.Boat.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public abstract class IC2Boat extends Boat {
   public IC2Boat(EntityType<? extends Boat> type, Level world) {
      super(type, world);
      this.setVariant(Type.OAK);
   }

   public IC2Boat(EntityType<? extends Boat> type, Level worldIn, double x, double y, double z) {
      this(type, worldIn);
      this.setPos(x, y, z);
      this.setDeltaMovement(Vec3.ZERO);
      this.xo = x;
      this.yo = y;
      this.zo = z;
   }

   public abstract Item getDropItem();

   public abstract ResourceLocation getTexture();

   public void spawnBrokenDrops() {
   }

   protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
      this.updateLastY();
      if (!this.isPassenger()) {
         if (onGroundIn) {
            if (this.fallDistance > 3.0F) {
               if (this.getBoatStatus() != Status.ON_LAND) {
                  this.fallDistance = 0.0F;
                  return;
               }

               this.causeFallDamage(this.fallDistance, 1.0F, this.level().damageSources().fall());
               if (!this.level().isClientSide && this.isAlive()) {
                  this.remove(RemovalReason.DISCARDED);
                  if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                     this.spawnBrokenDrops();
                  }
               }
            }

            this.fallDistance = 0.0F;
         } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && y < 0.0) {
            this.fallDistance = (float)((double)this.fallDistance - y);
         }
      }
   }

   public Status getBoatStatus() {
      return ((BoatMixin)this).getActualStatus();
   }

   public void updateLastY() {
      ((BoatMixin)this).setLastYDelta(this.getDeltaMovement().y);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
