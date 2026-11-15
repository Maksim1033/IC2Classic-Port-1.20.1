package ic2.api.items.electric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IScanner {
   int getScanRadius(ItemStack var1, boolean var2);

   boolean hasScanEffect(ItemStack var1);

   default boolean isOreValuable(ItemStack stack, BlockState state, LevelReader world, BlockPos pos) {
      return this.getOreValue(stack, state) > 0;
   }

   default boolean isOreValuable(ItemStack stack, BlockState state) {
      return this.getOreValue(stack, state) > 0;
   }

   default int getOreValue(ItemStack stack, BlockState state, LevelReader world, BlockPos pos) {
      return this.getOreValue(stack, state);
   }

   int getOreValue(ItemStack var1, BlockState var2);
}
