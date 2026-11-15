package ic2.core.block.rendering.block.tubes;

import org.joml.Vector3f;
import ic2.api.util.DirectionList;
import ic2.core.block.base.misc.IAdvancedTubeBlock;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.rendering.props.CableProperty;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class CableModel extends BaseModel {
   protected List<BakedQuad>[] quads = CollectionUtils.createLists(64);
   protected List<BakedQuad>[] anchor = CollectionUtils.createLists(64);
   protected ITubeBlock tube;
   protected IAdvancedTubeBlock advTube;
   protected BlockState state;

   public CableModel(ITubeBlock tube, BlockState state) {
      this.tube = tube;
      this.advTube = tube instanceof IAdvancedTubeBlock ? (IAdvancedTubeBlock)tube : null;
      this.state = state;
   }

   @Override
   public void init() {
      this.setParticleTexture(this.tube.getTexture(this.state, false, null, Direction.NORTH));
      float radius = this.tube.getRadius(this.state);
      float min = 8.0F - radius;
      float max = 8.0F + radius;
      TextureAtlasSprite sprite = IC2Models.getSafeIcon(IC2Textures.getMappedEntriesBlockIC2("misc").get("mining_pipe_shaft"));
      Byte2ObjectMap<BakedQuad> coreQuads = this.generateCoreQuads(min, max);
      Byte2ObjectMap<List<BakedQuad>> sideQuads = new Byte2ObjectOpenHashMap();
      Byte2ObjectMap<List<BakedQuad>> anchorQuadList = new Byte2ObjectOpenHashMap();

      for (Direction side : DirectionList.ALL) {
         sideQuads.put((byte)side.get3DDataValue(), this.generateSideQuads(side, min, max));
         anchorQuadList.put((byte)side.get3DDataValue(), this.generateAnchorQuads(sprite, side, min, max));
      }

      for (int i = 0; i < 64; i++) {
         DirectionList rotation = DirectionList.ofNumber(i);
         List<BakedQuad> quadList = this.quads[i];

         for (Direction side : rotation) {
            quadList.addAll((Collection<? extends BakedQuad>)sideQuads.get((byte)side.get3DDataValue()));
            this.anchor[i].addAll((Collection<? extends BakedQuad>)anchorQuadList.get((byte)side.get3DDataValue()));
         }

         for (Direction side : rotation.invert()) {
            quadList.add((BakedQuad)coreQuads.get((byte)side.get3DDataValue()));
         }
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side != null || this.isTranslucent(type)) {
         return empty();
      } else if (extraData.has(CableProperty.INSTANCE)) {
         int index = (Integer)extraData.get(CableProperty.INSTANCE);
         List<BakedQuad> list = CollectionUtils.<BakedQuad>createList();
         list.addAll(this.quads[index >> 6 & 63]);
         list.addAll(this.anchor[index & 63]);
         return list;
      } else {
         return this.quads[0];
      }
   }

   @Override
   public boolean useAmbientOcclusion() {
      return false;
   }

   protected DirectionList selectEndSides(DirectionList list, Direction facing) {
      return this.advTube == null ? list : this.advTube.selectEndSides(list, facing, this.state);
   }

   protected List<BakedQuad> generateAnchorQuads(TextureAtlasSprite sprite, Direction dir, float min, float max) {
      List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();
      Tuple<Vector3f, Vector3f> position = getPosForSide(dir, min, max);

      for (Direction side : DirectionList.ALL.remove(dir.getOpposite())) {
         quads.add(
            QuadBaker.BAKER
               .bakeQuad(
                  (Vector3f)position.getA(),
                  (Vector3f)position.getB(),
                  this.getFace(dir, side, min, max),
                  sprite,
                  side,
                  BlockModelRotation.X0_Y0,
                  null,
                  true,
                  LOCATION
               )
         );
      }

      return quads;
   }

   protected List<BakedQuad> generateSideQuads(Direction dir, float min, float max) {
      List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();
      Tuple<Vector3f, Vector3f> position = getPosForSide(dir, min, max);

      for (Direction side : this.selectEndSides(DirectionList.ALL.remove(dir.getOpposite()), dir)) {
         quads.add(
            QuadBaker.BAKER
               .bakeQuad(
                  (Vector3f)position.getA(),
                  (Vector3f)position.getB(),
                  this.getFace(dir, side, min, max),
                  this.tube.getTexture(this.state, false, dir, side),
                  side,
                  BlockModelRotation.X0_Y0,
                  null,
                  true,
                  LOCATION
               )
         );
      }

      return quads;
   }

   protected List<BakedQuad> generateSideQuads(Direction dir, float min, float max, float textureMin, float textureMax) {
      List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();
      Tuple<Vector3f, Vector3f> position = getPosForSide(dir, min, max);

      for (Direction side : this.selectEndSides(DirectionList.ALL.remove(dir), dir)) {
         quads.add(
            QuadBaker.BAKER
               .bakeQuad(
                  (Vector3f)position.getA(),
                  (Vector3f)position.getB(),
                  this.getFace(dir, side, textureMin, textureMax),
                  this.tube.getTexture(this.state, false, dir, side),
                  side,
                  BlockModelRotation.X0_Y0,
                  null,
                  true,
                  LOCATION
               )
         );
      }

      return quads;
   }

   protected Byte2ObjectMap<BakedQuad> generateCoreQuads(float min, float max) {
      Vector3f minF = new Vector3f(min, min, min);
      Vector3f maxF = new Vector3f(max, max, max);
      BlockElementFace face = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{min, min, max, max}, 0));
      Byte2ObjectMap<BakedQuad> quads = new Byte2ObjectOpenHashMap();

      for (Direction side : DirectionList.ALL) {
         quads.put(
            (byte)side.get3DDataValue(),
            QuadBaker.BAKER
               .bakeQuad(minF, maxF, face, this.tube.getTexture(this.state, true, null, side), side, BlockModelRotation.X0_Y0, null, true, LOCATION)
         );
      }

      return quads;
   }

   public static Tuple<Vector3f, Vector3f> getPosForSide(Direction facing, float min, float max) {
      switch (facing) {
         case DOWN:
            return new Tuple(new Vector3f(min, 0.0F, min), new Vector3f(max, min, max));
         case UP:
            return new Tuple(new Vector3f(min, max, min), new Vector3f(max, 16.0F, max));
         case NORTH:
            return new Tuple(new Vector3f(min, min, 0.0F), new Vector3f(max, max, min));
         case SOUTH:
            return new Tuple(new Vector3f(min, min, max), new Vector3f(max, max, 16.0F));
         case WEST:
            return new Tuple(new Vector3f(0.0F, min, min), new Vector3f(min, max, max));
         case EAST:
            return new Tuple(new Vector3f(max, min, min), new Vector3f(16.0F, max, max));
         default:
            return new Tuple(new Vector3f(min, min, min), new Vector3f(max, max, max));
      }
   }

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
         if (dir.getAxis().isVertical()) {
            return side.getAxisDirection() == AxisDirection.NEGATIVE
               ? new BlockElementFace(
                  null, -1, "", new BlockFaceUV(new float[]{0.0F, min, min, max}, dir.getAxisDirection() == AxisDirection.NEGATIVE ? 270 : 90)
               )
               : new BlockElementFace(
                  null, -1, "", new BlockFaceUV(new float[]{max, min, 16.0F, max}, dir.getAxisDirection() == AxisDirection.NEGATIVE ? 90 : 270)
               );
         } else {
            return side.getAxisDirection() == AxisDirection.NEGATIVE
               ? new BlockElementFace(
                  null,
                  -1,
                  "",
                  new BlockFaceUV(new float[]{0.0F, min, min, max}, dir.get2DDataValue() * 180 + (dir.getAxisDirection() == AxisDirection.NEGATIVE ? 0 : 180))
               )
               : new BlockElementFace(
                  null,
                  -1,
                  "",
                  new BlockFaceUV(new float[]{max, min, 16.0F, max}, dir.get2DDataValue() * 180 + (dir.getAxisDirection() == AxisDirection.NEGATIVE ? 0 : 180))
               );
         }
      } else {
         return null;
      }
   }
}
