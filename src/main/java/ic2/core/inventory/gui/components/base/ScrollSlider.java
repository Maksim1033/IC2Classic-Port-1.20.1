package ic2.core.inventory.gui.components.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.feature.ITooltipProvider;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.widget.ForgeSlider;

public class ScrollSlider extends ForgeSlider implements ITooltipProvider {
   private double scroll_offset = 0.0;
   boolean dragging = false;
   Component tooltip;
   Consumer<ScrollSlider> listener;

   public ScrollSlider(
      int xPos,
      int yPos,
      int width,
      int height,
      Component prefix,
      Component suf,
      double minVal,
      double maxVal,
      double currentVal,
      boolean drawString,
      Consumer<ScrollSlider> listener
   ) {
      super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, drawString);
      this.listener = listener;
   }

   public ScrollSlider(
      int xPos,
      int yPos,
      int width,
      int height,
      Component prefix,
      Component suf,
      double minVal,
      double maxVal,
      double currentVal,
      Consumer<ScrollSlider> listener
   ) {
      super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, true);
      this.listener = listener;
   }

   public ScrollSlider setScrollEffect(double scrollEffect) {
      this.scroll_offset = scrollEffect / (this.maxValue - this.minValue);
      return this;
   }

   public ScrollSlider setToolTip(Component tooltip) {
      this.tooltip = tooltip;
      return this;
   }

   public ScrollSlider setToolTip(String s, Object... args) {
      return this.setToolTip(Component.translatable(s, args));
   }

   public ScrollSlider setToolTip(String s) {
      return this.setToolTip(Component.translatable(s));
   }

   @Override
   public void addToolTip(IC2Screen gui, int x, int y, Consumer<Component> tooltips) {
      if (this.isHoveredOrFocused() && this.tooltip != null) {
         tooltips.accept(this.tooltip);
      }
   }

   public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
      return this.dragging || super.isMouseOver(p_isMouseOver_1_, p_isMouseOver_3_);
   }

   public void onClick(double mouseX, double mouseY) {
      super.onClick(mouseX, mouseY);
      this.dragging = true;
   }

   public void onRelease(double mouseX, double mouseY) {
      super.onRelease(mouseX, mouseY);
      this.dragging = false;
   }

   public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
      if (this.scroll_offset != 0.0 && this.active && this.visible) {
         this.value = this.value
            + this.scroll_offset * p_mouseScrolled_5_ * (double)(Screen.hasShiftDown() ? 10 : 1) * (double)(Screen.hasControlDown() ? 100 : 1);
         this.applyValue();
      }

      return this.scroll_offset != 0.0;
   }

   public void setValue(double d) {
      super.setValue(d);
      this.updateSlider(false);
   }

   @Override
   public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
      super.renderWidget(guiGraphics, mouseX, mouseY, partial);
      if (this.dragging) {
         this.setValueFromMouse((double)mouseX);
      }
   }

   private void setValueFromMouse(double mouseX) {
      this.setSliderValue((mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8));
   }

   private void setSliderValue(double value) {
      double oldValue = this.value;
      this.value = this.snapToNearest(value);
      if (!Mth.equal(oldValue, this.value)) {
         this.applyValue();
      }

      this.updateMessage();
   }

   public void applyValue() {
      this.updateSlider(true);
   }

   public void updateSlider(boolean sendListener) {
      this.value = Mth.clamp(this.value, 0.0, 1.0);
      this.updateMessage();
      if (this.listener != null && sendListener) {
         this.listener.accept(this);
      }
   }

   private double snapToNearest(double value) {
      if (this.stepSize <= 0.0) {
         return Mth.clamp(value, 0.0, 1.0);
      } else {
         value = Mth.lerp(Mth.clamp(value, 0.0, 1.0), this.minValue, this.maxValue);
         value = this.stepSize * (double)Math.round(value / this.stepSize);
         if (this.minValue > this.maxValue) {
            value = Mth.clamp(value, this.maxValue, this.minValue);
         } else {
            value = Mth.clamp(value, this.minValue, this.maxValue);
         }

         return Mth.map(value, this.minValue, this.maxValue, 0.0, 1.0);
      }
   }
}
