package ic2.core.block.transport.item.tubes;

import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneTubeTileEntity extends TubeTileEntity implements IRedstoneProvider, IRedstoneListener, ITileActivityProvider {
   public RedstoneTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"isActive"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.REDSTONE_TUBE;
   }

   @Override
   public boolean canLoseUpdateTick() {
      return !this.isActive() && super.canLoseUpdateTick();
   }

   @Override
   public void onTubeUpdate() {
      if (this.isSimulating()) {
         this.setActive(!this.items.isEmpty());
      }
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return true;
   }

   @Override
   public int getCommonSignalStrength(Direction side) {
      return this.isActive() ? 15 : 0;
   }
}
