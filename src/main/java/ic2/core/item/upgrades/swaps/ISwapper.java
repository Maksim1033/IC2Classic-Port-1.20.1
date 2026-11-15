package ic2.core.item.upgrades.swaps;

import ic2.api.blocks.PainterHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ISwapper {
   static ISwapper empty() {
      return EmptySwapper.INSTANCE;
   }

   static ISwapper chargePad() {
      return ChargepadSwapper.INSTANCE;
   }

   static ISwapper chargeBench() {
      return ChargingBenchSwapper.INSTANCE;
   }

   static ISwapper batteryStation() {
      return BatteryStationSwapper.INSTANCE;
   }

   static ISwapper energyStorage() {
      return EnergyStorageSwapper.INSTANCE;
   }

   void transfer(BlockEntity var1, BlockEntity var2);

   default BlockState transfer(BlockState from, Block to) {
      return PainterHelper.copyProperties(from, to.defaultBlockState());
   }
}
