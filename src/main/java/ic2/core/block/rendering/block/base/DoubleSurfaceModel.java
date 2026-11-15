package ic2.core.block.rendering.block.base;

import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class DoubleSurfaceModel extends BaseModel {
   public static final ShapeBuilder TOP = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 16.0, 2.0, 16.0)
      .addFaces(DirectionList.VERTICAL, 1.0F, 1.0F, 14.0F, 14.0F)
      .addFaces(DirectionList.N_CORNER, 1.0F, 1.0F, 14.0F, 3.0F)
      .addFaces(DirectionList.P_CORNER, 1.0F, 3.0F, 14.0F, 14.0F)
      .finish();
   public static final ShapeBuilder BOTTOM = new ShapeBuilder()
      .newQuad(0.0, 14.0, 0.0, 16.0, 16.0, 16.0)
      .addFaces(DirectionList.VERTICAL, 1.0F, 1.0F, 14.0F, 14.0F)
      .addFaces(DirectionList.N_CORNER, 1.0F, 1.0F, 14.0F, 3.0F)
      .addFaces(DirectionList.P_CORNER, 1.0F, 3.0F, 14.0F, 14.0F)
      .finish();
   TextureAtlasSprite top;
   TextureAtlasSprite bottom;
   List<BakedQuad> results = CollectionUtils.<BakedQuad>createList();

   public DoubleSurfaceModel(TextureAtlasSprite top, TextureAtlasSprite bottom) {
      this.setParticleTexture(top);
      this.top = top;
      this.bottom = bottom;
   }

   @Override
   public void init() {
      TOP.buildQuads(this.top, BlockModelRotation.X0_Y0, null, true, this.results);
      BOTTOM.buildQuads(this.bottom, BlockModelRotation.X0_Y0, null, true, this.results);
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      return side == null ? this.results : empty();
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
