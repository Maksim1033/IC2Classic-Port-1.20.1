package ic2.core.block.rendering.camouflage.shape;

import ic2.api.events.RetextureEvent;
import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.models.ShapeBuilder;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public class SlabShape extends CamouflageShape {
   static final List<ShapeBuilder.Quad>[] BOTTOM = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
      .addCulledFaces(DirectionList.HORIZONTAL, 0.0F, 0.0F, 16.0F, 8.0F)
      .addCulledFace(Direction.DOWN, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFace(Direction.UP, 0.0F, 0.0F, 16.0F, 16.0F)
      .finish()
      .getMappedQuads();
   static final List<ShapeBuilder.Quad>[] TOP = new ShapeBuilder()
      .newQuad(0.0, 8.0, 0.0, 16.0, 16.0, 16.0)
      .addCulledFaces(DirectionList.HORIZONTAL, 0.0F, 8.0F, 16.0F, 16.0F)
      .addCulledFace(Direction.UP, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFace(Direction.DOWN, 0.0F, 0.0F, 16.0F, 16.0F)
      .finish()
      .getMappedQuads();
   static final List<ShapeBuilder.Quad>[] DOUBLE = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
      .addCulledCube(0.0F, 0.0F, 16.0F, 16.0F)
      .finish()
      .getMappedQuads();

   @Override
   protected boolean isSideFull(BlockState state, Direction side) {
      SlabType type = (SlabType)state.getValue(SlabBlock.TYPE);
      return type == SlabType.DOUBLE || side == Direction.DOWN && type == SlabType.BOTTOM || side == Direction.UP && type == SlabType.TOP;
   }

   private List<ShapeBuilder.Quad>[] getQuads(SlabType type) {
      return type == SlabType.DOUBLE ? DOUBLE : (type == SlabType.BOTTOM ? BOTTOM : TOP);
   }

   @Override
   protected void generateBakedQuads(
      BlockState original, Direction dir, RetextureEvent.Rotation[] rotations, List<BakedQuad> blockQuads, List<BakedQuad>[] result
   ) {
      List<ShapeBuilder.Quad> quads = this.getQuads((SlabType)original.getValue(SlabBlock.TYPE))[dir.get3DDataValue()];
      if (!quads.isEmpty()) {
         List<BakedQuad> out = result[dir.get3DDataValue()];
         List<BakedQuad> allOut = result[6];
         int i = 0;

         for (int m = blockQuads.size(); i < m; i++) {
            BakedQuad baked = blockQuads.get(i);

            for (ShapeBuilder.Quad quad : quads) {
               (quad.isCull() ? out : allOut).add(createQuad(baked, quad, i, dir, rotations[i]));
            }
         }
      }
   }
}
