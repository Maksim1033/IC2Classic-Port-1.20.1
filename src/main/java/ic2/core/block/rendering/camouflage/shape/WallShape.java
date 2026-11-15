package ic2.core.block.rendering.camouflage.shape;

import ic2.api.events.RetextureEvent;
import ic2.core.block.rendering.camouflage.CamouflageBuilder;
import ic2.core.platform.rendering.models.ShapeBuilder;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;

public class WallShape extends CamouflageShape {
   private static final List<ShapeBuilder.Quad>[][] QUADS = new CamouflageBuilder()
      .addBox(4.0F, 0.0F, 4.0F, 12.0F, 16.0F, 12.0F)
      .addBox(5.0F, 0.16F, 8.0F, 11.0F, 14.0F, 16.0F)
      .addBox(0.0F, 0.16F, 5.0F, 8.0F, 14.0F, 11.0F)
      .addBox(5.0F, 0.16F, 0.0F, 11.0F, 14.0F, 8.0F)
      .addBox(8.0F, 0.16F, 5.0F, 16.0F, 14.0F, 11.0F)
      .build();

   @Override
   protected boolean isSideFull(BlockState state, Direction side) {
      return false;
   }

   @Override
   protected void generateBakedQuads(
      BlockState original, Direction dir, RetextureEvent.Rotation[] rotations, List<BakedQuad> blockQuads, List<BakedQuad>[] result
   ) {
      List<BakedQuad> out = result[dir.get3DDataValue()];
      List<BakedQuad> allOut = result[6];
      boolean up = (Boolean)original.getValue(WallBlock.UP);
      boolean north = original.getValue(WallBlock.NORTH_WALL) != WallSide.NONE;
      boolean east = original.getValue(WallBlock.EAST_WALL) != WallSide.NONE;
      boolean west = original.getValue(WallBlock.WEST_WALL) != WallSide.NONE;
      boolean south = original.getValue(WallBlock.SOUTH_WALL) != WallSide.NONE;
      int i = 0;

      for (int m = blockQuads.size(); i < m; i++) {
         BakedQuad baked = blockQuads.get(i);
         if (up) {
            for (ShapeBuilder.Quad quad : QUADS[0][dir.get3DDataValue()]) {
               (dir.getAxis().isVertical() ? out : allOut).add(createQuad(baked, quad, i, dir, rotations[i]));
            }
         }

         if (north || east || west || south) {
            if (south) {
               for (ShapeBuilder.Quad quad : QUADS[1][dir.get3DDataValue()]) {
                  (dir != Direction.DOWN && dir != Direction.NORTH ? allOut : out).add(createQuad(baked, quad, i, dir, rotations[i]));
               }
            }

            if (west) {
               for (ShapeBuilder.Quad quad : QUADS[2][dir.get3DDataValue()]) {
                  (dir != Direction.DOWN && dir != Direction.EAST ? allOut : out).add(createQuad(baked, quad, i, dir, rotations[i]));
               }
            }

            if (north) {
               for (ShapeBuilder.Quad quad : QUADS[3][dir.get3DDataValue()]) {
                  (dir != Direction.DOWN && dir != Direction.SOUTH ? allOut : out).add(createQuad(baked, quad, i, dir, rotations[i]));
               }
            }

            if (east) {
               for (ShapeBuilder.Quad quad : QUADS[4][dir.get3DDataValue()]) {
                  (dir != Direction.DOWN && dir != Direction.WEST ? allOut : out).add(createQuad(baked, quad, i, dir, rotations[i]));
               }
            }
         }
      }
   }
}
