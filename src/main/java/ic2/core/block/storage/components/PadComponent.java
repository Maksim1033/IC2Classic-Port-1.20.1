package ic2.core.block.storage.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.base.tiles.impls.BaseChargePadTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;

public class PadComponent extends GuiWidget {
   BaseChargePadTileEntity tile;

   public PadComponent(BaseChargePadTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      int max = this.tile.getMaxEU();
      int eu = Math.min(max, this.tile.getStoredEU());
      this.gui.drawString(matrix, this.translate("misc.ic2.eu", new Object[]{Formatters.EU_FORMAT.format((long)eu)}), 48, 16, 4210752);
      this.gui
         .drawString(matrix, this.string("/").append(this.translate("misc.ic2.eu", new Object[]{Formatters.EU_FORMAT.format((long)max)})), 48, 25, 4210752);
      matrix.pushPose();
      matrix.translate(48.0, 38.0, 0.0);
      matrix.scale(0.5F, 0.5F, 1.0F);
      this.gui
         .drawString(matrix, this.translate("gui.ic2.charge_pad.out", new Object[]{Formatters.EU_FORMAT.format((long)this.tile.transferLimit)}), 0, -8, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.charge_pad.tier", new Object[]{this.tile.tier}), 0, 2, 4210752);
      matrix.popPose();
   }

   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      if (this.tile.data.getBaseTier() == 1) {
         this.gui.drawTextureRegion(matrix, (float)(x + 151), (float)(y + 28), 188.0F, 0.0F, 18.0F, 18.0F);
         this.gui.drawTextureRegion(matrix, (float)(x + 151), (float)(y + 57), 188.0F, 0.0F, 18.0F, 18.0F);
      }

      int upgrades = this.tile.inventory.getSlotCount() - (this.tile.data.getBaseTier() > 1 ? 3 : 1);
      if (upgrades < 3) {
         this.gui.drawTextureRegion(matrix, (float)(x + 107), (float)(y + 57), 188.0F, 0.0F, 18.0F, 18.0F);
         if (upgrades < 2) {
            this.gui.drawTextureRegion(matrix, (float)(x + 87), (float)(y + 57), 188.0F, 0.0F, 18.0F, 18.0F);
         }
      }
   }
}
