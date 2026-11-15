package ic2.core.entity.misc;

import ic2.core.platform.registries.IC2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CarbonBoat extends IC2Boat {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/models/boat/carbon_boat.png");

   public CarbonBoat(EntityType<? extends Boat> type, Level world) {
      super(type, world);
   }

   public CarbonBoat(EntityType<? extends Boat> type, Level worldIn, double x, double y, double z) {
      super(type, worldIn, x, y, z);
   }

   @Override
   public Item getDropItem() {
      return IC2Items.CARBON_BOAT;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   public void onInsideBubbleColumn(boolean downwards) {
   }

   public void onAboveBubbleCol(boolean downwards) {
   }

   @Override
   protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
      this.updateLastY();
      if (onGroundIn) {
         this.fallDistance = 0.0F;
      } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && y < 0.0) {
         this.fallDistance = (float)((double)this.fallDistance - y);
      }
   }
}
