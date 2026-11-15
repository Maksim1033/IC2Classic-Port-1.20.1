package ic2.core.block.storage.tiles.station;

import ic2.core.block.base.tiles.impls.BaseBatteryStationTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HVBatteryStationTileEntity extends BaseBatteryStationTileEntity {
   public HVBatteryStationTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 75000000, 512);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.BATTERY_STATION_HV;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.7;
   }
}
