package ic2.core.block.base.cache;

import ic2.api.util.DirectionList;
import ic2.api.util.ILocation;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class FilteredCapabilityCache<T> extends BaseCache<T> {
   Capability<T> capability;
   Predicate<BlockEntity> filter;

   public FilteredCapabilityCache(ILocation position, DirectionList validChecks, Capability<T> capability, Predicate<BlockEntity> filter) {
      super(position, validChecks);
      this.capability = capability;
      this.filter = filter;
   }

   @Override
   public void update() {
      Level world = this.position.getWorldObj();
      BlockPos worldPos = this.position.getPosition();

      for (Direction dir : this.present.invert().remove(this.validSides.invert())) {
         BlockEntity tile = DirectionList.getNeighborTile(world, worldPos, dir);
         if (tile != null && this.filter.test(tile)) {
            LazyOptional<T> cap = tile.getCapability(this.capability, dir.getOpposite());
            if (cap.isPresent()) {
               this.addCache(dir, (T)cap.orElse(null));
               cap.addListener(T -> this.clearCache(dir));
            }
         }
      }
   }
}
