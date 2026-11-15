package ic2.core.block.rendering.block;

import org.joml.Vector3f;
import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IColoredBlockModel;
import ic2.core.platform.rendering.features.block.ILockedBlockUV;
import ic2.core.platform.rendering.features.block.IRotatableBlock;
import ic2.core.platform.rendering.misc.ModelTransform;
import ic2.core.platform.rendering.models.blocks.SimpleBlockModel;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;

public class FluidExpansionModel extends SimpleBlockModel {
   public FluidExpansionModel(BlockState state, IBlockModel model) {
      super(state, model);
   }

   @Override
   public void init() {
      super.init();
      boolean locked = this.state.getBlock() instanceof ILockedBlockUV;
      Direction blockFacing = Direction.NORTH;
      BlockModelRotation rotation = BlockModelRotation.X0_Y0;
      if (this.model instanceof IRotatableBlock facing && facing.hasRotation(this.state)) {
         blockFacing = facing.getRotation(this.state);
         Tuple<Integer, Integer> result = QuadBaker.getModelRotation(blockFacing);
         rotation = BlockModelRotation.by((Integer)result.getA(), (Integer)result.getB());
      }

      IColoredBlockModel colorModel = this.model instanceof IColoredBlockModel ? (IColoredBlockModel)this.model : null;
      Vector3f max = new Vector3f(0.005F, 0.005F, 0.005F);
      Vector3f min = new Vector3f(15.995F, 15.995F, 15.995F);

      for (Direction side : DirectionList.ALL) {
         BlockModelRotation sideRotation = this.getRotation(blockFacing, side, rotation);
         BlockElementFace face = this.createBlockFace(side, colorModel == null ? -1 : colorModel.getTintedIndexFor(this.state, side.getOpposite(), 0));
         TextureAtlasSprite sprite = IC2Models.getSafeIcon(this.model.getSpriteForState(this.state, side.getOpposite()));
         this.quads[6]
            .add(
               locked
                  ? QuadBaker.BAKER
                     .bakeQuad(
                        QuadBaker.getMin(side, min, max),
                        QuadBaker.getMax(side, min, max),
                        face,
                        sprite,
                        side,
                        new ModelTransform(sideRotation, true),
                        null,
                        true,
                        LOCATION
                     )
                  : QuadBaker.BAKER
                     .bakeQuad(QuadBaker.getMin(side, min, max), QuadBaker.getMax(side, min, max), face, sprite, side, sideRotation, null, true, LOCATION)
            );
      }
   }
}
