package ic2.core.block.rendering.block;

import ic2.core.block.cables.luminator.ConstructionLightBlock;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class ConstructionLightModel extends BaseModel {
   public static final ShapeBuilder BUILDER = new ShapeBuilder()
      .newQuad(7.0, 0.0, 9.0, 9.0, 1.0, 16.0)
      .addCube(
         new float[]{4.75F, 0.0F, 4.25F, 1.75F},
         new float[]{4.25F, 1.75F, 3.75F, 0.0F},
         new float[]{3.75F, 1.75F, 4.25F, 2.0F},
         new float[]{6.0F, 1.75F, 6.5F, 2.0F},
         new float[]{4.25F, 1.75F, 6.0F, 2.0F},
         new float[]{2.0F, 1.75F, 3.75F, 2.0F}
      )
      .finish()
      .newQuad(7.0, 0.0, 0.0, 9.0, 1.0, 7.0)
      .addCube(
         new float[]{4.75F, 2.0F, 4.25F, 3.75F},
         new float[]{4.25F, 3.75F, 3.75F, 2.0F},
         new float[]{3.75F, 3.75F, 4.25F, 4.0F},
         new float[]{6.0F, 3.75F, 6.5F, 4.0F},
         new float[]{4.25F, 3.75F, 6.0F, 4.0F},
         new float[]{2.0F, 3.75F, 3.75F, 4.0F}
      )
      .finish()
      .newQuad(9.0, 0.0, 7.0, 16.0, 1.0, 9.0)
      .addCube(
         new float[]{8.75F, 0.0F, 7.0F, 0.5F},
         new float[]{7.0F, 0.5F, 5.25F, 0.0F},
         new float[]{5.25F, 0.5F, 7.0F, 0.75F},
         new float[]{7.5F, 0.5F, 9.25F, 0.75F},
         new float[]{7.0F, 0.5F, 7.5F, 0.75F},
         new float[]{4.75F, 0.5F, 5.25F, 0.75F}
      )
      .finish()
      .newQuad(0.0, 0.0, 7.0, 7.0, 1.0, 9.0)
      .addCube(
         new float[]{8.75F, 0.75F, 7.0F, 1.25F},
         new float[]{7.0F, 1.25F, 5.25F, 0.75F},
         new float[]{5.25F, 1.25F, 7.0F, 1.5F},
         new float[]{7.5F, 1.25F, 9.25F, 1.5F},
         new float[]{7.0F, 1.25F, 7.5F, 1.5F},
         new float[]{4.75F, 1.25F, 5.25F, 1.5F}
      )
      .finish()
      .newQuad(7.0, 0.0, 7.0, 9.0, 27.0, 9.0)
      .addCube(
         new float[]{1.5F, 0.0F, 1.0F, 0.5F},
         new float[]{1.0F, 0.5F, 0.5F, 0.0F},
         new float[]{0.5F, 0.5F, 1.0F, 7.25F},
         new float[]{1.5F, 0.5F, 2.0F, 7.25F},
         new float[]{1.0F, 0.5F, 1.5F, 7.25F},
         new float[]{0.0F, 0.5F, 0.5F, 7.25F}
      )
      .finish()
      .newQuad(7.0, 25.0, 5.0, 9.0, 27.0, 7.0)
      .addCube(
         new float[]{3.5F, 5.5F, 3.0F, 6.0F},
         new float[]{3.0F, 6.0F, 2.5F, 5.5F},
         new float[]{2.5F, 6.0F, 3.0F, 6.5F},
         new float[]{3.5F, 6.0F, 4.0F, 6.5F},
         new float[]{3.0F, 6.0F, 3.5F, 6.5F},
         new float[]{2.0F, 6.0F, 2.5F, 6.5F}
      )
      .finish()
      .newQuad(6.0, 24.0, 4.0, 10.0, 28.0, 5.0)
      .addCube(
         new float[]{7.0F, 2.0F, 6.0F, 2.25F},
         new float[]{6.0F, 2.25F, 5.0F, 2.0F},
         new float[]{5.0F, 2.25F, 6.0F, 3.25F},
         new float[]{6.25F, 2.25F, 7.25F, 3.25F},
         new float[]{6.0F, 2.25F, 6.25F, 3.25F},
         new float[]{4.75F, 2.25F, 5.0F, 3.25F}
      )
      .finish()
      .newQuad(5.0, 24.0, 2.0, 11.0, 28.0, 4.0)
      .addCube(
         new float[]{5.5F, 4.0F, 4.0F, 4.5F},
         new float[]{4.0F, 4.5F, 2.5F, 4.0F},
         new float[]{2.5F, 4.5F, 4.0F, 5.5F},
         new float[]{4.5F, 4.5F, 6.0F, 5.5F},
         new float[]{4.0F, 4.5F, 4.5F, 5.5F},
         new float[]{2.0F, 4.5F, 2.5F, 5.5F}
      )
      .finish()
      .newQuad(10.0, 25.0, 4.0, 11.0, 27.0, 5.0)
      .addCube(
         new float[]{2.75F, 0.0F, 2.5F, 0.25F},
         new float[]{2.5F, 0.25F, 2.25F, 0.0F},
         new float[]{2.25F, 0.25F, 2.5F, 0.75F},
         new float[]{2.75F, 0.25F, 3.0F, 0.75F},
         new float[]{2.5F, 0.25F, 2.75F, 0.75F},
         new float[]{2.0F, 0.25F, 2.25F, 0.75F}
      )
      .finish()
      .newQuad(5.0, 25.0, 4.0, 6.0, 27.0, 5.0)
      .addCube(
         new float[]{2.75F, 2.0F, 2.5F, 2.25F},
         new float[]{2.5F, 2.25F, 2.25F, 2.0F},
         new float[]{2.25F, 2.25F, 2.5F, 2.75F},
         new float[]{2.75F, 2.25F, 3.0F, 2.75F},
         new float[]{2.5F, 2.25F, 2.75F, 2.75F},
         new float[]{2.0F, 2.25F, 2.25F, 2.75F}
      )
      .finish();
   BlockState state;
   List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();

   public ConstructionLightModel(BlockState state) {
      this.state = state;
   }

   @Override
   public void init() {
      this.setParticleTexture(IC2Textures.getMappedEntriesBlockIC2("electric/luminator").get("construction_light"));
      if (!(Boolean)this.state.getValue(ConstructionLightBlock.TOP)) {
         BUILDER.buildQuads(
            this.getParticleIcon(),
            BlockModelRotation.by(0, (int)((Direction)this.state.getValue(ConstructionLightBlock.FACING)).toYRot()),
            null,
            false,
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
