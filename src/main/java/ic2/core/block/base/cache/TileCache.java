package ic2.core.block.base.cache;

import ic2.api.util.DirectionList;
import ic2.api.util.ILocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileCache<T extends BlockEntity> extends BaseCache<T> {
   Class<T> filter;
   boolean testing = false;

   public TileCache(ILocation position, DirectionList validChecks, Class<T> filter) {
      super(position, validChecks);
      this.filter = filter;
   }

   public void validateExisting() {
      this.testing = true;
      boolean update = false;

      for (Direction dir : this.present) {
         BlockEntity entry = this.get(dir);
         if (entry != null && entry.isRemoved()) {
            this.clearCache(dir);
            update = true;
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

      for (Direction dir : this.validSides) {
         BlockEntity tile = DirectionList.getNeighborTile(world, worldPos, dir);
         if (!this.filter.isInstance(tile)) {
            this.clearCache(dir);
         } else {
            this.addCache(dir, (T)tile);
         }
      }

      this.testing = true;
   }

   public T getHandler(Direction dir) {
      T entry = this.get(dir);
      if (entry != null && entry.isRemoved()) {
         this.clearCache(dir);
         return null;
      } else {
         return entry;
      }
   }
}
