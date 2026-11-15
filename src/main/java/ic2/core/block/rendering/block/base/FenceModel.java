package ic2.core.block.rendering.block.base;

import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class FenceModel extends BaseModel {
   private static final ResourceLocation FENCE = new ResourceLocation("ic2:fence");
   private static final String JSON_DATA = "{\"display\":{\"gui\":{\"rotation\":[30,225,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"fixed\":{\"rotation\":[0,90,0],\"translation\":[0,0,0],\"scale\":[1,1,1]}}}";
   private static final float[][][] FACES = new float[][][]{
      {{7.0F, 0.0F, 9.0F, 9.0F}, {7.0F, 0.0F, 9.0F, 9.0F}, {7.0F, 1.0F, 9.0F, 4.0F}, new float[0], {0.0F, 1.0F, 9.0F, 4.0F}, {0.0F, 1.0F, 9.0F, 4.0F}},
      {{7.0F, 0.0F, 9.0F, 9.0F}, {7.0F, 0.0F, 9.0F, 9.0F}, {7.0F, 7.0F, 9.0F, 10.0F}, new float[0], {0.0F, 7.0F, 9.0F, 10.0F}, {0.0F, 7.0F, 9.0F, 10.0F}}
   };
   private static final BooleanProperty[] CONNECTION = new BooleanProperty[]{FenceBlock.NORTH, FenceBlock.EAST, FenceBlock.SOUTH, FenceBlock.WEST};
   private static final BlockElementFace VERTICAL = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{6.0F, 6.0F, 10.0F, 10.0F}, 0));
   private static final BlockElementFace HORIZONTAL = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{6.0F, 0.0F, 10.0F, 16.0F}, 0));
   private BlockState base;
   private List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();
   private List<BakedQuad> itemQuads = CollectionUtils.<BakedQuad>createList();
   private String textureFolder;
   private String textureName;

   public FenceModel(BlockState state, String textureFolder, String textureName) {
      this.base = state;
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   @Override
   public void init() {
      this.setTransforms(
         IC2Models.getTransformMap(
            IC2Models.BLOCK_LOCATION,
            FENCE,
            "{\"display\":{\"gui\":{\"rotation\":[30,225,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"fixed\":{\"rotation\":[0,90,0],\"translation\":[0,0,0],\"scale\":[1,1,1]}}}"
         )
      );
      this.setParticleTexture(IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.textureName));
      AABB center = new AABB(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
      AABB lower = new AABB(7.0, 6.0, 0.0, 9.0, 9.0, 6.0);
      AABB upper = new AABB(7.0, 12.0, 0.0, 9.0, 15.0, 6.0);

      for (Direction direction : DirectionList.ALL) {
         this.quads
            .add(
               QuadBaker.createLockedQuad(
                  center, direction, direction.getAxis().isVertical() ? VERTICAL : HORIZONTAL, this.getParticleIcon(), BlockModelRotation.X0_Y0, null, true
               )
            );
         if (direction != Direction.SOUTH) {
            for (Direction side : DirectionList.HORIZONTAL) {
               if ((Boolean)this.base.getValue(CONNECTION[side.get2DDataValue()])) {
                  this.quads
                     .add(
                        QuadBaker.createLockedQuad(
                           upper,
                           direction,
                           new BlockElementFace(null, 0, "", new BlockFaceUV(FACES[0][direction.get3DDataValue()], 0)),
                           this.getParticleIcon(),
                           BlockModelRotation.values()[side.get2DDataValue()],
                           null,
                           true
                        )
                     );
                  this.quads
                     .add(
                        QuadBaker.createLockedQuad(
                           lower,
                           direction,
                           new BlockElementFace(null, 0, "", new BlockFaceUV(FACES[1][direction.get3DDataValue()], 0)),
                           this.getParticleIcon(),
                           BlockModelRotation.values()[side.get2DDataValue()],
                           null,
                           true
                        )
                     );
               }
            }
         }
      }

      AABB left = new AABB(6.0, 0.0, 0.0, 10.0, 16.0, 4.0);
      AABB right = new AABB(6.0, 0.0, 12.0, 10.0, 16.0, 16.0);
      AABB middleTop = new AABB(7.0, 12.0, -2.0, 9.0, 15.0, 18.0);
      AABB middleBottom = new AABB(7.0, 6.0, -2.0, 9.0, 9.0, 18.0);

      for (Direction directionx : DirectionList.ALL) {
         this.itemQuads
            .add(
               QuadBaker.createLockedQuad(
                  left, directionx, directionx.getAxis().isVertical() ? VERTICAL : HORIZONTAL, this.getParticleIcon(), BlockModelRotation.X0_Y90, null, true
               )
            );
         this.itemQuads
            .add(
               QuadBaker.createLockedQuad(
                  right, directionx, directionx.getAxis().isVertical() ? VERTICAL : HORIZONTAL, this.getParticleIcon(), BlockModelRotation.X0_Y90, null, true
               )
            );
         if (directionx != Direction.SOUTH) {
            this.itemQuads
               .add(
                  QuadBaker.createLockedQuad(
                     middleTop,
                     directionx,
                     new BlockElementFace(null, 0, "", new BlockFaceUV(FACES[0][directionx.get3DDataValue()], 0)),
                     this.getParticleIcon(),
                     BlockModelRotation.X0_Y90,
                     null,
                     true
                  )
               );
            this.itemQuads
               .add(
                  QuadBaker.createLockedQuad(
                     middleBottom,
                     directionx,
                     new BlockElementFace(null, 0, "", new BlockFaceUV(FACES[1][directionx.get3DDataValue()], 0)),
                     this.getParticleIcon(),
                     BlockModelRotation.X0_Y90,
                     null,
                     true
                  )
               );
         }
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      return side == null ? (state == null ? this.itemQuads : this.quads) : empty();
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
