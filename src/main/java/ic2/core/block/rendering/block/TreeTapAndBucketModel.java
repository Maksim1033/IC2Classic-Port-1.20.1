package ic2.core.block.rendering.block;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.misc.TreeTapAndBucketBlock;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class TreeTapAndBucketModel extends BaseModel {
   private static final ShapeBuilder MODEL = new ShapeBuilder()
      .newQuad(4.0, 0.0, 12.0, 12.0, 6.0, 12.0)
      .addFace(Direction.SOUTH, 0.0F, 0.0F, 4.0F, 3.0F)
      .finish()
      .newQuad(12.0, 0.0, 4.0, 12.0, 6.0, 12.0)
      .addFace(Direction.EAST, 4.0F, 0.0F, 8.0F, 3.0F)
      .finish()
      .newQuad(4.0, 0.0, 4.0, 12.0, 6.0, 4.0)
      .addFace(Direction.NORTH, 8.0F, 0.0F, 12.0F, 3.0F)
      .finish()
      .newQuad(4.0, 0.0, 4.0, 4.0, 6.0, 12.0)
      .addFace(Direction.WEST, 12.0F, 0.0F, 16.0F, 3.0F)
      .finish()
      .newQuad(5.0, 1.0, 11.0, 11.0, 6.0, 11.0)
      .addFace(Direction.NORTH, 6.0F, 3.0F, 9.0F, 5.5F)
      .finish()
      .newQuad(5.0, 1.0, 5.0, 11.0, 6.0, 5.0)
      .addFace(Direction.SOUTH, 0.0F, 3.0F, 3.0F, 5.5F)
      .finish()
      .newQuad(5.0, 1.0, 5.0, 5.0, 6.0, 11.0)
      .addFace(Direction.EAST, 3.0F, 3.0F, 6.0F, 5.5F)
      .finish()
      .newQuad(11.0, 1.0, 5.0, 11.0, 6.0, 11.0)
      .addFace(Direction.WEST, 9.0F, 3.0F, 12.0F, 5.5F)
      .finish()
      .newQuad(4.0, 0.0, 4.0, 12.0, 0.0, 12.0)
      .addCulledFace(Direction.DOWN, 0.0F, 5.5F, 4.0F, 9.5F)
      .finish()
      .newQuad(4.0, 6.0, 4.0, 5.0, 6.0, 12.0)
      .addFace(Direction.UP, 4.0F, 5.5F, 4.5F, 9.5F)
      .finish()
      .newQuad(11.0, 6.0, 4.0, 12.0, 6.0, 12.0)
      .addFace(Direction.UP, 7.5F, 5.5F, 8.0F, 9.5F)
      .finish()
      .newQuad(5.0, 6.0, 4.0, 11.0, 6.0, 5.0)
      .addFace(Direction.UP, 4.5F, 5.5F, 7.5F, 6.0F)
      .finish()
      .newQuad(5.0, 6.0, 11.0, 11.0, 6.0, 12.0)
      .addFace(Direction.UP, 4.5F, 9.0F, 7.5F, 9.5F)
      .finish()
      .newQuad(5.0, 1.0, 5.0, 11.0, 1.0, 11.0)
      .addFace(Direction.UP, 4.5F, 6.0F, 7.5F, 9.0F)
      .finish()
      .newQuad(7.0, 2.0, 0.0, 9.0, 4.0, 4.0)
      .addCube(
         new float[]{0.0F, 9.5F, 1.0F, 11.5F},
         new float[]{3.0F, 9.5F, 4.0F, 11.5F},
         new float[]{0.0F, 0.0F, 1.0F, 1.0F},
         new float[]{0.0F, 0.0F, 1.0F, 1.0F},
         new float[]{1.0F, 10.5F, 3.0F, 11.5F},
         new float[]{1.0F, 9.5F, 3.0F, 10.5F}
      )
      .finish()
      .newQuad(7.199999809265137, 7.0, 7.0, 8.800000190734863, 11.0, 9.0)
      .addCube(
         new float[]{13.0F, 15.0F, 14.0F, 16.0F},
         new float[]{13.0F, 12.0F, 14.0F, 13.0F},
         new float[]{14.0F, 13.0F, 15.0F, 15.0F},
         new float[]{12.0F, 13.0F, 13.0F, 15.0F},
         new float[]{15.0F, 13.0F, 16.0F, 15.0F},
         new float[]{13.0F, 13.0F, 14.0F, 15.0F}
      )
      .finish()
      .newQuad(6.0, 12.0, 6.0, 10.0, 13.0, 7.0)
      .addCube(
         new float[]{9.5F, 15.5F, 11.5F, 16.0F},
         new float[]{9.5F, 14.5F, 11.5F, 15.0F},
         new float[]{9.5F, 14.0F, 11.5F, 14.5F},
         new float[]{9.5F, 15.0F, 11.5F, 15.5F},
         new float[]{9.0F, 15.0F, 9.5F, 15.5F},
         new float[]{11.5F, 15.0F, 12.0F, 15.5F}
      )
      .finish()
      .newQuad(7.0, 10.0, 0.0, 9.0, 12.0, 8.0)
      .addCube(
         new float[]{10.0F, 7.0F, 11.0F, 11.0F},
         new float[]{11.0F, 7.0F, 12.0F, 11.0F},
         new float[]{0.0F, 0.0F, 1.0F, 1.0F},
         new float[]{11.0F, 11.0F, 12.0F, 12.0F},
         new float[]{12.0F, 11.0F, 16.0F, 12.0F},
         new float[]{12.0F, 10.0F, 16.0F, 11.0F}
      )
      .finish();
   private static final ShapeBuilder RESIN_NEW = new ShapeBuilder()
      .newQuad(5.0, 2.0, 5.0, 11.0, 2.0, 11.0)
      .addFace(Direction.UP, 0.0F, 13.0F, 3.0F, 16.0F)
      .finish()
      .newQuad(5.0, 3.0, 5.0, 11.0, 3.0, 11.0)
      .addFace(Direction.UP, 0.0F, 13.0F, 3.0F, 16.0F)
      .finish()
      .newQuad(5.0, 4.0, 5.0, 11.0, 4.0, 11.0)
      .addFace(Direction.UP, 0.0F, 13.0F, 3.0F, 16.0F)
      .finish()
      .newQuad(5.0, 5.0, 5.0, 11.0, 5.0, 11.0)
      .addFace(Direction.UP, 0.0F, 13.0F, 3.0F, 16.0F)
      .finish()
      .newQuad(5.0, 6.0, 5.0, 11.0, 6.0, 11.0)
      .addFace(Direction.UP, 0.0F, 13.0F, 3.0F, 16.0F)
      .finish();
   BlockState state;
   List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();

   public TreeTapAndBucketModel(BlockState state, TextureAtlasSprite texture) {
      this.state = state;
      this.setParticleTexture(texture);
   }

   @Override
   public void init() {
      BlockModelRotation rotation = BlockModelRotation.by(0, (int)((Direction)this.state.getValue(TreeTapAndBucketBlock.FACING)).getOpposite().toYRot());
      MODEL.buildQuads(this.getParticleIcon(), rotation, null, true, this.quads);
      int level = (Integer)this.state.getValue(TreeTapAndBucketBlock.FILL_STAGE);
      if (level > 0) {
         RESIN_NEW.buildQuads(this.getParticleIcon(), rotation, null, true, level - 1, level, this.quads);
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      return side == null ? this.quads : empty();
   }

   @Override
   public BakedModel applyTransform(ItemDisplayContext ItemDisplayContext, PoseStack poseStack, boolean applyLeftHandTransform) {
      BakedModel model = this.handlePerspective(this, ItemDisplayContext, poseStack, applyLeftHandTransform);
      poseStack.translate(0.0, 0.25, 0.0);
      poseStack.scale(1.5F, 1.5F, 1.5F);
      return model;
   }

   @Override
   public boolean useAmbientOcclusion() {
      return false;
   }
}
