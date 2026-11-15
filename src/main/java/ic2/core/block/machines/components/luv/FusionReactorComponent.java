package ic2.core.block.machines.components.luv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.tiles.luv.FusionReactorTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class FusionReactorComponent extends GuiWidget {
   FusionReactorTileEntity tile;

   public FusionReactorComponent(FusionReactorTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      float total = (float)this.tile.heat / 48000.0F * 50.0F;
      if (total > 0.0F) {
         this.gui
            .drawGradientRegionVertical(
               matrix,
               (float)(this.gui.getGuiLeft() + 140),
               (float)(this.gui.getGuiTop() + 19) + (50.0F - total),
               6.0F,
               total,
               ColorUtils.mixColors(ColorUtils.BLUE, ColorUtils.RED, Math.max(0.0F, (total - 25.0F) / 25.0F)),
               ColorUtils.BLUE
            );
      }

      total = this.tile.getMaterial() * 50.0F;
      if (total > 0.0F) {
         this.gui
            .drawTextureRegion(
               matrix, (float)(this.gui.getGuiLeft() + 30), (float)(this.gui.getGuiTop() + 18) + (50.0F - total), 176.0F, 156.0F - total, 6.0F, total
            );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      long ticks = this.tile.getTicksTillHeatUp();
      if (ticks > 0L) {
         this.gui.drawString(matrix, this.translate("gui.ic2.fusion.heatup"), 39, 62, 4210752);
         this.gui.drawRightString(matrix, this.string(DurationFormatUtils.formatDuration(Math.abs(ticks * 50L), "HH:mm:ss")), 79, 72, 4210752);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (mouseX >= 140 && mouseX <= 146 && mouseY >= 19 && mouseY <= 69) {
         tooltips.accept(this.translate("gui.ic2.fusion.heat", new Object[]{this.tile.heat}));
      }

      if (mouseX >= 30 && mouseX <= 36 && mouseY >= 18 && mouseY <= 68) {
         tooltips.accept(this.translate("gui.ic2.fusion.material", new Object[]{this.tile.material, 10000}));
      }
   }
}
