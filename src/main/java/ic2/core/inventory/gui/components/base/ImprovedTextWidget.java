package ic2.core.inventory.gui.components.base;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.feature.ITooltipProvider;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class ImprovedTextWidget extends EditBox implements ITooltipProvider {
   Consumer<String> actualResponder = null;
   boolean autoUpdating = false;
   Component tooltip;
   boolean external = false;

   public ImprovedTextWidget(int x, int y, int width, int height) {
      this(Minecraft.getInstance().font, x, y, width, height, Component.empty());
   }

   public ImprovedTextWidget(int x, int y, int width, int height, Component text) {
      super(Minecraft.getInstance().font, x, y, width, height, text);
   }

   public ImprovedTextWidget(Font font, int x, int y, int width, int height) {
      this(font, x, y, width, height, Component.empty());
   }

   public ImprovedTextWidget(Font font, int x, int y, int width, int height, Component text) {
      super(font, x, y, width, height, text);
      super.setResponder(this::onTextChanged);
   }

   public ImprovedTextWidget setToolTip(Component tooltip) {
      this.tooltip = tooltip;
      return this;
   }

   public ImprovedTextWidget setToolTip(String s, Object... args) {
      return this.setToolTip(Component.translatable(s, args));
   }

   public ImprovedTextWidget setToolTip(String s) {
      return this.setToolTip(Component.translatable(s));
   }

   public ImprovedTextWidget setAutoUpdating(boolean value) {
      this.autoUpdating = value;
      return this;
   }

   public void setExternalValue(String newValue) {
      this.external = true;
      this.setValue(newValue);
      this.external = false;
   }

   public void setResponder(Consumer<String> responderIn) {
      this.actualResponder = responderIn;
   }

   @Override
   public void setFocused(boolean focused) {
      super.setFocused(focused);
      if (!focused && !this.autoUpdating && this.actualResponder != null) {
         this.actualResponder.accept(this.getValue());
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 257) {
         if (this.actualResponder != null) {
            this.actualResponder.accept(this.getValue());
         }

         this.setFocused(false);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private void onTextChanged(String s) {
      if (!this.external) {
         if (this.actualResponder != null && !this.autoUpdating) {
            this.actualResponder.accept(s);
         }
      }
   }

   @Override
   public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
   }

   @Override
   public void addToolTip(IC2Screen gui, int x, int y, Consumer<Component> tooltip) {
      if (this.isMouseOver((double)x, (double)y) && this.tooltip != null) {
         tooltip.accept(this.tooltip);
      }
   }
}
