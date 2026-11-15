package ic2.core.block.rendering.camouflage.shape;

import com.google.common.base.Objects;
import ic2.api.events.RetextureEvent;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.block.rendering.camouflage.IQuadCache;
import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.QuadBaker;
import ic2.core.platform.rendering.misc.UVHelper;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

public abstract class CamouflageShape {
   public static final CamouflageShape FULL_CUBE = new FullCubeShape();
   public static final CamouflageShape SLAB = new SlabShape();
   public static final CamouflageShape STAIRS = new StairShape();
   public static final CamouflageShape WALL = new WallShape();

   protected abstract boolean isSideFull(BlockState var1, Direction var2);

   @OnlyIn(Dist.CLIENT)
   protected abstract void generateBakedQuads(BlockState var1, Direction var2, RetextureEvent.Rotation[] var3, List<BakedQuad> var4, List<BakedQuad>[] var5);

   @OnlyIn(Dist.CLIENT)
   public CamouflageShape.QuadResults generateQuads(CamouflageStorage storage, BlockState state, Level world, BlockPos pos, RenderType type) {
      BlockModelShaper shapes = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
      List<BakedQuad>[] fullQuads = CollectionUtils.createLists(7);
      List<BakedQuad>[] translucentQuads = CollectionUtils.createLists(7);

      for (Direction side : DirectionList.ALL) {
         if (this.isSideFull(state, side)) {
            BlockEntity tile = world.getBlockEntity(pos.relative(side));
            if (tile instanceof ICamouflagable && ((ICamouflagable)tile).isSideEnabled(side.getOpposite()) && storage.isColored(side)) {
               continue;
            }
         }

         IQuadCache cache = storage.getCache(side);
         BlockState theState = cache.getDisplayBlock();
         if (theState != null) {
            getTypesForRender(
               theState,
               pos,
               shapes,
               fullQuads,
               translucentQuads,
               T -> {
                  List<BakedQuad> quads = shapes.getBlockModel(theState)
                     .getQuads(theState, cache.getSide(), RandomSource.create(theState.getSeed(pos)), ModelData.EMPTY, type);
                  if (!quads.isEmpty()) {
                     this.generateBakedQuads(state, side, cache.getRotations(), quads, T);
                  }
               }
            );
         }
      }

      List<BakedQuad>[] quadList = CollectionUtils.createLists(2);

      for (Direction side : DirectionList.ALL) {
         BlockState theState = storage.getCache(side).getDisplayBlock();
         if (Block.shouldRenderFace(theState == null ? state : theState, world, pos.relative(side), side, pos) || pos == BlockPos.ZERO) {
            quadList[0].addAll(fullQuads[side.get3DDataValue()]);
            quadList[1].addAll(translucentQuads[side.get3DDataValue()]);
         }
      }

      quadList[0].addAll(fullQuads[6]);
      quadList[1].addAll(translucentQuads[6]);
      return new CamouflageShape.QuadResults(quadList[0], quadList[1]);
   }

   @OnlyIn(Dist.CLIENT)
   protected static BakedQuad createQuad(BakedQuad original, ShapeBuilder.Quad quad, int index, Direction dir, RetextureEvent.Rotation rotation) {
      return QuadBaker.createQuad(
         quad.getBox(),
         dir,
         new BlockElementFace(
            null,
            original.isTinted() ? dir.get3DDataValue() * 50 + index : -1,
            "",
            new BlockFaceUV(UVHelper.rotateUVs(quad.getFace().uv.uvs, rotation), rotation.getRotation())
         ),
         original.getSprite(),
         BlockModelRotation.X0_Y0,
         null,
         true
      );
   }

   @OnlyIn(Dist.CLIENT)
   protected static void getTypesForRender(
      BlockState state, BlockPos pos, BlockModelShaper shapes, List<BakedQuad>[] quads, List<BakedQuad>[] translucentQuads, Consumer<List<BakedQuad>[]> result
   ) {
      for (RenderType type : IC2Models.getRenderTypes(state, RandomSource.create(state.getSeed(pos)))) {
         result.accept(isTranslucent(type) ? translucentQuads : quads);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static boolean isTranslucent(RenderType type) {
      return Objects.equal(type, RenderType.translucent()) || Objects.equal(type, RenderType.tripwire());
   }

   @OnlyIn(Dist.CLIENT)
   public static class QuadResults {
      List<BakedQuad> normalQuads;
      List<BakedQuad> translucentQuads;

      public QuadResults(List<BakedQuad> normalQuads, List<BakedQuad> translucentQuads) {
         this.normalQuads = normalQuads;
         this.translucentQuads = translucentQuads;
      }

      public List<BakedQuad> getQuads(RenderType type) {
         return CamouflageShape.isTranslucent(type) ? this.translucentQuads : this.normalQuads;
      }
   }
}
