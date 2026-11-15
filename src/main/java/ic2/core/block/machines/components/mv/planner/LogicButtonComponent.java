package ic2.core.block.machines.components.mv.planner;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.logic.planner.newLogic.ReactorLogic;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class LogicButtonComponent extends GuiWidget {
   public static final Predicate<String> NUMBERS_ONLY = T -> {
      try {
         if (T.isEmpty()) {
            return true;
         } else {
            int number = Integer.parseInt(T);
            return number >= 1;
         }
      } catch (Exception var2) {
         return false;
      }
   };
   ReactorPlannerTileEntity tile;
   @OnlyIn(Dist.CLIENT)
   EditBox speed;
   boolean lastActive = false;
   boolean updating = false;

   public LogicButtonComponent(ReactorPlannerTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         100, new ExtendedButton(x + 170, y + 20, 47, 10, this.translate("gui.ic2.reactor_planner.button.start"), T -> this.tile.sendToServer(7, 0))
      );
      gui.addRenderableWidget(
         99, new ExtendedButton(x + 170, y + 31, 47, 10, this.translate("gui.ic2.reactor_planner.button.pulse"), T -> this.tile.sendToServer(18, 0))
      );
      gui.addRenderableWidget(101, new ExtendedButton(x + 170, y + 42, 47, 10, this.getPauseName(), T -> this.tile.sendToServer(8, 0)));
      gui.addRenderableWidget(
         102, new ExtendedButton(x + 170, y + 53, 47, 10, this.translate("gui.ic2.reactor_planner.button.stop"), T -> this.tile.sendToServer(7, 1))
      );
      gui.addRenderableWidget(
         103, new ExtendedButton(x + 170, y + 67, 47, 10, this.translate("gui.ic2.reactor_planner.button.compile"), T -> this.tile.sendToServer(9, 0))
      );
      gui.addRenderableWidget(
         104, new ExtendedButton(x + 170, y + 78, 47, 10, this.translate("gui.ic2.reactor_planner.button.reset"), T -> this.tile.sendToServer(9, 1))
      );
      gui.addRenderableWidget(
         105, new ExtendedButton(x + 170, y + 92, 47, 10, this.translate("gui.ic2.reactor_planner.button.backup"), T -> this.tile.sendToServer(10, 0))
      );
      gui.addRenderableWidget(
         106, new ExtendedButton(x + 170, y + 103, 47, 10, this.translate("gui.ic2.reactor_planner.button.restore"), T -> this.tile.sendToServer(10, 1))
      );
      gui.addRenderableWidget(
         107,
         new ExtendedButton(
            x + 170, y + 118, 47, 10, this.translate("gui.ic2.reactor_planner.button.clear"), T -> this.tile.sendToServer(14, Screen.hasShiftDown() ? 1 : 0)
         )
      );
      gui.addRenderableWidget(
         110, new ExtendedButton(x + 220, y + 130, 10, 10, this.string("+"), T -> this.tile.sendToServer(13, Screen.hasShiftDown() ? 10 : 1))
      );
      gui.addRenderableWidget(110, new ExtendedButton(x + 264, y + 130, 10, 10, this.string("-"), T -> {
         if (!this.updating) {
            this.tile.sendToServer(13, Screen.hasShiftDown() ? -10 : -1);
         }
      }));
      this.updating = true;
      this.speed = new EditBox(gui.getFont(), gui.getGuiLeft() + 220, gui.getGuiTop() + 153, 54, 10, this.string());
      this.speed.setValue(Integer.toString(this.tile.settings.ticksPerTick));
      this.speed.setResponder(T -> {
         if (!T.isEmpty() && !this.updating) {
            this.tile.sendToServer(15, Math.min(100000, Integer.parseInt(T)));
         }
      });
      this.speed.setCanLoseFocus(true);
      this.speed.setFilter(NUMBERS_ONLY);
      gui.addRenderableWidget(111, this.speed);
      this.updating = false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ReactorLogic logic = this.tile.simulation;
      gui.getButton(100).active = logic.isValid && !this.tile.isActive();
      gui.getButton(99).active = logic.isValid && !this.tile.isActive();
      AbstractWidget button = gui.getButton(101);
      button.active = logic.isValid;
      if (this.lastActive != logic.isProducing) {
         this.lastActive = logic.isProducing;
         button.setMessage(this.getPauseName());
      }

      gui.getButton(102).active = this.tile.isActive();
      gui.getButton(103).active = !logic.isValid;
      gui.getButton(104).active = logic.isValid || logic.tick > 0;
      gui.getButton(105).active = logic.isValid;
      gui.getButton(106).active = this.tile.backup.hasBackup && !this.tile.isActive();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      if (this.speed.isMouseOver((double)(mouseX + this.gui.getGuiLeft()), (double)(mouseY + this.gui.getGuiTop()))) {
         this.tile.sendToServer(16, scroll * (Screen.hasShiftDown() ? 10 : 1));
         return true;
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void onChanged() {
      this.updating = true;
      this.speed.setValue(Integer.toString(this.tile.settings.ticksPerTick));
      this.updating = false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawCenterString(matrix, this.string(Integer.toString(this.tile.stackSize)), 247, 131, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.reactor_planner.sim_speed"), 220, 143, 4210752);
   }

   public Component getPauseName() {
      return this.translate("gui.ic2.reactor_planner.button." + (this.tile.simulation.isProducing ? "pause" : "continue"));
   }
}
