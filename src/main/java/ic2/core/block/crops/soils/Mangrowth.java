package ic2.core.block.crops.soils;

import ic2.api.crops.ISubSoil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MangroveRootsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class Mangrowth implements ISubSoil {
   @Override
   public int getHumidity(BlockState state) {
      return state.getFluidState().getType() == Fluids.WATER ? 3 : 0;
   }

   @Override
   public int getNutrients(BlockState state) {
      return state.getFluidState().getType() == Fluids.WATER ? -1 : 0;
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   @Override
   public BlockState getSpecialState(Block block) {
      return (BlockState)block.defaultBlockState().setValue(MangroveRootsBlock.WATERLOGGED, true);
   }
}
