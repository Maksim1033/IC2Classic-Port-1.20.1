package ic2.core.block.generators.tiles;

import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.reactor.IReactor;
import ic2.api.util.DirectionList;
import ic2.core.block.base.tiles.impls.BaseReactorChamberTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricReactorChamberTileEntity extends BaseReactorChamberTileEntity implements IEnergyTile {
   public IReactor reactor;

   public ElectricReactorChamberTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public IReactor getReactor() {
      if (this.reactor != null && ((BlockEntity)this.reactor).isRemoved()) {
         this.reactor = null;
      }

      if (this.reactor == null) {
         for (Direction dir : DirectionList.ALL) {
            BlockEntity tile = DirectionList.getNeighborTile(this, dir);
            if (tile instanceof IReactor && tile instanceof IEnergySource) {
               this.reactor = (IReactor)tile;
            }
         }
      }

      if (this.reactor == null) {
         this.getBlockState().neighborChanged(this.getLevel(), this.getBlockPos(), Blocks.AIR, this.getBlockPos(), false);
      }

      return this.reactor;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.REACTOR_CHAMBER;
   }
}
