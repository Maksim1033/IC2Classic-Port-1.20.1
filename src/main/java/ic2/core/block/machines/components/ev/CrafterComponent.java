package ic2.core.block.machines.components.ev;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.containers.ev.CrafterContainer;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.slot.MemorySlot;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrafterComponent extends GuiWidget {
   public CrafterComponent() {
      super(new Box2i(176, 0, 61, 64));
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      Box2i box = this.getBox();
      this.gui
         .drawTextureRegion(
            matrix,
            (float)(this.gui.getGuiLeft() + box.getX()),
            (float)(this.gui.getGuiTop() + box.getY()),
            (float)box.getX(),
            (float)box.getY(),
            (float)box.getWidth(),
            (float)box.getHeight()
         );

      for (Slot slot : this.gui.getCastedContainer(CrafterContainer.class).slots) {
         if (slot instanceof MemorySlot && !slot.isActive()) {
            this.gui.makeSlotFunction(matrix, slot);
         }
      }
   }
}
