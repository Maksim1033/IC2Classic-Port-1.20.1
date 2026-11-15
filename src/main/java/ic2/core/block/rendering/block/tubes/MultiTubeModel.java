package ic2.core.block.rendering.block.tubes;

import ic2.api.util.DirectionList;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CableProperty;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class MultiTubeModel extends TubeModel {
   protected List<BakedQuad>[] filledSides = CollectionUtils.createLists(6);

   public MultiTubeModel(ITubeBlock tube, BlockState state, boolean cfoam) {
      super(tube, state, cfoam);
   }

   @Override
   public void init() {
      super.init();
      float radius = this.tube.getRadius(this.state);
      float min = 8.0F - radius;
      float max = 8.0F + radius;
      BlockState original = this.state;

      for (Direction dir : DirectionList.ALL) {
         this.state = (BlockState)this.state.setValue(IC2Properties.ALL_FACINGS, dir);
         this.filledSides[dir.get3DDataValue()] = this.generateSideQuads(dir, min, max);
      }

      this.state = original;
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side == null) {
         Integer value = (Integer)extraData.get(CableProperty.INSTANCE);
         Integer extraValue = (Integer)extraData.get(CableProperty.TUBE_EXTRA_SIDE);
         Function<RenderType, CamouflageShape.QuadResults> camouflage = (Function<RenderType, CamouflageShape.QuadResults>)extraData.get(
            CamouflageProperty.INSTANCE
         );
         if (value == null && camouflage == null) {
            return this.itemQuads;
         } else if (value != null && camouflage == null) {
            int index = value;
            List<BakedQuad> list = CollectionUtils.<BakedQuad>createList();
            if (extraValue != null) {
               index &= ~(extraValue << 6);

               for (Direction dir : DirectionList.ofNumber(extraValue)) {
                  list.addAll(this.filledSides[dir.ordinal()]);
               }
            }

            list.addAll(this.quads[index >> 6 & 63]);
            list.addAll(this.anchor[index & 63]);
            return list;
         } else if (value == null) {
            List<BakedQuad> releaseQuads = camouflage.apply(type).getQuads(type);
            return !releaseQuads.isEmpty() ? releaseQuads : this.quads[0];
         } else {
            int index = value;
            List<BakedQuad> list = CollectionUtils.<BakedQuad>createList();
            if (extraValue != null) {
               index &= ~(extraValue << 6);

               for (Direction dir : DirectionList.ofNumber(extraValue)) {
                  list.addAll(this.filledSides[dir.ordinal()]);
               }
            }

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
