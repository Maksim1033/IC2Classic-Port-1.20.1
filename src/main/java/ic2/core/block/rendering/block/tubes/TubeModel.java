package ic2.core.block.rendering.block.tubes;

import ic2.api.util.DirectionList;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CableProperty;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class TubeModel extends CableModel {
   List<BakedQuad> itemQuads = CollectionUtils.<BakedQuad>createList();
   boolean generatingItems = false;
   boolean cfoam;

   public TubeModel(ITubeBlock tube, BlockState state, boolean cfoam) {
      super(tube, state);
      this.cfoam = cfoam;
   }

   @Override
   public void init() {
      super.init();
      float radius = this.tube.getRadius(this.state);
      float min = 8.0F - radius;
      float max = 8.0F + radius;
      this.generatingItems = true;
      Byte2ObjectMap<BakedQuad> coreQuads = this.generateCoreQuads(min, max);

      for (Direction dir : DirectionList.VERTICAL) {
         this.itemQuads.addAll(this.generateSideQuads(dir, min, max));
      }

      for (Direction dir : DirectionList.HORIZONTAL) {
         this.itemQuads.add((BakedQuad)coreQuads.get((byte)dir.get3DDataValue()));
      }

      this.generatingItems = false;
   }

   @Override
   public boolean usesBlockLight() {
      return this.cfoam;
   }

   @Override
   public boolean useAmbientOcclusion() {
      return this.cfoam;
   }

   @Override
   protected DirectionList selectEndSides(DirectionList list, Direction facing) {
      return this.generatingItems ? list : (this.advTube == null ? list.remove(facing) : this.advTube.selectEndSides(list, facing, this.state));
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side == null) {
         Integer value = (Integer)extraData.get(CableProperty.INSTANCE);
         Function<RenderType, CamouflageShape.QuadResults> camouflage = (Function<RenderType, CamouflageShape.QuadResults>)extraData.get(
            CamouflageProperty.INSTANCE
         );
         if (value == null && camouflage == null) {
            return this.itemQuads;
         } else if (value != null && camouflage == null) {
            int index = value;
            List<BakedQuad> list = CollectionUtils.<BakedQuad>createList();
            list.addAll(this.quads[index >> 6 & 63]);
            list.addAll(this.anchor[index & 63]);
            return list;
         } else if (value == null) {
            List<BakedQuad> releaseQuads = camouflage.apply(type).getQuads(type);
            return !releaseQuads.isEmpty() ? releaseQuads : this.quads[0];
         } else {
            int index = value;
            List<BakedQuad> list = CollectionUtils.<BakedQuad>createList();
            list.addAll(this.quads[index >> 6 & 63]);
            list.addAll(this.anchor[index & 63]);
            List<BakedQuad> releaseQuads = camouflage.apply(type).getQuads(type);
            if (!releaseQuads.isEmpty()) {
               list.addAll(releaseQuads);
            }

            return list;
         }
      } else {
         return empty();
      }
   }
}
