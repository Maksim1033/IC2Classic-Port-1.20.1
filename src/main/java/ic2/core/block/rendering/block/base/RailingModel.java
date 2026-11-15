package ic2.core.block.rendering.block.base;

import ic2.core.block.misc.base.IC2PanelBlock;
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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class RailingModel extends BaseModel {
   private static final String JSON_DATA = "{\"display\":{\"gui\":{\"rotation\":[30,45,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"head\":{\"rotation\":[0,0,0],\"translation\":[0,0,0],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,-45,0],\"translation\":[0,0,0],\"scale\":[0.40,0.40,0.40]},\"thirdperson_lefthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}, \"thirdperson_righthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}}}";
   private static final ResourceLocation RAILING = new ResourceLocation("ic2:railing");
   static final ShapeBuilder STRAIGHT = new ShapeBuilder()
      .newQuad(0.0, 0.0, 14.0, 16.0, 16.0, 16.0)
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
   private BlockState state;
   private String key;
   private String value;
   private List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();

   public RailingModel(BlockState state, String key, String value) {
      this.state = state;
      this.key = key;
      this.value = value;
   }

   @Override
   public void init() {
      this.setTransforms(
         IC2Models.getTransformMap(
            IC2Models.BLOCK_LOCATION,
            RAILING,
            "{\"display\":{\"gui\":{\"rotation\":[30,45,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"head\":{\"rotation\":[0,0,0],\"translation\":[0,0,0],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,-45,0],\"translation\":[0,0,0],\"scale\":[0.40,0.40,0.40]},\"thirdperson_lefthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}, \"thirdperson_righthand\":{\"rotation\":[75,-45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}}}"
         )
      );
      this.setParticleTexture(IC2Textures.getMappedEntriesBlockIC2(this.key).get(this.value));
      StairsShape shape = (StairsShape)this.state.getValue(IC2PanelBlock.SHAPE);
      Direction facing = (Direction)this.state.getValue(IC2PanelBlock.FACING);
      if (shape == StairsShape.STRAIGHT) {
         STRAIGHT.buildQuads(this.getParticleIcon(), BlockModelRotation.by(0, facing.get2DDataValue() * 90 + 180), null, true, this.quads);
      } else if (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) {
         INNER.buildQuads(
            this.getParticleIcon(),
            BlockModelRotation.by(0, ((Direction)this.state.getValue(StairBlock.FACING)).get2DDataValue() * 90 + (shape == StairsShape.INNER_LEFT ? 0 : 90)),
            null,
            true,
            this.quads
         );
      } else if (shape == StairsShape.OUTER_LEFT || shape == StairsShape.OUTER_RIGHT) {
         OUTER.buildQuads(
            this.getParticleIcon(),
            BlockModelRotation.by(0, ((Direction)this.state.getValue(StairBlock.FACING)).get2DDataValue() * 90 + (shape == StairsShape.OUTER_LEFT ? 180 : 270)),
            null,
            true,
            this.quads
         );
      }
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
