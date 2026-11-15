package ic2.core.block.base.features;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface ISpecialWrenchable extends IWrenchableTile {
   @Override
   default boolean canSetFacing(Direction dir) {
      return false;
   }

   @Override
   default void setFacing(Direction dir) {
   }

   @Override
   boolean doSpecialAction(Direction var1, Vec3 var2, Player var3);

   @Override
   AABB hasSpecialAction(Direction var1, Vec3 var2, Player var3);

   @Override
   default boolean canRemoveBlock(Player player) {
      return false;
   }

   @Override
   default double getDropRate(Player player) {
      return 0.0;
   }
}
