package ic2.core.block.rendering.camouflage.shape;

import ic2.api.events.RetextureEvent;
import ic2.api.util.DirectionList;
import ic2.core.block.rendering.camouflage.CamouflageBuilder;
import ic2.core.platform.rendering.models.ShapeBuilder;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

public class StairShape extends CamouflageShape {
   private static final List<ShapeBuilder.Quad>[][] QUADS = new CamouflageBuilder()
      .addCulledBox(DirectionList.UP.invert(), 0.0F, 0.0F, 0.0F, 16.0F, 8.0F, 16.0F)
      .addCulledBox(DirectionList.DOWN.invert(), 0.0F, 8.0F, 0.0F, 16.0F, 16.0F, 16.0F)
      .addBox(DirectionList.DOWN.invert(), DirectionList.NORTH.invert(), 0.0F, 8.0F, 8.0F, 16.0F, 16.0F, 16.0F)
      .addBox(DirectionList.DOWN.invert(), DirectionList.EAST.invert(), 0.0F, 8.0F, 0.0F, 8.0F, 16.0F, 16.0F)
      .addBox(DirectionList.DOWN.invert(), DirectionList.SOUTH.invert(), 0.0F, 8.0F, 0.0F, 16.0F, 16.0F, 8.0F)
      .addBox(DirectionList.DOWN.invert(), DirectionList.WEST.invert(), 8.0F, 8.0F, 0.0F, 16.0F, 16.0F, 16.0F)
      .addBox(DirectionList.UP.invert(), DirectionList.NORTH.invert(), 0.0F, 0.0F, 8.0F, 16.0F, 8.0F, 16.0F)
      .addBox(DirectionList.UP.invert(), DirectionList.EAST.invert(), 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 16.0F)
      .addBox(DirectionList.UP.invert(), DirectionList.SOUTH.invert(), 0.0F, 0.0F, 0.0F, 16.0F, 8.0F, 8.0F)
      .addBox(DirectionList.UP.invert(), DirectionList.WEST.invert(), 8.0F, 0.0F, 0.0F, 16.0F, 8.0F, 16.0F)
      .addBox(DirectionList.DOWN.invert(), DirectionList.NORTH.invert(), 8.0F, 8.0F, 8.0F, 16.0F, 16.0F, 16.0F)
      .addBox(DirectionList.DOWN.invert(), DirectionList.EAST.invert(), 0.0F, 8.0F, 8.0F, 8.0F, 16.0F, 16.0F)
      .addBox(DirectionList.DOWN.invert(), DirectionList.SOUTH.invert(), 0.0F, 8.0F, 0.0F, 8.0F, 16.0F, 8.0F)
      .addBox(DirectionList.DOWN.invert(), DirectionList.WEST.invert(), 8.0F, 8.0F, 0.0F, 16.0F, 16.0F, 8.0F)
      .addBox(DirectionList.UP.invert(), DirectionList.NORTH.invert(), 8.0F, 0.0F, 8.0F, 16.0F, 8.0F, 16.0F)
      .addBox(DirectionList.UP.invert(), DirectionList.EAST.invert(), 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 16.0F)
      .addBox(DirectionList.UP.invert(), DirectionList.SOUTH.invert(), 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F)
      .addBox(DirectionList.UP.invert(), DirectionList.WEST.invert(), 8.0F, 0.0F, 0.0F, 16.0F, 8.0F, 8.0F)
      .build();

   @Override
   protected boolean isSideFull(BlockState state, Direction side) {
      return false;
   }

   private Direction getDirection(StairsShape shape, Direction facing) {
      if (shape != StairsShape.INNER_LEFT && shape != StairsShape.INNER_RIGHT) {
         if (shape != StairsShape.OUTER_LEFT && shape != StairsShape.OUTER_RIGHT) {
            return facing;
         } else {
            return shape == StairsShape.OUTER_RIGHT ? facing.getClockWise() : facing;
         }
      } else {
         return shape == StairsShape.INNER_LEFT ? facing.getCounterClockWise() : facing.getOpposite();
      }
   }

   @Override
   protected void generateBakedQuads(
      BlockState original, Direction dir, RetextureEvent.Rotation[] rotations, List<BakedQuad> blockQuads, List<BakedQuad>[] result
   ) {
      StairsShape shape = (StairsShape)original.getValue(StairBlock.SHAPE);
      boolean top = original.getValue(StairBlock.HALF) == Half.TOP;
      Direction facing = (Direction)original.getValue(StairBlock.FACING);
      List<BakedQuad> out = result[dir.get3DDataValue()];
      List<BakedQuad> allOut = result[6];
      List<ShapeBuilder.Quad> main = QUADS[top ? 1 : 0][dir.get3DDataValue()];
      List<ShapeBuilder.Quad> straight = QUADS[2 + (top ? 4 : 0) + facing.get2DDataValue()][dir.get3DDataValue()];
      List<ShapeBuilder.Quad> corner = QUADS[10 + (top ? 4 : 0) + this.getDirection(shape, facing).get2DDataValue()][dir.get3DDataValue()];
      int i = 0;

      for (int m = blockQuads.size(); i < m; i++) {
         BakedQuad baked = blockQuads.get(i);

         for (ShapeBuilder.Quad quad : main) {
            (quad.isCull() ? out : allOut).add(createQuad(baked, quad, i, dir, rotations[i]));
         }

         if (shape == StairsShape.STRAIGHT || shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) {
            for (ShapeBuilder.Quad quad : straight) {
               (quad.isCull() ? out : allOut).add(createQuad(baked, quad, i, dir, rotations[i]));
            }
         }

         if (shape != StairsShape.STRAIGHT) {
            for (ShapeBuilder.Quad quad : corner) {
               (quad.isCull() ? out : allOut).add(createQuad(baked, quad, i, dir, rotations[i]));
            }
         }
      }
   }
}
