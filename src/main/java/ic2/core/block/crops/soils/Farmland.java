package ic2.core.block.crops.soils;

import ic2.api.crops.IFarmland;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class Farmland implements IFarmland {
   @Override
   public int getHumidity(BlockState state) {
      return state.hasProperty(BlockStateProperties.MOISTURE) && state.getValue(BlockStateProperties.MOISTURE) == 7 ? 4 : 0;
   }

   @Override
   public int getNutrients(BlockState state) {
      return 0;
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   @Override
   public BlockState getSpecialState(Block block) {
      return (BlockState)block.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7);
   }
}
