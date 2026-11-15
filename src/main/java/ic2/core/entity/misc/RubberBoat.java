package ic2.core.entity.misc;

import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RubberBoat extends IC2Boat {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/models/boat/rubber_boat.png");
   public float nextBounce = 0.0F;

   public RubberBoat(EntityType<? extends Boat> type, Level world) {
      super(type, world);
   }

   public RubberBoat(EntityType<? extends Boat> type, Level worldIn, double x, double y, double z) {
      super(type, worldIn, x, y, z);
   }

   @Override
   public Item getDropItem() {
      return IC2Items.RUBBER_BOAT;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   public boolean isInvulnerableTo(DamageSource source) {
      return source.is(DamageTypeTags.IS_FALL) || super.isInvulnerableTo(source);
   }

   public void tick() {
      if (this.nextBounce > 0.0F) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, (double)this.nextBounce, 0.0));
         this.nextBounce = 0.0F;
      }

      super.tick();
   }

   @Override
   public void spawnBrokenDrops() {
      for (int i = 0; i < 3; i++) {
         this.spawnAtLocation(IC2Blocks.RUBBERWOOD_PLANKS.asItem());
      }

      for (int j = 0; j < 2; j++) {
         this.spawnAtLocation(Items.STICK);
      }
   }

   @Override
   protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
      this.updateLastY();
      if (onGroundIn) {
         if (this.fallDistance > 40.0F) {
            if (!this.level().isClientSide && this.isAlive()) {
               this.remove(RemovalReason.DISCARDED);
               this.spawnAtLocation(IC2Items.RUBBER_BOAT_BROKEN);
            }
         } else if (this.fallDistance != 0.0F) {
            this.nextBounce = this.fallDistance / 17.5F;
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.1, 1.0, 1.1));
         }

         this.fallDistance = 0.0F;
      } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && y < 0.0) {
         this.fallDistance = (float)((double)this.fallDistance - y);
      }
   }
}
