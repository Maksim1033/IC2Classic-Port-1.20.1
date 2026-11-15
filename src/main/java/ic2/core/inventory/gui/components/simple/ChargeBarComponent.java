package ic2.core.inventory.gui.components.simple;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tiles.readers.IEUStorage;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChargeBarComponent extends GuiWidget {
   IEUStorage provider;
   Vec2i offset;
   boolean vertical;
   int stepSize = 0;
   int initialOffset = 0;

   public ChargeBarComponent(Box2i box, IEUStorage storage, Vec2i sourcePosition, boolean vertical) {
      super(box);
      this.provider = storage;
      this.offset = sourcePosition;
      this.vertical = vertical;
   }

   public ChargeBarComponent setStepSize(int value, int initialOffset) {
      this.stepSize = value;
      this.initialOffset = initialOffset;
      return this;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int eu = this.provider.getStoredEU();
      if (eu > 0) {
         Box2i box = this.getBox();
         int width = box.getWidth();
         int height = box.getHeight();
         int max = this.vertical ? height : width;
         float lvl = (float)max * Math.min(1.0F, (float)eu / (float)this.provider.getMaxEU());
         if (lvl <= 0.0F) {
            return;
         }

         if (this.stepSize > 0) {
            boolean isMax = lvl >= (float)max;
            lvl = isMax ? (float)max : (float)((int)lvl / this.stepSize * this.stepSize);
            if (lvl <= 0.0F) {
               return;
            }

            if (!isMax) {
               lvl += (float)this.initialOffset;
            }
         }

         if (this.vertical) {
            this.gui
               .drawTextureRegion(
                  matrix,
                  (float)(this.gui.getGuiLeft() + box.getX()),
                  (float)(this.gui.getGuiTop() + box.getY()) + ((float)height - lvl),
                  (float)this.offset.getX(),
                  (float)this.offset.getY() + ((float)height - lvl),
                  (float)width,
                  lvl
               );
            return;
         }

         this.gui
            .drawTextureRegion(
               matrix,
               (float)(this.gui.getGuiLeft() + box.getX()),
               (float)(this.gui.getGuiTop() + box.getY()),
               (float)this.offset.getX(),
               (float)this.offset.getY(),
               lvl,
               (float)height
            );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (this.isMouseOver(mouseX, mouseY) && this.hasEUReader()) {
         tooltips.accept(
            this.translate(
               "gui.ic2.charge",
               new Object[]{Formatters.EU_FORMAT.format((long)this.provider.getStoredEU()), Formatters.EU_FORMAT.format((long)this.provider.getMaxEU())}
            )
         );
      }
   }
}
