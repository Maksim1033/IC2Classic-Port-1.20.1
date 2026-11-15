package ic2.core.item.upgrades.swaps;

import net.minecraft.world.level.block.entity.BlockEntity;

public class EmptySwapper implements ISwapper {
   public static final ISwapper INSTANCE = new EmptySwapper();

   @Override
   public void transfer(BlockEntity from, BlockEntity to) {
   }
}
