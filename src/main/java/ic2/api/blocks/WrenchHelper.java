package ic2.api.blocks;

import ic2.api.items.readers.IWrenchTool;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class WrenchHelper {
   public static boolean hasWrench(Player player) {
      return isWrench(player.getMainHandItem()) || isWrench(player.getOffhandItem());
   }

   public static boolean isWrench(ItemStack stack) {
      if (stack.getItem() instanceof IWrenchTool tool && tool.shouldRenderOverlay(stack)) {
         return true;
      }

      return false;
   }

   public static int getDirectionIndex(BlockHitResult result) {
      return getDirectionIndex(result.getDirection(), result.getLocation().subtract(Vec3.atLowerCornerOf(result.getBlockPos())));
   }

   public static int getDirectionIndex(UseOnContext context) {
      return getDirectionIndex(context.getClickedFace(), context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos())));
   }

   public static int getDirectionIndex(Direction direction, Vec3 sideHit) {
      switch (direction.getAxis()) {
         case X:
            return calculateIndex(direction == Direction.EAST ? 1.0 - sideHit.z() : sideHit.z(), 1.0 - sideHit.y());
         case Y:
            return calculateIndex(sideHit.x(), direction == Direction.DOWN ? 1.0 - sideHit.z() : sideHit.z());
         case Z:
            return calculateIndex(direction == Direction.NORTH ? 1.0 - sideHit.x() : sideHit.x(), 1.0 - sideHit.y());
         default:
            return 0;
      }
   }

   public static Direction getFacingFromIndex(Direction base, int index, Player player) {
      if (base.getAxis().isHorizontal()) {
         return switch (index) {
            case 1 -> player.isShiftKeyDown() ? base.getOpposite() : base;
            case 2 -> Direction.DOWN;
            case 4 -> Direction.UP;
            case 8 -> base.getClockWise();
            case 16 -> base.getCounterClockWise();
            default -> null;
         };
      } else {
         return switch (index) {
            case 1 -> player.isShiftKeyDown() ? base.getOpposite() : base;
            case 2 -> base == Direction.DOWN ? Direction.NORTH : Direction.SOUTH;
            case 4 -> base == Direction.DOWN ? Direction.SOUTH : Direction.NORTH;
            case 8 -> Direction.WEST;
            case 16 -> Direction.EAST;
            default -> null;
         };
      }
   }

   private static int calculateIndex(double x, double z) {
      if (x >= 0.2 && x <= 0.8 && z >= 0.2 && z <= 0.8) {
         return 1;
      } else if (!(x < 0.2) && !(x > 0.8)
         || (!(z >= 0.2) || !(z <= 0.8)) && (!(x > 0.8) || (z > 0.5 ? !(z <= x) : !(z >= 1.0 - x))) && (!(x < 0.2) || (z > 0.5 ? !(z <= 1.0 - x) : !(z >= x)))) {
         if ((z < 0.2 || z > 0.8) && (x >= 0.2 && x <= 0.8 || z > 0.8 && (x > 0.5 || x >= 1.0 - z) || z < 0.2 && (x > 0.5 ? x <= 1.0 - z : x >= z))) {
            return z < 0.5 ? 4 : 2;
         } else {
            return 0;
         }
      } else {
         return x < 0.5 ? 8 : 16;
      }
   }
}
