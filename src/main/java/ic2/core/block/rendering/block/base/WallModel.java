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
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraftforge.client.model.data.ModelData;

public class WallModel extends BaseModel {
   private static final String JSON_DATA = "{\"display\":{\"gui\":{\"rotation\":[30,45,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"head\":{\"rotation\":[0,0,0],\"translation\":[0,0,0],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,-45,0],\"translation\":[0,0,0],\"scale\":[0.40,0.40,0.40]},\"thirdperson_lefthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}, \"thirdperson_righthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}}}";
   private static final ResourceLocation WALL = new ResourceLocation("ic2:wall");
   private static final ShapeBuilder POLE = new ShapeBuilder()
      .newQuad(4.0, 0.0, 4.0, 12.0, 16.0, 12.0)
      .addFaces(DirectionList.VERTICAL, 4.0F, 4.0F, 12.0F, 12.0F)
      .addFaces(DirectionList.Z_AXIS, 4.0F, 0.0F, 12.0F, 16.0F)
      .addFaces(DirectionList.X_AXIS, 4.0F, 0.0F, 12.0F, 16.0F)
      .finish();
   private static final ShapeBuilder SIDE_WALL = new ShapeBuilder()
      .newQuad(5.0, 0.1599999964237213, 8.0, 11.0, 14.0, 16.0)
      .addCube(
         new float[]{11.0F, 8.0F, 5.0F, 16.0F},
         new float[]{5.0F, 8.0F, 11.0F, 16.0F},
         new float[]{5.0F, 0.0F, 11.0F, 13.0F},
         new float[]{5.0F, 0.0F, 11.0F, 13.0F},
         new float[]{8.0F, 0.0F, 16.0F, 14.0F},
         new float[]{0.0F, 0.0F, 8.0F, 14.0F}
      )
      .finish();
   private static final ShapeBuilder ITEM_SIDE_WALL = new ShapeBuilder()
      .newQuad(5.0, 0.1599999964237213, 8.0, 11.0, 14.0, 16.0)
      .addFaces(DirectionList.VERTICAL, 5.0F, 0.0F, 11.0F, 16.0F)
      .addFaces(DirectionList.Z_AXIS, 5.0F, 3.0F, 11.0F, 16.0F)
      .addFaces(DirectionList.X_AXIS, 0.0F, 3.0F, 16.0F, 16.0F)
      .finish();
   private BlockState state;
   private String textureFolder;
   private String textureName;
   private List<BakedQuad> blockQuads = CollectionUtils.<BakedQuad>createList();
   private List<BakedQuad> itemQuads = CollectionUtils.<BakedQuad>createList();

   public WallModel(BlockState state, String textureFolder, String textureName) {
      this.state = state;
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   @Override
   public void init() {
      this.setTransforms(
         IC2Models.getTransformMap(
            IC2Models.BLOCK_LOCATION,
            WALL,
            "{\"display\":{\"gui\":{\"rotation\":[30,45,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"head\":{\"rotation\":[0,0,0],\"translation\":[0,0,0],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,-45,0],\"translation\":[0,0,0],\"scale\":[0.40,0.40,0.40]},\"thirdperson_lefthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}, \"thirdperson_righthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}}}"
         )
      );
      this.setParticleTexture(IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.textureName));
      POLE.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y0, null, true, this.itemQuads);
      ITEM_SIDE_WALL.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y90, null, true, this.itemQuads);
      ITEM_SIDE_WALL.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y270, null, true, this.itemQuads);
      if ((Boolean)this.state.getValue(WallBlock.UP)) {
         POLE.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y0, null, true, this.blockQuads);
      }

      if (this.state.getValue(WallBlock.NORTH_WALL) != WallSide.NONE) {
         SIDE_WALL.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y180, null, true, this.blockQuads);
      }

      if (this.state.getValue(WallBlock.EAST_WALL) != WallSide.NONE) {
         SIDE_WALL.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y270, null, true, this.blockQuads);
      }

      if (this.state.getValue(WallBlock.SOUTH_WALL) != WallSide.NONE) {
         SIDE_WALL.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y0, null, true, this.blockQuads);
      }

      if (this.state.getValue(WallBlock.WEST_WALL) != WallSide.NONE) {
         SIDE_WALL.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y90, null, true, this.blockQuads);
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side != null) {
         return empty();
      } else {
         return state == null ? this.itemQuads : this.blockQuads;
      }
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
