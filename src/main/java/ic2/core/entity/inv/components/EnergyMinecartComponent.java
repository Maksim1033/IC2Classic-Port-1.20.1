package ic2.core.entity.inv.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.entity.minecarts.ElectricMinecart;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyMinecartComponent extends GuiWidget {
   ElectricMinecart minecart;

   public EnergyMinecartComponent(ElectricMinecart minecart) {
      super(Box2i.EMPTY_BOX);
      this.minecart = minecart;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int eu = this.minecart.getStoredEU();
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      int offset = this.minecart.getGuiOffset();
      this.gui.drawTextureRegion(matrix, (float)(x + 55 + offset), (float)(y + 16), 0.0F, 166.0F, 52.0F, 54.0F);
      if (eu > 0) {
         float per = Math.min(1.0F, (float)eu / (float)this.minecart.getMaxEU());
         float lvl = per * 24.0F;
         if (lvl > 0.0F) {
            this.gui.drawTextureRegion(matrix, (float)(x + 79 + offset), (float)(y + 34), 176.0F, 14.0F, lvl, 16.0F);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      int offset = this.minecart.getGuiOffset();
      this.gui.drawString(matrix, this.translate("gui.ic2.storage.power"), 85 + offset, 23, 4210752);
      int max = this.minecart.getMaxEU();
      int eu = Math.min(this.minecart.getStoredEU(), max);
      this.gui.drawString(matrix, this.translate("misc.ic2.eu", new Object[]{Formatters.EU_FORMAT.format((long)eu)}), 109 + offset, 35, 4210752);
      this.gui
         .drawString(
            matrix, this.string("/").append(this.translate("misc.ic2.eu", new Object[]{Formatters.EU_FORMAT.format((long)max)})), 109 + offset, 45, 4210752
         );
   }
}
