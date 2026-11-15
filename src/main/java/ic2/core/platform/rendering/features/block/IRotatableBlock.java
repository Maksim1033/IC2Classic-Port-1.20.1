package ic2.core.platform.rendering.features.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IRotatableBlock {
   boolean hasRotation(BlockState var1);

   Direction getRotation(BlockState var1);
}
