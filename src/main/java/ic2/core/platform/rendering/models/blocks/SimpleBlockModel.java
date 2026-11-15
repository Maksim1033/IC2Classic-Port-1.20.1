package ic2.core.platform.rendering.models.blocks;

import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.platform.rendering.features.block.IColoredBlockModel;
import ic2.core.platform.rendering.features.block.ILockedBlockUV;
import ic2.core.platform.rendering.features.block.IRotatableBlock;
import ic2.core.platform.rendering.models.BaseModel;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class SimpleBlockModel extends BaseModel {
   public static final BlockFaceUV DEFAULT_UVS = new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0);
   protected BlockState state;
   protected IBlockModel model;
   protected List<BakedQuad>[] quads = multiList(7);
   protected IBlockModifiers mod;

   public SimpleBlockModel(BlockState state, IBlockModel model) {
      this.state = state;
      this.model = model;
   }

   @Override
   public void init() {
      this.setParticleTexture(this.model.getSpriteForParticle(this.state));
      this.mod = this.state.getBlock() instanceof IBlockModifiers ? (IBlockModifiers)this.state.getBlock() : null;
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
      AABB box = this.model.getModelBounds(this.state);
      if (full) {
         for (Direction side : DirectionList.ALL) {
            BlockModelRotation sideRotation = this.getRotation(blockFacing, side, rotation);
            BlockElementFace face = this.createBlockFace(side, colorModel == null ? -1 : colorModel.getTintedIndexFor(this.state, side, 0));
            TextureAtlasSprite sprite = IC2Models.getSafeIcon(this.model.getSpriteForState(this.state, side));
            this.quads[Direction.rotate(sideRotation.getRotation().getMatrix(), side).get3DDataValue()]
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
            TextureAtlasSprite sprite = IC2Models.getSafeIcon(this.model.getSpriteForState(this.state, side));
            this.quads[6]
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
      return this.quads[side == null ? 6 : side.get3DDataValue()];
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }

   protected BlockModelRotation getRotation(Direction facing, Direction side, BlockModelRotation defaultRotation) {
      return defaultRotation;
   }

   protected BlockElementFace createBlockFace(Direction side, int color) {
      return this.mod != null
         ? new BlockElementFace(
            null,
            color,
            "",
            new BlockFaceUV(
               this.mod.hasCustomTextureUVs(this.state, side) ? this.mod.getCustomTextureUVs(this.state, side) : new float[]{0.0F, 0.0F, 16.0F, 16.0F},
               this.mod.hasTextureRotation(this.state, side) ? this.mod.getTextureRotation(this.state, side) : 0
            )
         )
         : new BlockElementFace(null, color, "", DEFAULT_UVS);
   }
}
