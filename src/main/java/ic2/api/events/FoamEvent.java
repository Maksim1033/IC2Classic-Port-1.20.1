package ic2.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public abstract class FoamEvent extends LevelEvent {
   final BlockPos pos;

   public FoamEvent(LevelAccessor world, BlockPos pos) {
      super(world);
      this.pos = pos;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public BlockState getState() {
      return this.getLevel().getBlockState(this.getPos());
   }

   public BlockEntity getBlockEntity() {
      return this.getLevel().getBlockEntity(this.getPos());
   }

   @Cancelable
   public static class Check extends FoamEvent {
      FoamEvent.TargetType type = FoamEvent.TargetType.ANY;
      boolean isCustom = false;

      public Check(LevelAccessor world, BlockPos pos) {
         super(world, pos);
      }

      public void setCustomTarget(FoamEvent.TargetType type) {
         this.type = type;
         this.isCustom = true;
      }

      public boolean isCustomPlacement() {
         return this.isCustom;
      }

      public FoamEvent.TargetType getType() {
         return this.type;
      }
   }

   @Cancelable
   public static class Place extends FoamEvent {
      boolean placeFoam = false;

      public Place(LevelAccessor world, BlockPos pos) {
         super(world, pos);
      }

      public void requestFoamPlacement() {
         this.placeFoam = true;
      }

      public boolean shouldPlaceFoam() {
         return this.placeFoam;
      }
   }

   public static enum TargetType {
      ANY,
      SCAFFOLD,
      CABLE,
      TUBE,
      PIPE,
      CUSTOM;
   }
}
