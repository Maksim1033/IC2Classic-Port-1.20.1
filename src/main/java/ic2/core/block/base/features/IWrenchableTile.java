package ic2.core.block.base.features;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface IWrenchableTile {
   boolean canSetFacing(Direction var1);

   void setFacing(Direction var1);

   default boolean doSpecialAction(Direction side, Vec3 hit, Player player) {
      return false;
   }

   default AABB hasSpecialAction(Direction side, Vec3 hit, Player player) {
      return null;
   }

   boolean canRemoveBlock(Player var1);

   double getDropRate(Player var1);

   default boolean isHarvestWrenchRequired(Player player) {
      return true;
   }
}
