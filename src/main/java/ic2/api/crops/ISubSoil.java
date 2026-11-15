package ic2.api.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ISubSoil {
   default int getHumidity(Level world, BlockPos pos) {
      return this.getHumidity(world.getBlockState(pos));
   }

   int getHumidity(BlockState var1);

   default int getNutrients(Level world, BlockPos pos) {
      return this.getNutrients(world.getBlockState(pos));
   }

   int getNutrients(BlockState var1);

   default boolean isSpecial() {
      return false;
   }

   default BlockState getSpecialState(Block block) {
      return block.defaultBlockState();
   }
}
