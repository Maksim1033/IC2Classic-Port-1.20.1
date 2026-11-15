package ic2.core.block.rendering.block.base;

import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class StairsModel extends BaseModel {
   private static final String JSON_DATA = "{\"display\":{\"gui\":{\"rotation\":[30,45,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"head\":{\"rotation\":[0,0,0],\"translation\":[0,0,0],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,-45,0],\"translation\":[0,0,0],\"scale\":[0.40,0.40,0.40]},\"thirdperson_lefthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}, \"thirdperson_righthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}}}";
   private static final ResourceLocation STAIRS = new ResourceLocation("ic2:stairs");
   private static final ShapeBuilder STRAIGHT = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
      .addFace(Direction.UP, 0.0F, 0.0F, 16.0F, 16.0F)
      .addCulledFace(Direction.DOWN, 0.0F, 0.0F, 16.0F, 16.0F)
      .addCulledFaces(DirectionList.HORIZONTAL, 0.0F, 8.0F, 16.0F, 16.0F)
      .finish()
      .newQuad(8.0, 8.0, 0.0, 16.0, 16.0, 16.0)
      .addCulledFace(Direction.UP, 8.0F, 0.0F, 16.0F, 16.0F)
      .addCulledFace(Direction.NORTH, 0.0F, 0.0F, 8.0F, 8.0F)
      .addCulledFace(Direction.SOUTH, 8.0F, 0.0F, 16.0F, 8.0F)
      .addFace(Direction.WEST, 0.0F, 0.0F, 16.0F, 8.0F)
      .addCulledFace(Direction.EAST, 0.0F, 0.0F, 16.0F, 8.0F)
      .finish();
   private static final ShapeBuilder INNER = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
      .addFace(Direction.UP, 0.0F, 0.0F, 16.0F, 16.0F)
      .addCulledFace(Direction.DOWN, 0.0F, 0.0F, 16.0F, 16.0F)
      .addCulledFaces(DirectionList.HORIZONTAL, 0.0F, 8.0F, 16.0F, 16.0F)
      .finish()
      .newQuad(8.0, 8.0, 0.0, 16.0, 16.0, 16.0)
      .addCulledFace(Direction.UP, 8.0F, 0.0F, 16.0F, 16.0F)
      .addCulledFace(Direction.NORTH, 0.0F, 0.0F, 8.0F, 8.0F)
      .addCulledFace(Direction.SOUTH, 8.0F, 0.0F, 16.0F, 8.0F)
      .addFace(Direction.WEST, 0.0F, 0.0F, 16.0F, 8.0F)
      .addCulledFace(Direction.EAST, 0.0F, 0.0F, 16.0F, 8.0F)
      .finish()
      .newQuad(0.0, 8.0, 8.0, 8.0, 16.0, 16.0)
      .addFace(Direction.UP, 0.0F, 8.0F, 8.0F, 16.0F)
      .addFace(Direction.NORTH, 8.0F, 0.0F, 16.0F, 8.0F)
      .addCulledFace(Direction.SOUTH, 0.0F, 0.0F, 8.0F, 8.0F)
      .addCulledFace(Direction.WEST, 8.0F, 0.0F, 16.0F, 8.0F)
      .finish();
   private static final ShapeBuilder OUTER = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
      .addFace(Direction.UP, 0.0F, 0.0F, 16.0F, 16.0F)
      .addCulledFace(Direction.DOWN, 0.0F, 0.0F, 16.0F, 16.0F)
      .addCulledFaces(DirectionList.HORIZONTAL, 0.0F, 8.0F, 16.0F, 16.0F)
      .finish()
      .newQuad(8.0, 8.0, 8.0, 16.0, 16.0, 16.0)
      .addCulledFace(Direction.UP, 8.0F, 8.0F, 16.0F, 16.0F)
      .addFace(Direction.NORTH, 0.0F, 0.0F, 8.0F, 8.0F)
      .addFace(Direction.SOUTH, 8.0F, 0.0F, 16.0F, 8.0F)
      .addFace(Direction.WEST, 0.0F, 0.0F, 16.0F, 8.0F)
      .addCulledFace(Direction.EAST, 0.0F, 0.0F, 16.0F, 8.0F)
      .finish();
   private BlockState state;
   private String textureName;
   private String textureFolder;
   private List<BakedQuad>[] quads = CollectionUtils.createLists(7);

   public StairsModel(BlockState state, String textureFolder, String textureName) {
      this.state = state;
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   @Override
   public void init() {
      this.setTransforms(
         IC2Models.getTransformMap(
            IC2Models.BLOCK_LOCATION,
            STAIRS,
            "{\"display\":{\"gui\":{\"rotation\":[30,45,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"head\":{\"rotation\":[0,0,0],\"translation\":[0,0,0],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,-45,0],\"translation\":[0,0,0],\"scale\":[0.40,0.40,0.40]},\"thirdperson_lefthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}, \"thirdperson_righthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}}}"
         )
      );
      TextureAtlasSprite texture = IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.textureName);
      this.setParticleTexture(texture);
      boolean top = this.state.getValue(StairBlock.HALF) == Half.TOP;
      StairsShape shape = (StairsShape)this.state.getValue(StairBlock.SHAPE);
      int rotate = shape == StairsShape.STRAIGHT ? 90 : (top ? 180 : 90) - shape.ordinal() % 2 * 90;
      BlockModelRotation rotation = BlockModelRotation.by(top ? 180 : 0, ((Direction)this.state.getValue(StairBlock.FACING)).get2DDataValue() * 90 + rotate);
      if (shape == StairsShape.STRAIGHT) {
         STRAIGHT.buildLockedQuads(texture, rotation, null, true, this.quads);
      } else if (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) {
         INNER.buildLockedQuads(texture, rotation, null, true, this.quads);
      } else if (shape == StairsShape.OUTER_LEFT || shape == StairsShape.OUTER_RIGHT) {
         OUTER.buildLockedQuads(texture, rotation, null, true, this.quads);
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      return this.quads[side == null ? 6 : side.get3DDataValue()];
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
