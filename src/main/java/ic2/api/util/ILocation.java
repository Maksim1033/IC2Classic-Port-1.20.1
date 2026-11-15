package ic2.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ILocation {
   default Level getWorldObj() {
      if (this instanceof BlockEntity) {
         return ((BlockEntity)this).getLevel();
      } else {
         throw new RuntimeException("ILocation needs to be implemented explicitly");
      }
   }

   default BlockPos getPosition() {
      if (this instanceof BlockEntity) {
         return ((BlockEntity)this).getBlockPos();
      } else {
         throw new RuntimeException("ILocation needs to be implemented explicitly");
      }
   }
}
