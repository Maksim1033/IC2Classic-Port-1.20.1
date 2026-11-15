package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import ic2.core.block.machines.tiles.mv.ChunkloaderTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class ChunkloaderRenderer implements BlockEntityRenderer<ChunkloaderTileEntity> {
   ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
   ItemStack activePearl = new ItemStack(Items.ENDER_PEARL);

   public ChunkloaderRenderer(Context context) {
      this.activePearl.enchant(Enchantments.AQUA_AFFINITY, 1);
   }

   public void render(
      ChunkloaderTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      ItemRenderer render = Minecraft.getInstance().getItemRenderer();
      matrixStackIn.pushPose();
      if (tileEntityIn.animationProgress < 1.0F) {
         float yOffset = Mth.lerp(tileEntityIn.animationProgress, 1.0F, 1.5F);
         float hOffset = Mth.lerp(tileEntityIn.animationProgress, 0.03F, 0.0F);
         float rotation = Mth.lerp(tileEntityIn.animationProgress, 90.0F, 0.0F);
         matrixStackIn.translate(0.5 - (double)hOffset, (double)yOffset, 0.5 + (double)hOffset);
         matrixStackIn.scale(0.75F, 0.75F, 0.75F);
         matrixStackIn.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotation));
      } else {
         matrixStackIn.translate(0.5, 1.5, 0.5);
         matrixStackIn.scale(0.75F, 0.75F, 0.75F);
         matrixStackIn.mulPose(com.mojang.math.Axis.YP.rotationDegrees(tileEntityIn.animationProgress * 360.0F));
      }

      render.renderStatic(
         tileEntityIn.doesChunkProcessing && tileEntityIn.isActive() ? this.activePearl : this.pearl,
         ItemDisplayContext.FIXED,
         LevelRenderer.getLightColor(tileEntityIn.getLevel(), tileEntityIn.getBlockPos().above()),
         combinedOverlayIn,
         matrixStackIn,
         bufferIn,
         tileEntityIn.getLevel(),
         0
      );
      matrixStackIn.popPose();
   }
}
