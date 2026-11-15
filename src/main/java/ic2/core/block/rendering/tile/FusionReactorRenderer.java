package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;
import ic2.api.util.DirectionList;
import ic2.core.block.machines.tiles.luv.FusionReactorTileEntity;
import ic2.core.block.rendering.models.RodModel;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.platform.rendering.misc.IC2RenderTypes;
import ic2.core.platform.rendering.misc.UVHelper;
import ic2.core.utils.helpers.FluidHelper;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;

public class FusionReactorRenderer implements BlockEntityRenderer<FusionReactorTileEntity> {
   private static final Quaternionf INVERSE = com.mojang.math.Axis.XP.rotationDegrees(180.0F);
   private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/models/tile/uranium_rod.png");
   RodModel model = new RodModel();
   BlockRenderDispatcher blockRenderer;

   public FusionReactorRenderer(Context context) {
      this.blockRenderer = context.getBlockRenderDispatcher();
   }

   public void render(
      FusionReactorTileEntity tileEntityIn, float partialTicks, PoseStack matrix, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      if (tileEntityIn.isValid && tileEntityIn.getBox() != null) {
         BlockPos pos = tileEntityIn.getBox().getCenter();
         BlockState state = tileEntityIn.renderInfo.getMeltingBlock();
         BlockPos offset = pos.subtract(tileEntityIn.getBlockPos());
         matrix.pushPose();
         if (tileEntityIn.storedFluid > 0) {
            VertexConsumer builder = bufferIn.getBuffer(IC2RenderTypes.FLUID_RENDER);
            int lightValue = LevelRenderer.getLightColor(tileEntityIn.getLevel(), pos);
            float filled = (float)Math.min(tileEntityIn.storedFluid, 10000) / 40000.0F;
            FluidStack stack = new FluidStack(Fluids.LAVA, tileEntityIn.storedFluid);
            UVHelper notFlowing = UVHelper.fromSprite(FluidHelper.getTexture(stack, false));
            RenderShapes.renderColorTextureLightCube(
               tileEntityIn.getBox(filled), notFlowing, FluidHelper.getFluidColor(stack), OverlayTexture.NO_OVERLAY, lightValue, builder, matrix
            );
         }

         matrix.translate((double)offset.getX(), (double)offset.getY(), (double)offset.getZ());
         if (!state.isAir()) {
            long seed = state.getSeed(pos);
            BakedModel blockModel = this.blockRenderer.getBlockModel(state);

            for (RenderType renderType : blockModel.getRenderTypes(state, RandomSource.create(seed), ModelData.EMPTY)) {
               VertexConsumer vertexConsumer = bufferIn.getBuffer(renderType);
               this.blockRenderer
                  .getModelRenderer()
                  .tesselateBlock(
                     tileEntityIn.getLevel(),
                     blockModel,
                     state,
                     pos,
                     matrix,
                     vertexConsumer,
                     false,
                     RandomSource.create(),
                     seed,
                     combinedOverlayIn,
                     ModelData.EMPTY,
                     renderType
                  );
            }
         }

         for (Direction dir : DirectionList.ALL) {
            if (tileEntityIn.renderInfo.isVisible(dir)) {
               matrix.pushPose();
               matrix.translate(0.5, 0.5, 0.5);
               Quaternionf quad = new Quaternionf(dir.getRotation());
               quad.mul(INVERSE);
               matrix.mulPose(quad);
               this.model
                  .render(
                     matrix,
                     bufferIn.getBuffer(RenderType.entityCutout(TEXTURE)),
                     15728880,
                     655360,
                     tileEntityIn.renderInfo.getColor(dir),
                     tileEntityIn.renderInfo.getCount(dir)
                  );
               matrix.popPose();
            }
         }

         matrix.popPose();
      }
   }
}
