package ic2.core.block.base.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public interface IClickable {
   default IClickable.ClickAction getRequiredActions() {
      return IClickable.ClickAction.RIGHT_CLICK;
   }

   boolean onRightClick(Player var1, InteractionHand var2, Direction var3, BlockHitResult var4);

   default boolean onLeftClick(Player player, BlockPos pos) {
      return false;
   }

   public static enum ClickAction {
      NONE,
      RIGHT_CLICK,
      LEFT_CLICK,
      BOTH;

      public boolean canDoLeftClick() {
         return this == LEFT_CLICK || this == BOTH;
      }

      public boolean canDoRightClick() {
         return this == RIGHT_CLICK || this == BOTH;
      }
   }
}
