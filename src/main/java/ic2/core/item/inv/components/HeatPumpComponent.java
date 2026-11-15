package ic2.core.item.inv.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.item.inv.inventory.HeatPumpInventory;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class HeatPumpComponent extends GuiWidget {
   HeatPumpInventory inventory;

   public HeatPumpComponent(HeatPumpInventory inventory) {
      super(Box2i.EMPTY_BOX);
      this.inventory = inventory;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, new ExtendedButton(x + 40, y + 16, 30, 14, this.translate("tooltip.ic2.heat_pump.pull"), T -> this.toggleSide(0, false))).active = (
            this.inventory.directions & 1
         )
         == 0;
      gui.addRenderableWidget(1, new ExtendedButton(x + 75, y + 16, 30, 14, this.translate("tooltip.ic2.heat_pump.push"), T -> this.toggleSide(0, true))).active = (
            this.inventory.directions & 1
         )
         != 0;
      gui.addRenderableWidget(2, new ExtendedButton(x + 40, y + 31, 30, 14, this.translate("tooltip.ic2.heat_pump.pull"), T -> this.toggleSide(1, false))).active = (
            this.inventory.directions & 2
         )
         == 0;
      gui.addRenderableWidget(3, new ExtendedButton(x + 75, y + 31, 30, 14, this.translate("tooltip.ic2.heat_pump.push"), T -> this.toggleSide(1, true))).active = (
            this.inventory.directions & 2
         )
         != 0;
      gui.addRenderableWidget(4, new ExtendedButton(x + 40, y + 46, 30, 14, this.translate("tooltip.ic2.heat_pump.pull"), T -> this.toggleSide(2, false))).active = (
            this.inventory.directions & 4
         )
         == 0;
      gui.addRenderableWidget(5, new ExtendedButton(x + 75, y + 46, 30, 14, this.translate("tooltip.ic2.heat_pump.push"), T -> this.toggleSide(2, true))).active = (
            this.inventory.directions & 4
         )
         != 0;
      gui.addRenderableWidget(6, new ExtendedButton(x + 40, y + 61, 30, 14, this.translate("tooltip.ic2.heat_pump.pull"), T -> this.toggleSide(3, false))).active = (
            this.inventory.directions & 8
         )
         == 0;
      gui.addRenderableWidget(7, new ExtendedButton(x + 75, y + 61, 30, 14, this.translate("tooltip.ic2.heat_pump.push"), T -> this.toggleSide(3, true))).active = (
            this.inventory.directions & 8
         )
         != 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = (this.inventory.directions & 1) == 0;
      gui.getButton(1).active = (this.inventory.directions & 1) != 0;
      gui.getButton(2).active = (this.inventory.directions & 2) == 0;
      gui.getButton(3).active = (this.inventory.directions & 2) != 0;
      gui.getButton(4).active = (this.inventory.directions & 4) == 0;
      gui.getButton(5).active = (this.inventory.directions & 4) != 0;
      gui.getButton(6).active = (this.inventory.directions & 8) == 0;
      gui.getButton(7).active = (this.inventory.directions & 8) != 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawRightString(matrix, this.translate("misc.ic2.side.up").append(": "), 40, 19, 4210752);
      this.gui.drawRightString(matrix, this.translate("misc.ic2.side.down").append(": "), 40, 33, 4210752);
      this.gui.drawRightString(matrix, this.translate("misc.ic2.side.left").append(": "), 40, 50, 4210752);
      this.gui.drawRightString(matrix, this.translate("misc.ic2.side.right").append(": "), 40, 65, 4210752);
   }

   private void toggleSide(int index, boolean value) {
      this.inventory.onDataReceived(index, value ? 1 : 0);
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), index, value ? 1 : 0);
   }
}
