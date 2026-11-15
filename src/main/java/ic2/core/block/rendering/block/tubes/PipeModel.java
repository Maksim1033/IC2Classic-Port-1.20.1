package ic2.core.block.rendering.block.tubes;

import ic2.api.util.DirectionList;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CableProperty;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class PipeModel extends CableModel {
   List<BakedQuad>[] extractionQuads = CollectionUtils.createLists(64);
   List<BakedQuad> itemQuads = CollectionUtils.<BakedQuad>createList();
   boolean generatingItems = false;

   public PipeModel(ITubeBlock tube, BlockState state) {
      super(tube, state);
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
      this.generateExtractionQuads();
   }

   private void generateExtractionQuads() {
      float radius = this.tube.getRadius(this.state);
      float min = 8.0F - (radius + 2.0F);
      float max = 8.0F + radius + 2.0F;
      float textureMin = 8.0F - radius;
      float textureMax = 8.0F + radius;
      Byte2ObjectMap<List<BakedQuad>> sideQuads = new Byte2ObjectOpenHashMap();

      for (Direction side : DirectionList.ALL) {
         sideQuads.put((byte)side.get3DDataValue(), this.generateSideQuads(side, min, max, textureMin, textureMax));
      }

      for (int i = 0; i < 64; i++) {
         for (Direction side : DirectionList.ofNumber(i)) {
            this.extractionQuads[i].addAll((Collection<? extends BakedQuad>)sideQuads.get((byte)side.get3DDataValue()));
         }
      }
   }

   @Override
   protected DirectionList selectEndSides(DirectionList list, Direction facing) {
      return this.generatingItems ? list : (this.advTube == null ? list.remove(facing) : this.advTube.selectEndSides(list, facing, this.state));
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side == null) {
         Integer value = (Integer)extraData.get(CableProperty.INSTANCE);
         Integer extraction = (Integer)extraData.get(CableProperty.PIPE_EXTRACTION);
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
            if (extraction != null) {
               list.addAll(this.extractionQuads[extraction & 63]);
            }

            return list;
         } else if (value == null) {
            List<BakedQuad> releaseQuads = camouflage.apply(type).getQuads(type);
            return !releaseQuads.isEmpty() ? releaseQuads : this.quads[0];
         } else {
            int index = value;
            List<BakedQuad> list = CollectionUtils.<BakedQuad>createList();
            list.addAll(this.quads[index >> 6 & 63]);
            list.addAll(this.anchor[index & 63]);
            if (extraction != null) {
               list.addAll(this.extractionQuads[extraction & 63]);
            }

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
