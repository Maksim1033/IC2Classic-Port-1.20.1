package ic2.api.items.electric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;

public interface IFluidScanner {
   int getScanRadius(ItemStack var1, boolean var2);

   default boolean isValuableFluid(ItemStack stack, FluidState state, LevelReader world, BlockPos pos) {
      return this.isValuableFluid(stack, state);
   }

   boolean isValuableFluid(ItemStack var1, FluidState var2);
}
