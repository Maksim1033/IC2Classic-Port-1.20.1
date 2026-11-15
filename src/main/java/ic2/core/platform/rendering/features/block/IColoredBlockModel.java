package ic2.core.platform.rendering.features.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IColoredBlockModel {
   int getTintedIndexFor(BlockState var1, Direction var2, int var3);
}
