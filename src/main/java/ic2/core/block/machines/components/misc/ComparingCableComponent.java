package ic2.core.block.machines.components.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.cables.ComparingDetectorCableTileEntity;
import ic2.core.block.machines.components.mv.planner.NamingTabComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ComparingCableComponent extends GuiWidget {
   ComparingDetectorCableTileEntity tile;
   int lastMin;
   int lastMax;

   public ComparingCableComponent(ComparingDetectorCableTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
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
      this.lastMin = this.tile.minEnergy;
      this.lastMax = this.tile.maxEnergy;
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      ImprovedTextWidget widget = gui.addRenderableWidget(0, new ImprovedTextWidget(x + 85, y + 20, 63, 12));
      widget.setMaxLength(10);
      widget.setBordered(false);
      widget.setCanLoseFocus(true);
      widget.setFilter(NamingTabComponent.NUMBERS_ONLY);
      widget.setValue(Integer.toString(this.lastMin));
      widget.setResponder(T -> this.set(true, T));
      widget = gui.addRenderableWidget(1, new ImprovedTextWidget(x + 85, y + 38, 63, 12));
      widget.setMaxLength(10);
      widget.setBordered(false);
      widget.setCanLoseFocus(true);
      widget.setFilter(NamingTabComponent.NUMBERS_ONLY);
      widget.setValue(Integer.toString(this.lastMax));
      widget.setResponder(T -> this.set(false, T));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      if (this.tile.minEnergy != this.lastMin) {
         this.lastMin = this.tile.minEnergy;
         ((ImprovedTextWidget)gui.getCastedButton(0, ImprovedTextWidget.class)).setExternalValue(Integer.toString(this.lastMin));
      }

      if (this.tile.maxEnergy != this.lastMax) {
         this.lastMax = this.tile.maxEnergy;
         ((ImprovedTextWidget)gui.getCastedButton(1, ImprovedTextWidget.class)).setExternalValue(Integer.toString(this.lastMax));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.comparing_cable.min"), 25, 20, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.comparing_cable.max"), 25, 38, 4210752);
   }

   public void set(boolean min, String value) {
      try {
         this.set(min, Integer.parseInt(value));
      } catch (Exception var4) {
      }
   }

   public void set(boolean min, int value) {
      if (min) {
         this.tile.minEnergy = Math.min(value, this.tile.maxEnergy - 1);
         this.tile.sendToServer(0, this.tile.minEnergy);
      } else {
         this.tile.maxEnergy = Math.max(value, this.tile.minEnergy + 1);
         this.tile.sendToServer(1, this.tile.maxEnergy);
      }
   }
}
