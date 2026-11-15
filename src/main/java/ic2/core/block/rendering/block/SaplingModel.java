package ic2.core.block.rendering.block;

import org.joml.Vector3f;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class SaplingModel extends BaseModel {
   public static final BlockElementFace NORTH = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0));
   public static final BlockElementFace SOUTH = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0));
   public static final BlockElementFace WEST = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0));
   public static final BlockElementFace EAST = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0));
   List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();

   public SaplingModel(TextureAtlasSprite sprite) {
      this.setParticleTexture(sprite);
   }

   @Override
   public void init() {
      this.quads
         .add(
            QuadBaker.BAKER
               .bakeQuad(
                  new Vector3f(0.8F, 0.0F, 8.0F),
                  new Vector3f(15.2F, 16.0F, 8.0F),
                  NORTH,
                  this.getParticleIcon(),
                  Direction.NORTH,
                  BlockModelRotation.X0_Y0,
                  new BlockElementRotation(new Vector3f(0.5F, 0.5F, 0.5F), Axis.Y, 45.0F, true),
                  true,
                  LOCATION
               )
         );
      this.quads
         .add(
            QuadBaker.BAKER
               .bakeQuad(
                  new Vector3f(0.8F, 0.0F, 8.0F),
                  new Vector3f(15.2F, 16.0F, 8.0F),
                  SOUTH,
                  this.getParticleIcon(),
                  Direction.SOUTH,
                  BlockModelRotation.X0_Y0,
                  new BlockElementRotation(new Vector3f(0.5F, 0.5F, 0.5F), Axis.Y, 45.0F, true),
                  true,
                  LOCATION
               )
         );
      this.quads
         .add(
            QuadBaker.BAKER
               .bakeQuad(
                  new Vector3f(8.0F, 0.0F, 0.8F),
                  new Vector3f(8.0F, 16.0F, 15.2F),
                  WEST,
                  this.getParticleIcon(),
                  Direction.WEST,
                  BlockModelRotation.X0_Y0,
                  new BlockElementRotation(new Vector3f(0.5F, 0.5F, 0.5F), Axis.Y, 45.0F, true),
                  true,
                  LOCATION
               )
         );
      this.quads
         .add(
            QuadBaker.BAKER
               .bakeQuad(
                  new Vector3f(8.0F, 0.0F, 0.8F),
                  new Vector3f(8.0F, 16.0F, 15.2F),
                  EAST,
                  this.getParticleIcon(),
                  Direction.EAST,
                  BlockModelRotation.X0_Y0,
                  new BlockElementRotation(new Vector3f(0.5F, 0.5F, 0.5F), Axis.Y, 45.0F, true),
                  true,
                  LOCATION
               )
         );
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      return side == null ? this.quads : empty();
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
