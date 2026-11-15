package ic2.core.block.machines.tiles.mv;

import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlastInductionFurnaceTileEntity extends InductionFurnaceTileEntity {
   public BlastInductionFurnaceTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 29, 2000);
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).blast_furnace;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.BLAST_FURNACE;
   }
}
