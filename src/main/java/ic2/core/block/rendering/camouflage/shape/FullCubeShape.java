package ic2.core.block.rendering.camouflage.shape;

import ic2.api.events.RetextureEvent;
import ic2.core.platform.rendering.models.ShapeBuilder;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FullCubeShape extends CamouflageShape {
   static final List<ShapeBuilder.Quad>[] QUADS = new ShapeBuilder()
      .newQuad(new AABB(0.0, 0.0, 0.0, 16.0, 16.0, 16.0).inflate(0.0010000000474974513))
      .addCube(0.0F, 0.0F, 16.0F, 16.0F)
      .finish()
      .getMappedQuads();

   @Override
   protected boolean isSideFull(BlockState state, Direction side) {
      return true;
   }

   @Override
   protected void generateBakedQuads(
      BlockState original, Direction dir, RetextureEvent.Rotation[] rotations, List<BakedQuad> blockQuads, List<BakedQuad>[] result
   ) {
      List<ShapeBuilder.Quad> quads = QUADS[dir.get3DDataValue()];
      if (!quads.isEmpty()) {
         List<BakedQuad> out = result[dir.get3DDataValue()];
         int i = 0;

         for (int m = blockQuads.size(); i < m; i++) {
            BakedQuad baked = blockQuads.get(i);

            for (ShapeBuilder.Quad quad : quads) {
               out.add(createQuad(baked, quad, i, dir, rotations[i]));
            }
         }
      }
   }
}
