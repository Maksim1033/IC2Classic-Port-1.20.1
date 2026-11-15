package ic2.core.item.upgrades;

import ic2.core.IC2;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.upgrades.swaps.EmptySwapper;
import ic2.core.item.upgrades.swaps.ISwapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SwapperUpgrade extends IC2SimpleItem {
   Block from;
   Block to;
   ISwapper swapper;

   public SwapperUpgrade(String itemName, String textureFolder, String textureName, Block from, Block to) {
      this(itemName, textureFolder, textureName, from, to, EmptySwapper.INSTANCE);
   }

   public SwapperUpgrade(String itemName, String textureFolder, String textureName, Block from, Block to, ISwapper swapper) {
      super(itemName, textureFolder, textureName);
      this.from = from;
      this.to = to;
      this.swapper = swapper;
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() == this.from) {
         BlockEntity before = world.getBlockEntity(pos);
         if (world.setBlockAndUpdate(pos, this.swapper.transfer(state, this.to))) {
            BlockEntity after = world.getBlockEntity(pos);
            this.swapper.transfer(before, after);
            IC2.PLATFORM.markBlockForRenderUpdate(pos);
            if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
               stack.shrink(1);
            }

            return InteractionResult.sidedSuccess(world.isClientSide);
         }
      }

      return InteractionResult.PASS;
   }
}
