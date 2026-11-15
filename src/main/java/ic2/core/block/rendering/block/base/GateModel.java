package ic2.core.block.rendering.block.base;

import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class GateModel extends BaseModel {
   private static final ResourceLocation GATE = new ResourceLocation("ic2:fence_gate");
   private static final String JSON_DATA = "{\"display\":{\"gui\":{\"rotation\":[ 30, 45, 0 ],\"translation\":[ 0, -1, 0],\"scale\":[ 0.8, 0.8, 0.8 ]},\"head\": {\"rotation\": [ 0, 0, 0 ],\"translation\": [ 0, -3, -6],\"scale\":[ 1, 1, 1]}}}";
   private static final ShapeBuilder OPEN_BUILDER = new ShapeBuilder()
      .newQuad(14.0, 5.0, 7.0, 16.0, 16.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 7.0F, 2.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 0.0F, 2.0F, 11.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 0.0F, 9.0F, 11.0F)
      .finish()
      .newQuad(0.0, 5.0, 7.0, 2.0, 16.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 7.0F, 16.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 14.0F, 0.0F, 16.0F, 11.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 0.0F, 9.0F, 11.0F)
      .finish()
      .newQuad(14.0, 6.0, 13.0, 16.0, 15.0, 15.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 13.0F, 16.0F, 15.0F)
      .addFaces(DirectionList.Z_AXIS, 14.0F, 1.0F, 16.0F, 10.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 1.0F, 15.0F, 10.0F)
      .finish()
      .newQuad(0.0, 6.0, 13.0, 2.0, 15.0, 15.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 13.0F, 2.0F, 15.0F)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 1.0F, 2.0F, 10.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 1.0F, 15.0F, 10.0F)
      .finish()
      .newQuad(14.0, 2.0, 7.0, 16.0, 13.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 7.0F, 2.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 0.0F, 2.0F, 11.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 0.0F, 9.0F, 11.0F)
      .finish()
      .newQuad(0.0, 2.0, 7.0, 2.0, 13.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 7.0F, 16.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 14.0F, 0.0F, 16.0F, 11.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 0.0F, 9.0F, 11.0F)
      .finish()
      .newQuad(14.0, 3.0, 13.0, 16.0, 12.0, 15.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 13.0F, 16.0F, 15.0F)
      .addFaces(DirectionList.Z_AXIS, 14.0F, 1.0F, 16.0F, 10.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 1.0F, 15.0F, 10.0F)
      .finish()
      .newQuad(0.0, 3.0, 13.0, 2.0, 12.0, 15.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 13.0F, 2.0F, 15.0F)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 1.0F, 2.0F, 10.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 1.0F, 15.0F, 10.0F)
      .finish();
   private static final ShapeBuilder CLOSE_BUILDER = new ShapeBuilder()
      .newQuad(14.0, 5.0, 7.0, 16.0, 16.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 7.0F, 16.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 14.0F, 0.0F, 16.0F, 11.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 0.0F, 9.0F, 11.0F)
      .finish()
      .newQuad(0.0, 5.0, 7.0, 2.0, 16.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 7.0F, 2.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 0.0F, 2.0F, 11.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 0.0F, 9.0F, 11.0F)
      .finish()
      .newQuad(8.0, 6.0, 7.0, 10.0, 15.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 8.0F, 7.0F, 10.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 8.0F, 1.0F, 10.0F, 10.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 1.0F, 9.0F, 10.0F)
      .finish()
      .newQuad(6.0, 6.0, 7.0, 8.0, 15.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 6.0F, 7.0F, 8.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 6.0F, 1.0F, 8.0F, 10.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 1.0F, 9.0F, 10.0F)
      .finish()
      .newQuad(14.0, 2.0, 7.0, 16.0, 13.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 7.0F, 16.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 14.0F, 0.0F, 16.0F, 11.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 0.0F, 9.0F, 11.0F)
      .finish()
      .newQuad(0.0, 2.0, 7.0, 2.0, 13.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 7.0F, 2.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 0.0F, 2.0F, 11.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 0.0F, 9.0F, 11.0F)
      .finish()
      .newQuad(8.0, 3.0, 7.0, 10.0, 12.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 8.0F, 7.0F, 10.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 8.0F, 1.0F, 10.0F, 10.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 1.0F, 9.0F, 10.0F)
      .finish()
      .newQuad(6.0, 3.0, 7.0, 8.0, 12.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 6.0F, 7.0F, 8.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 6.0F, 1.0F, 8.0F, 10.0F)
      .addFaces(DirectionList.X_AXIS, 7.0F, 1.0F, 9.0F, 10.0F)
      .finish();
   private static final ShapeBuilder OPEN_BAR_BUILDER = new ShapeBuilder()
      .newQuad(14.0, 12.0, 9.0, 16.0, 15.0, 13.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 9.0F, 16.0F, 13.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 1.0F, 15.0F, 4.0F)
      .finish()
      .newQuad(14.0, 6.0, 9.0, 16.0, 9.0, 13.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 9.0F, 16.0F, 13.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 7.0F, 15.0F, 10.0F)
      .finish()
      .newQuad(0.0, 12.0, 9.0, 2.0, 15.0, 13.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 9.0F, 2.0F, 13.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 1.0F, 15.0F, 4.0F)
      .finish()
      .newQuad(0.0, 6.0, 9.0, 2.0, 9.0, 13.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 9.0F, 2.0F, 13.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 7.0F, 15.0F, 10.0F)
      .finish()
      .newQuad(14.0, 9.0, 9.0, 16.0, 12.0, 13.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 9.0F, 16.0F, 13.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 1.0F, 15.0F, 4.0F)
      .finish()
      .newQuad(14.0, 3.0, 9.0, 16.0, 6.0, 13.0)
      .addFaces(DirectionList.VERTICAL, 14.0F, 9.0F, 16.0F, 13.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 7.0F, 15.0F, 10.0F)
      .finish()
      .newQuad(0.0, 9.0, 9.0, 2.0, 12.0, 13.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 9.0F, 2.0F, 13.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 1.0F, 15.0F, 4.0F)
      .finish()
      .newQuad(0.0, 3.0, 9.0, 2.0, 6.0, 13.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 9.0F, 2.0F, 13.0F)
      .addFaces(DirectionList.X_AXIS, 13.0F, 7.0F, 15.0F, 10.0F)
      .finish();
   public static final ShapeBuilder CLOSE_BAR_BUILDER = new ShapeBuilder()
      .newQuad(10.0, 12.0, 7.0, 14.0, 15.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 10.0F, 7.0F, 14.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 10.0F, 1.0F, 14.0F, 4.0F)
      .finish()
      .newQuad(10.0, 6.0, 7.0, 14.0, 9.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 10.0F, 7.0F, 14.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 10.0F, 7.0F, 14.0F, 10.0F)
      .finish()
      .newQuad(2.0, 12.0, 7.0, 6.0, 15.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 2.0F, 7.0F, 6.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 2.0F, 1.0F, 6.0F, 4.0F)
      .finish()
      .newQuad(2.0, 6.0, 7.0, 6.0, 9.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 2.0F, 7.0F, 6.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 2.0F, 7.0F, 6.0F, 10.0F)
      .finish()
      .newQuad(10.0, 9.0, 7.0, 14.0, 12.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 10.0F, 7.0F, 14.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 10.0F, 1.0F, 14.0F, 4.0F)
      .finish()
      .newQuad(10.0, 3.0, 7.0, 14.0, 6.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 10.0F, 7.0F, 14.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 10.0F, 7.0F, 14.0F, 10.0F)
      .finish()
      .newQuad(2.0, 9.0, 7.0, 6.0, 12.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 2.0F, 7.0F, 6.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 2.0F, 1.0F, 6.0F, 4.0F)
      .finish()
      .newQuad(2.0, 3.0, 7.0, 6.0, 6.0, 9.0)
      .addFaces(DirectionList.VERTICAL, 2.0F, 7.0F, 6.0F, 9.0F)
      .addFaces(DirectionList.Z_AXIS, 2.0F, 7.0F, 6.0F, 10.0F)
      .finish();
   private BlockState state;
   private List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();
   private String textureFolder;
   private String textureName;

   public GateModel(BlockState state, String textureFolder, String textureName) {
      this.state = state;
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   @Override
   public void init() {
      this.setTransforms(
         IC2Models.getTransformMap(
            IC2Models.BLOCK_LOCATION,
            GATE,
            "{\"display\":{\"gui\":{\"rotation\":[ 30, 45, 0 ],\"translation\":[ 0, -1, 0],\"scale\":[ 0.8, 0.8, 0.8 ]},\"head\": {\"rotation\": [ 0, 0, 0 ],\"translation\": [ 0, -3, -6],\"scale\":[ 1, 1, 1]}}}"
         )
      );
      this.setParticleTexture(IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.textureName));
      boolean open = (Boolean)this.state.getValue(FenceGateBlock.OPEN);
      boolean inWall = (Boolean)this.state.getValue(FenceGateBlock.IN_WALL);
      BlockModelRotation rotation = BlockModelRotation.by(0, ((Direction)this.state.getValue(FenceGateBlock.FACING)).get2DDataValue() * 90);
      int base = inWall ? 24 : 0;
      int sub = inWall ? 16 : 0;
      if (open) {
         OPEN_BUILDER.buildQuads(this.getParticleIcon(), rotation, null, true, base, base + 24, this.quads);
         OPEN_BAR_BUILDER.buildQuads(this.getParticleIcon(), rotation, null, true, sub, sub + 16, this.quads);
      } else {
         CLOSE_BUILDER.buildQuads(this.getParticleIcon(), rotation, null, true, base, base + 24, this.quads);
         CLOSE_BAR_BUILDER.buildQuads(this.getParticleIcon(), rotation, null, true, sub, sub + 16, this.quads);
      }
   }

   @Nonnull
   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType type) {
      return side != null ? empty() : this.quads;
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
