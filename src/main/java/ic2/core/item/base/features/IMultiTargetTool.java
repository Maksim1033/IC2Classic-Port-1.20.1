package ic2.core.item.base.features;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IMultiTargetTool {
   boolean isMultiMining(ItemStack var1);

   boolean canMultiMine(ItemStack var1);

   Iterator<BlockPos> getHitPositions(ItemStack var1, Player var2, BlockPos var3, Direction var4);
}
