package ic2.core.block.rendering.block;

import org.joml.Vector3f;
import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class DynamiteModel extends BaseModel {
   Direction direction;
   List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();

   public DynamiteModel(TextureAtlasSprite texture, Direction side) {
      this.setParticleTexture(texture);
      this.direction = side;
   }

   @Override
   public void init() {
      if (this.direction == Direction.DOWN) {
         this.quads
            .add(
               QuadBaker.BAKER
                  .bakeQuad(
                     new Vector3f(7.0F, 0.0F, 7.0F),
                     new Vector3f(9.0F, 10.0F, 9.0F),
                     new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{7.0F, 13.0F, 9.0F, 15.0F}, 0)),
                     this.getParticleIcon(),
                     Direction.DOWN,
                     BlockModelRotation.X0_Y0,
                     null,
                     false,
                     LOCATION
                  )
            );
         this.quads
            .add(
               QuadBaker.BAKER
                  .bakeQuad(
                     new Vector3f(7.0F, 0.0F, 7.0F),
                     new Vector3f(9.0F, 10.0F, 9.0F),
                     new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{7.0F, 6.0F, 9.0F, 8.0F}, 0)),
                     this.getParticleIcon(),
                     Direction.UP,
                     BlockModelRotation.X0_Y0,
                     null,
                     false,
                     LOCATION
                  )
            );
         this.quads
            .add(
               QuadBaker.BAKER
                  .bakeQuad(
                     new Vector3f(7.0F, 0.0F, 0.0F),
                     new Vector3f(9.0F, 16.0F, 16.0F),
                     new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)),
                     this.getParticleIcon(),
                     Direction.WEST,
                     BlockModelRotation.X0_Y0,
                     null,
                     false,
                     LOCATION
                  )
            );
         this.quads
            .add(
               QuadBaker.BAKER
                  .bakeQuad(
                     new Vector3f(7.0F, 0.0F, 0.0F),
                     new Vector3f(9.0F, 16.0F, 16.0F),
                     new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)),
                     this.getParticleIcon(),
                     Direction.EAST,
                     BlockModelRotation.X0_Y0,
                     null,
                     false,
                     LOCATION
                  )
            );
         this.quads
            .add(
               QuadBaker.BAKER
                  .bakeQuad(
                     new Vector3f(0.0F, 0.0F, 7.0F),
                     new Vector3f(16.0F, 16.0F, 9.0F),
                     new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)),
                     this.getParticleIcon(),
                     Direction.NORTH,
                     BlockModelRotation.X0_Y0,
                     null,
                     false,
                     LOCATION
                  )
            );
         this.quads
            .add(
               QuadBaker.BAKER
                  .bakeQuad(
                     new Vector3f(0.0F, 0.0F, 7.0F),
                     new Vector3f(16.0F, 16.0F, 9.0F),
                     new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)),
                     this.getParticleIcon(),
                     Direction.SOUTH,
                     BlockModelRotation.X0_Y0,
                     null,
                     false,
                     LOCATION
                  )
            );
      } else {
         Vector3f vector = new Vector3f(0.0F, 3.5F, 8.0F);
         vector.mul(0.0625F);
         BlockModelRotation rot = BlockModelRotation.values()[DirectionList.rotateAround(this.direction, Axis.Y).get2DDataValue()];

         for (Direction facing : DirectionList.ALL) {
            this.quads
               .add(
                  QuadBaker.BAKER
                     .bakeQuad(
                        new Vector3f(-1.0F, 3.5F, 7.0F),
                        new Vector3f(1.0F, 13.5F, 9.0F),
                        new BlockElementFace(
                           null,
                           -1,
                           "",
                           new BlockFaceUV(facing == Direction.UP ? new float[]{7.0F, 6.0F, 9.0F, 8.0F} : new float[]{7.0F, 6.0F, 9.0F, 14.0F}, 0)
                        ),
                        this.getParticleIcon(),
                        facing,
                        rot,
                        new BlockElementRotation(vector, Axis.Z, -22.5F, false),
                        false,
                        LOCATION
                     )
               );
         }
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      return side == null && state != null ? this.quads : empty();
   }

   @Override
   public boolean useAmbientOcclusion() {
      return false;
   }

   @Override
   public boolean isGui3d() {
      return false;
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
