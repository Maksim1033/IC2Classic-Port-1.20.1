package ic2.core.platform.rendering;

import ic2.core.block.base.misc.color.IBlockColorListener;
import ic2.core.block.base.misc.color.IItemColorListener;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IC2Colors implements BlockColor, ItemColor {
   public static final IC2Colors INSTANCE = new IC2Colors();

   public int getColor(ItemStack stack, int tintIndex) {
      return stack.getItem() instanceof IItemColorListener ? ((IItemColorListener)stack.getItem()).getItemColor(stack, tintIndex) : -1;
   }

   public int getColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
      return state.getBlock() instanceof IBlockColorListener ? ((IBlockColorListener)state.getBlock()).getBlockColor(state, world, pos, tintIndex) : -1;
   }
}
