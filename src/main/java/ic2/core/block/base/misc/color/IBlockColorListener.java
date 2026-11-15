package ic2.core.block.base.misc.color;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IBlockColorListener extends IItemColorListener {
   @OnlyIn(Dist.CLIENT)
   int getBlockColor(BlockState var1, BlockAndTintGetter var2, BlockPos var3, int var4);
}
