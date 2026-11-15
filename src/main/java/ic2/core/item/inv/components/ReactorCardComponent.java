package ic2.core.item.inv.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.block.machines.components.mv.planner.NamingTabComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.item.inv.inventory.ReactorCardInventory;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReactorCardComponent extends GuiWidget {
   ReactorCardInventory inventory;

   public ReactorCardComponent(ReactorCardInventory inventory) {
      super(Box2i.EMPTY_BOX);
      this.inventory = inventory;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      ImprovedTextWidget number = new ImprovedTextWidget(gui.getGuiLeft() + 118, gui.getGuiTop() + 27, 50, 20);
      number.setCanLoseFocus(true);
      number.setMaxLength(5);
      number.setBordered(false);
      number.setFilter(NamingTabComponent.NUMBERS_ONLY);
      number.setValue(Integer.toString(this.inventory.maxHeat));
      number.setResponder(this::updateMaxHeat);
      gui.addRenderableWidget(-2, number);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.reactor_card.max_heat"), 116, 17, 4210752);
   }

   @Override
   public boolean onKeyTyped(int keyCode) {
      return ((ImprovedTextWidget)this.gui.getCastedButton(-2, ImprovedTextWidget.class)).isFocused() && keyCode == 69;
   }

   private void updateMaxHeat(String number) {
      try {
         IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), -1, Integer.parseInt(number));
      } catch (Exception var3) {
      }
   }
}
