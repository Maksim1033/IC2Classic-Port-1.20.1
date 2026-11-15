package ic2.core.block.generators.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.generators.tiles.WindTurbineTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WindTurbineComponent extends GuiWidget {
   WindTurbineTileEntity tile;

   public WindTurbineComponent(WindTurbineTileEntity tile) {
      super(new Box2i(80, 45, 14, 14));
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      float l = Math.min(14.0F, this.tile.subproduction.getProduction(1600.0F) * 14.0F);
      if (l > 0.0F) {
         this.gui.drawTextureRegion(matrix, (float)(x + 80), (float)(y + 45 + 14) - l, 176.0F, 14.0F - l, 14.0F, l);
      }

      l = Math.min(14.0F, this.tile.subproduction.getProduction(1600.0F, 1200.0F) * 14.0F);
      if (l > 0.0F) {
         this.gui.drawTextureRegion(matrix, (float)(x + 80), (float)(y + 45 + 14) - l, 176.0F, 28.0F - l, 14.0F, l);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (this.isMouseOver(mouseX, mouseY)) {
         tooltips.accept(this.translate("gui.ic2.production", new Object[]{Formatters.EU_FORMAT.format((double)this.tile.subproduction.getProduction(100.0F))}));
      }
   }
}
