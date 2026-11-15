package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;
import ic2.core.block.machines.tiles.hv.ElectricEnchanterTileEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.Mth;

public class EnchanterRenderer implements BlockEntityRenderer<ElectricEnchanterTileEntity> {
   private BookModel modelBook;

   public EnchanterRenderer(Context context) {
      this.modelBook = new BookModel(context.bakeLayer(ModelLayers.BOOK));
   }

   public void render(
      ElectricEnchanterTileEntity tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      ElectricEnchanterTileEntity.BookInformation te = tile.renderData;
      te.tick();
      matrixStackIn.pushPose();
      matrixStackIn.translate(0.5, 0.775, 0.5);
      float f = (float)te.tickCount + partialTicks;
      matrixStackIn.translate(0.0, (double)(0.1F + Mth.sin(f * 0.1F) * 0.01F), 0.0);
      float f1 = te.bookRotation - te.bookRotationPrev;
      if ((double)f1 > 3.141592653589793) {
         f1 -= 6.2831855F;
      }

      if ((double)f1 < -3.141592653589793) {
         f1 += 6.2831855F;
      }

      float f2 = te.bookRotationPrev + f1 * partialTicks;
      matrixStackIn.mulPose(com.mojang.math.Axis.YP.rotation(-f2));
      matrixStackIn.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(80.0F));
      float f3 = Mth.lerp(partialTicks, te.pageFlipPrev, te.pageFlip);
      float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
      float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
      float f6 = Mth.lerp(partialTicks, te.bookSpreadPrev, te.bookSpread);
      this.modelBook.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
      VertexConsumer iVertexBuilder = EnchantTableRenderer.BOOK_LOCATION.buffer(bufferIn, RenderType::entitySolid);
      this.modelBook
         .render(
            matrixStackIn, iVertexBuilder, LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().above()), combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F
         );
      matrixStackIn.popPose();
   }
}
