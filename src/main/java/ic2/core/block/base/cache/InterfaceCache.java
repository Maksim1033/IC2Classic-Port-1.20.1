package ic2.core.block.base.cache;

import com.google.common.collect.ObjectArrays;
import ic2.api.util.DirectionList;
import ic2.api.util.ILocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class InterfaceCache<T> extends BaseCache<T> {
   Class<T> filter;
   boolean testing = false;

   public InterfaceCache(ILocation position, DirectionList validChecks, Class<T> filter) {
      super(position, validChecks);
      this.filter = filter;
      this.cache = (T[])ObjectArrays.newArray(filter, 6);
   }

   public void validateExisting() {
      this.testing = true;
      boolean update = false;

      for (Direction dir : this.present) {
         T entry = this.get(dir);
         if (entry instanceof BlockEntity) {
            BlockEntity entity = (BlockEntity)entry;
            if (entity.isRemoved()) {
               this.clearCache(dir);
               update = true;
            }
         }
      }

      if (update) {
         this.update();
      }

      this.testing = false;
   }

   @Override
   public void markDirty() {
      if (!this.testing) {
         super.markDirty();
      }
   }

   @Override
   public void update() {
      this.testing = true;
      Level world = this.position.getWorldObj();
      BlockPos worldPos = this.position.getPosition();

      for (Direction dir : this.present.invert().remove(this.validSides.invert())) {
         BlockPos pos = worldPos.relative(dir);
         if (!world.isLoaded(pos)) {
            this.clearCache(dir);
         } else {
            BlockEntity tile = world.getBlockEntity(pos);
            if (!this.filter.isInstance(tile)) {
               this.clearCache(dir);
            } else {
               this.addCache(dir, (T)tile);
            }
         }
      }

      this.testing = false;
   }

   @Override
   public T getHandler(Direction dir) {
      T entry = this.get(dir);
      if (entry instanceof BlockEntity entity && entity.isRemoved()) {
         this.clearCache(dir);
         return null;
      }

      return entry;
   }
}
