package ic2.core.block.rendering.block.tubes;

import ic2.core.block.base.misc.ITubeBlock;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;

public class SuperCableModel extends CableModel {
   public SuperCableModel(ITubeBlock tube, BlockState state) {
      super(tube, state);
   }

   @Override
   public BlockElementFace getFace(Direction dir, Direction side, float min, float max) {
      if (side == dir) {
         return new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{min, min, max, max}, 0));
      } else if (side.getAxis().isVertical()) {
         return side.getAxisDirection() == AxisDirection.NEGATIVE
            ? new BlockElementFace(
               null, -1, "", new BlockFaceUV(new float[]{min, 0.0F, max, min}, dir.get2DDataValue() * 90 + (dir.getAxis() == Axis.X ? 180 : 0))
            )
            : new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{min, max, max, 16.0F}, dir.get2DDataValue() * 90));
      } else if (side.getAxis().isHorizontal()) {
         return side.getAxisDirection() == AxisDirection.NEGATIVE
            ? new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, min, min, max}, 0))
            : new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{max, min, 16.0F, max}, 0));
      } else {
         return null;
      }
   }
}
