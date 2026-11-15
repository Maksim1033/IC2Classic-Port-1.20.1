package ic2.core.platform.rendering.models.blocks;

import ic2.api.util.DirectionList;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IColoredBlockModel;
import ic2.core.platform.rendering.features.block.ILockedBlockUV;
import ic2.core.platform.rendering.features.block.IRotatableBlock;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;

public class CamouflageableBlock extends SimpleBlockModel {
   List<BakedQuad>[] overlayQuad = multiList(7);
   boolean transparency;
   ITextureProvider overlay;

   public CamouflageableBlock(BlockState state, IBlockModel model, ITextureProvider overlay, boolean transparency) {
      super(state, model);
      this.overlay = overlay;
      this.transparency = transparency;
   }

   @Override
   public void init() {
      super.init();
      boolean full = this.model.isFullCube(this.state);
      boolean locked = this.state.getBlock() instanceof ILockedBlockUV;
      Direction blockFacing = Direction.NORTH;
      BlockModelRotation rotation = BlockModelRotation.X0_Y0;
      if (this.model instanceof IRotatableBlock facing && facing.hasRotation(this.state)) {
         blockFacing = facing.getRotation(this.state);
         Tuple<Integer, Integer> result = QuadBaker.getModelRotation(blockFacing);
         rotation = BlockModelRotation.by((Integer)result.getA(), (Integer)result.getB());
      }

      IColoredBlockModel colorModel = this.model instanceof IColoredBlockModel ? (IColoredBlockModel)this.model : null;
      AABB box = this.model.getModelBounds(this.state).inflate(0.005);
      if (full) {
         for (Direction side : DirectionList.ALL) {
            BlockModelRotation sideRotation = this.getRotation(blockFacing, side, rotation);
            BlockElementFace face = this.createBlockFace(side, colorModel == null ? -1 : colorModel.getTintedIndexFor(this.state, side, 0));
            TextureAtlasSprite sprite = IC2Models.getSafeIcon(this.overlay.getTexture(this.state, side));
            this.overlayQuad[Direction.rotate(sideRotation.getRotation().getMatrix(), side).get3DDataValue()]
               .add(
                  locked
                     ? QuadBaker.createLockedQuad(box, side, face, sprite, sideRotation, null, true)
                     : QuadBaker.createQuad(box, side, face, sprite, sideRotation, null, true)
               );
         }
      } else {
         for (Direction side : DirectionList.ALL) {
            BlockModelRotation sideRotation = this.getRotation(blockFacing, side, rotation);
            BlockElementFace face = this.createBlockFace(side, colorModel == null ? -1 : colorModel.getTintedIndexFor(this.state, side, 0));
            TextureAtlasSprite sprite = IC2Models.getSafeIcon(this.overlay.getTexture(this.state, side));
            this.overlayQuad[6]
               .add(
                  locked
                     ? QuadBaker.createLockedQuad(box, side, face, sprite, sideRotation, null, true)
                     : QuadBaker.createQuad(box, side, face, sprite, sideRotation, null, true)
               );
         }
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType type) {
      Function<RenderType, CamouflageShape.QuadResults> results = (Function<RenderType, CamouflageShape.QuadResults>)data.get(CamouflageProperty.INSTANCE);
      return results == null && !this.transparency && this.isTranslucent(type)
         ? empty()
         : this.getResult(side == null ? 6 : side.get3DDataValue(), results == null ? null : results.apply(type), type);
   }

   private List<BakedQuad> getResult(int index, CamouflageShape.QuadResults results, RenderType type) {
      if (results != null) {
         if (index != 6) {
            return this.overlayQuad[index];
         } else {
            return this.overlayQuad[6].isEmpty() ? results.getQuads(type) : CollectionUtils.merge(results.getQuads(type), this.overlayQuad[index]);
         }
      } else if (this.quads[index].isEmpty() && this.overlayQuad[index].isEmpty()) {
         return empty();
      } else if (!this.quads[index].isEmpty() && this.overlayQuad[index].isEmpty()) {
         return this.quads[index];
      } else {
         return this.quads[index].isEmpty() && !this.overlayQuad[index].isEmpty()
            ? this.overlayQuad[index]
            : CollectionUtils.merge(this.quads[index], this.overlayQuad[index]);
      }
   }
}
