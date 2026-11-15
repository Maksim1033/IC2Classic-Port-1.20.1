package ic2.core.block.base.features.personal;

import ic2.core.block.base.features.IWrenchableTile;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public interface IPersonalWrenchable extends IWrenchableTile {
   boolean canSetFacing(Direction var1, Player var2);

   @Override
   default boolean canSetFacing(Direction dir) {
      return false;
   }
}
