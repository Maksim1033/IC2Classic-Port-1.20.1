package ic2.core.block.transport.item.tubes;

import ic2.api.tiles.tubes.TransportedItem;
import ic2.core.block.base.features.ICollideable;
import ic2.core.block.transport.item.TubeAction;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PickupTubeTileEntity extends TubeTileEntity implements ICollideable {
   public boolean largeRadius = false;

   public PickupTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PICKUP_TUBE;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "large", this.largeRadius, false);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.largeRadius = compound.getBoolean("large");
   }

   @Override
   public void onEntityCollided(Entity entity) {
      if (!this.isRendering() && entity instanceof ItemEntity item && item.isAlive()) {
         TransportedItem transported = new TransportedItem(item.getItem().copy());
         item.remove(RemovalReason.DISCARDED);
         this.addItem(transported, null);
         transported.setCentering();
         return;
      }
   }

   @Override
   public void onTubeUpdate() {
      if (!this.isRendering() && this.largeRadius && !this.invClock(5)) {
         for (ItemEntity entity : this.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(this.getBlockPos()).inflate(5.0))) {
            if (entity.isAlive()) {
               TransportedItem transported = new TransportedItem(entity.getItem().copy());
               entity.remove(RemovalReason.DISCARDED);
               this.addItem(transported, null);
               transported.setCentering();
            }
         }
      }
   }

   @Override
   public boolean doTubeAction(Direction side, Vec3 hit, Player player, TubeAction action) {
      if (action == TubeAction.LARGE_PICKUP_RADIUS) {
         this.largeRadius = !this.largeRadius;
         if (this.isSimulating()) {
            player.displayClientMessage(
               this.translate(this.largeRadius ? "gui.ic2.tube.pickup.large_radius.enable" : "gui.ic2.tube.pickup.large_radius.disable"), false
            );
         }

         return true;
      } else {
         return false;
      }
   }
}
