package ic2.core.block.base;

import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

public class IC2TileType<T extends BlockEntity> extends BlockEntityType<T> {
   public IC2TileType(BlockEntitySupplier<T> factoryIn) {
      super(factoryIn, ObjectSets.emptySet(), null);
   }

   public boolean isValid(BlockState blockIn) {
      return true;
   }
}
