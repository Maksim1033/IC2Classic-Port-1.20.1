package ic2.core.block.transport.item.tubes;

import ic2.core.block.base.cache.ICache;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TransportTubeTileEntity extends TubeTileEntity {
   public TransportTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addCaches(new ICache[]{this.tubes});
   }

   @Override
   public boolean useDefaultCache() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.TRANSPORT_TUBE;
   }
}
