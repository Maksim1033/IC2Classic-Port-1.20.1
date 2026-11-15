package ic2.core.block.machines.components.mv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.item.inv.inventory.CropBreederInventory;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class CropBreederComponent extends GuiWidget {
   CropBreederInventory breeder;

   public CropBreederComponent(CropBreederInventory breeder) {
      super(Box2i.EMPTY_BOX);
      this.breeder = breeder;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @Override
   public boolean isMouseOver(int x, int y) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, new ExtendedButton(x + 7, y + 108, 10, 10, this.string("<"), T -> this.moveBar(false, true)));
      gui.addRenderableWidget(1, new ExtendedButton(x + 158, y + 108, 10, 10, this.string(">"), T -> this.moveBar(false, false)));
      gui.addRenderableWidget(2, new ExtendedButton(x + 30, y + 108, 40, 10, this.translate("gui.ic2.crop_analyzer.cancel"), T -> this.otherButtons(true)));
      gui.addRenderableWidget(3, new ExtendedButton(x + 100, y + 108, 50, 10, this.translate("gui.ic2.crop_analyzer.breed"), T -> this.otherButtons(false)));
      gui.addRenderableWidget(4, new ExtendedButton(x + 7, y + 27, 10, 10, this.string("<"), T -> this.moveBar(true, true)));
      gui.addRenderableWidget(5, new ExtendedButton(x + 158, y + 27, 10, 10, this.string(">"), T -> this.moveBar(true, false)));
      gui.addRenderableWidget(
         new ExtendedButton(
            x + 105, y + 27, 30, 10, this.translate(this.breeder.waterlogged ? "gui.ic2.crop_analyzer.yes" : "gui.ic2.crop_analyzer.no"), this::toggleResult
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.crop_analyzer.waterlogged"), 38, 28, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   public void toggleResult(Button button) {
      this.breeder.waterlogged = !this.breeder.waterlogged;
      button.setMessage(this.translate(this.breeder.waterlogged ? "gui.ic2.crop_analyzer.yes" : "gui.ic2.crop_analyzer.no"));
      IC2.NETWORKING.get(false).sendClientItemEvent(this.breeder.getInventoryStack(), 4, 0);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(3).active = this.canBreed();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      Slot slot = this.gui.getSlotUnderMouse();
      if (slot instanceof LockedSlot) {
         this.moveBar(slot.index < 9, scroll > 0);
         return true;
      } else {
         return false;
      }
   }

   public boolean canBreed() {
      int count = 0;

      for (int i = 0; i < 4; i++) {
         count += this.breeder.getStackInSlot(i).isEmpty() ? 0 : 1;
      }

      return count > 1;
   }

   public void otherButtons(boolean breed) {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.breeder.getInventoryStack(), breed ? 2 : 3, 0);
   }

   public void moveBar(boolean top, boolean left) {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.breeder.getInventoryStack(), top ? 0 : 1, left ? -1 : 1);
   }
}
