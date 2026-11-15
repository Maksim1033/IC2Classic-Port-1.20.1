package ic2.core.block.base.misc;

import ic2.api.util.DirectionList;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IAdvancedTubeBlock {
   DirectionList selectEndSides(DirectionList var1, Direction var2, BlockState var3);
}
