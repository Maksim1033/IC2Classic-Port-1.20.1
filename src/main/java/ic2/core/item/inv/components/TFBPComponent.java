package ic2.core.item.inv.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.item.inv.inventory.TFBPInventory;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TFBPComponent extends GuiWidget {
   TFBPInventory inventory;

   public TFBPComponent(TFBPInventory inventory) {
      super(Box2i.EMPTY_BOX);
      this.inventory = inventory;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      ((ToolTipButton)gui.addRenderableWidget(0, new ToolTipButton(x + 49, y + 35, 12, 12, this.string("-"), T -> this.changeRadius(-1))))
         .setToolTip(this.translate("tooltip.ic2.press_key_description", new Object[]{this.translate("tooltip.ic2.shift.name"), "10x"}));
      ((ToolTipButton)gui.addRenderableWidget(1, new ToolTipButton(x + 115, y + 35, 12, 12, this.string("+"), T -> this.changeRadius(1))))
         .setToolTip(this.translate("tooltip.ic2.press_key_description", new Object[]{this.translate("tooltip.ic2.shift.name"), "10x"}));
   }

   private void changeRadius(int offset) {
      offset *= Screen.hasShiftDown() ? 10 : 1;
      int newRadius = Mth.clamp(this.inventory.radius + offset, 0, 100);
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 0, newRadius);
      this.inventory.radius = newRadius;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.tfbp.radius"), 88, 22, 4210752);
      this.gui.drawCenterString(matrix, this.string(Integer.toString(this.inventory.radius)), 88, 37, 4210752);
   }
}
