package ic2.core.block.rendering.block;

import ic2.api.util.DirectionList;
import ic2.core.block.cables.luminator.LuminatorBlock;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.CamouflageProperty;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

public class LuminatorModel extends BaseModel {
   public static final ShapeBuilder FULL = new ShapeBuilder()
      .newQuad(0.1599999964237213, 0.1599999964237213, 0.009999999776482582, 15.84000015258789, 15.84000015258789, 1.0)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.VERTICAL, 0.0F, 15.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.X_AXIS, 15.0F, 0.0F, 16.0F, 16.0F)
      .finish();
   public static final ShapeBuilder SMALL = new ShapeBuilder()
      .newQuad(4.0, 13.0, 4.0, 12.0, 15.989999771118164, 12.0)
      .addFaces(DirectionList.VERTICAL, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 15.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.X_AXIS, 15.0F, 0.0F, 16.0F, 16.0F)
      .finish()
      .newQuad(0.1599999964237213, 0.009999999776482582, 0.1599999964237213, 15.84000015258789, 1.0, 15.84000015258789)
      .addFaces(DirectionList.VERTICAL, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 15.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.X_AXIS, 15.0F, 0.0F, 16.0F, 16.0F)
      .finish()
      .newQuad(6.0, 3.0, 0.0, 10.0, 13.0, 2.0)
      .addFaces(DirectionList.Z_AXIS, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.VERTICAL, 0.0F, 15.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.X_AXIS, 15.0F, 0.0F, 16.0F, 16.0F)
      .finish();
   BlockState state;
   boolean isItem;
   List<BakedQuad> itemQuads = CollectionUtils.<BakedQuad>createList();
   List<BakedQuad> baseQuads = CollectionUtils.<BakedQuad>createList();

   public LuminatorModel(BlockState state, TextureAtlasSprite sprite) {
      this.state = state;
      this.isItem = state.getBlock().defaultBlockState() == state;
      this.setParticleTexture(sprite);
   }

   @Override
   public void init() {
      Direction side = (Direction)this.state.getValue(LuminatorBlock.FACING);
      Tuple<Integer, Integer> result = QuadBaker.getModelRotation(side);
      if ((Boolean)this.state.getValue(LuminatorBlock.BIG)) {
         FULL.buildQuads(this.getParticleIcon(), BlockModelRotation.by((Integer)result.getA(), (Integer)result.getB()), null, true, this.baseQuads);
      } else {
         int index = side.getAxis().isVertical() ? side.get3DDataValue() : 2;
         SMALL.buildQuads(
            this.getParticleIcon(),
            index != 2 ? BlockModelRotation.X0_Y0 : BlockModelRotation.by((Integer)result.getA(), (Integer)result.getB()),
            null,
            true,
            index * 6,
            index * 6 + 6,
            this.baseQuads
         );
      }

      if (this.isItem) {
         this.itemQuads.addAll(IC2Models.generateItemQuads(-1, this.getParticleIcon(), IC2Models.getItemTransforms().gui));
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side != null) {
         return empty();
      } else if (state == null) {
         return this.itemQuads;
      } else {
         Function<RenderType, CamouflageShape.QuadResults> glass = (Function<RenderType, CamouflageShape.QuadResults>)extraData.get(CamouflageProperty.INSTANCE);
         if (glass != null) {
            List<BakedQuad> quads = new ObjectArrayList();
            if (!CamouflageShape.isTranslucent(type)) {
               quads.addAll(this.baseQuads);
            }

            quads.addAll(glass.apply(type).getQuads(type));
            return quads;
         } else {
            return this.baseQuads;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static CamouflageShape.QuadResults createQuads(Level world, BlockPos pos, BlockState state) {
      DirectionList drawList = DirectionList.EMPTY;

      for (Direction dir : DirectionList.ALL) {
         if (Block.shouldRenderFace(state, world, pos.relative(dir), dir, pos)) {
            drawList = drawList.add(dir);
         }
      }

      return createQuads(state, RandomSource.create(state.getSeed(pos)), drawList);
   }

   public static CamouflageShape.QuadResults createQuads(BlockState state, RandomSource rand, DirectionList drawList) {
      List<BakedQuad>[] quads = CollectionUtils.createLists(2);
      BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
      ChunkRenderTypeSet set = IC2Models.getRenderTypes(state, rand);

      for (RenderType type : RenderType.chunkBufferLayers()) {
         if (set.contains(type)) {
            int index = CamouflageShape.isTranslucent(type) ? 1 : 0;
            boolean added = false;

            for (Direction dir : drawList) {
               List<BakedQuad> subQuads = model.getQuads(state, dir, rand, ModelData.EMPTY, type);
               if (!subQuads.isEmpty()) {
                  quads[index].addAll(subQuads);
                  added = true;
               }
            }

            if (!added) {
               List<BakedQuad> subQuads = model.getQuads(state, null, rand, ModelData.EMPTY, type);
               if (!subQuads.isEmpty()) {
                  quads[index].addAll(subQuads);
               }
            }
         }
      }

      return new CamouflageShape.QuadResults(quads[0], quads[1]);
   }

   @Override
   public boolean useAmbientOcclusion() {
      return false;
   }
}
