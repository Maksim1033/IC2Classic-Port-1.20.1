package ic2.core.block.rendering.block.base;

import ic2.core.block.misc.base.IC2EdgeRailingBlock;
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

public class EdgeRailingModel extends BaseModel {
   static final ShapeBuilder OUTER = new ShapeBuilder()
      .newQuad(0.0, 0.0, 14.0, 16.0, 16.0, 16.0)
      .addCube(
         new float[]{1.0F, 1.0F, 15.0F, 3.0F},
         new float[]{1.0F, 1.0F, 15.0F, 3.0F},
         new float[]{1.0F, 1.0F, 15.0F, 15.0F},
         new float[]{1.0F, 1.0F, 15.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F}
      )
      .finish()
      .newQuad(14.0, 0.0, 0.0, 16.0, 16.0, 16.0)
      .addCube(
         new float[]{1.0F, 1.0F, 15.0F, 3.0F},
         new float[]{1.0F, 1.0F, 15.0F, 3.0F},
         new float[]{1.0F, 1.0F, 15.0F, 15.0F},
         new float[]{1.0F, 1.0F, 15.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F}
      )
      .finish();
   static final ShapeBuilder INNER = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 2.0, 16.0, 2.0)
      .addCube(
         new float[]{1.0F, 1.0F, 3.0F, 3.0F},
         new float[]{1.0F, 1.0F, 3.0F, 3.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F}
      )
      .finish();
   private BlockState state;
   TextureAtlasSprite main;
   TextureAtlasSprite secondary;
   private List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();

   public EdgeRailingModel(BlockState state, TextureAtlasSprite main, TextureAtlasSprite secondary) {
      this.state = state;
      this.setParticleTexture(main);
      this.main = main;
      this.secondary = secondary;
   }

   @Override
   public void init() {
      boolean right = (Boolean)this.state.getValue(IC2EdgeRailingBlock.RIGHT);
      Direction facing = (Direction)this.state.getValue(IC2EdgeRailingBlock.FACING);
      OUTER.buildQuads(this.main, BlockModelRotation.by(0, facing.get2DDataValue() * 90 + (right ? 270 : 180)), null, true, this.quads);
      INNER.buildQuads(this.secondary, BlockModelRotation.by(0, facing.getOpposite().get2DDataValue() * 90 + (right ? 90 : 0)), null, true, this.quads);
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
