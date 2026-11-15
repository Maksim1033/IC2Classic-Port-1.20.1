package ic2.core.block.rendering.block;

import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class CamouflageModel extends BaseModel {
   static final CamouflageModel INSTANCE = new CamouflageModel();
   static final ShapeBuilder BUILDER = new ShapeBuilder().newAutoQuad(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();

   private CamouflageModel() {
   }

   public static CamouflageModel getInstance() {
      return INSTANCE;
   }

   @Override
   public void init() {
      this.quads.clear();
      this.setParticleTexture(IC2Textures.getMappedEntriesBlockIC2("cfoam").get("wet_containing"));
      BUILDER.buildQuads(this.getParticleIcon(), BlockModelRotation.X0_Y0, null, true, this.quads);
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side != null) {
         return empty();
      } else {
         Function<RenderType, CamouflageShape.QuadResults> quads = (Function<RenderType, CamouflageShape.QuadResults>)extraData.get(CamouflageProperty.INSTANCE);
         if (quads != null) {
            List<BakedQuad> releaseQuads = quads.apply(type).getQuads(type);
            return !releaseQuads.isEmpty() ? releaseQuads : empty();
         } else {
            return this.quads;
         }
      }
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
