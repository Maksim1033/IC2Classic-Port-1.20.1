package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.api.tiles.display.IMonitorRenderer;
import ic2.core.block.cables.mointor.MonitorBlock;
import ic2.core.block.cables.mointor.MonitorTileEntity;
import ic2.core.platform.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class MonitorRenderer implements BlockEntityRenderer<MonitorTileEntity> {
   public MonitorRenderer(Context context) {
   }

   public void render(MonitorTileEntity tile, float partialTicks, PoseStack matrix, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
      BlockState state = tile.getBlockState();
      if ((Boolean)state.getValue(MonitorBlock.MASTER) && (Boolean)state.getValue(MonitorBlock.ACTIVE) && !tile.dataManager.isEmpty()) {
         float scale = tile.scale;
         float multiplier = 256.0F / scale;
         float offset = 28.0F / scale;
         matrix.pushPose();
         Direction dir = tile.getFacing();
         RenderUtils.prepareText(matrix, dir.getAxis() == Axis.Z ? dir : dir.getOpposite(), scale, 16.0, 16.0, -0.001, 0.0, 0.0, 0.0);
         MonitorRenderer.Helper helper = new MonitorRenderer.Helper(MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()), Minecraft.getInstance());
         int width = (int)((float)tile.width * multiplier - offset);
         int height = (int)((float)tile.height * multiplier - offset);
         int y = 0;
         int x = tile.align.getXOffset(width);
         int i = 0;

         for (int m = tile.dataManager.size(); i < m && height > 0; i++) {
            IDisplayInfo info = tile.dataManager.get(i);
            info.render(matrix, x, y, width, height, tile.align, helper);
            int size = info.getHeight(width, tile.align);
            helper.endBatch();
            height -= size;
            y += size;
         }

         matrix.popPose();
      }
   }

   public boolean shouldRenderOffScreen(MonitorTileEntity p_188185_1_) {
      return true;
   }

   public static class Helper implements IMonitorRenderer {
      BufferSource batcher;
      Minecraft mc;

      public Helper(BufferSource batcher, Minecraft mc) {
         this.batcher = batcher;
         this.mc = mc;
      }

      public void endBatch() {
         this.batcher.endBatch();
      }

      @Override
      public BufferSource getBatcher() {
         return this.batcher;
      }

      @Override
      public Font getFont() {
         return this.mc.font;
      }

      @Override
      public ItemRenderer getItemRenderer() {
         return this.mc.getItemRenderer();
      }

      @Override
      public void renderGuiItems(PoseStack matrix, ItemStack stack, float x, float y) {
         RenderUtils.renderGuiItem(this.mc.getItemRenderer(), matrix, stack, x, y);
      }

      @Override
      public void renderGuiItemText(PoseStack matrixStack, Font font, ItemStack stack, float x, float y, String text) {
         RenderUtils.renderGuiItemDecorations(matrixStack, font, stack, x, y, text);
      }
   }
}
