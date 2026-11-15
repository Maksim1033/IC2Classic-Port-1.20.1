package ic2.core.inventory.gui.components.base;

import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.feature.ITooltipProvider;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class ToolTipButton extends ExtendedButton implements ITooltipProvider {
   Component toolTip;

   public ToolTipButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
      super(xPos, yPos, width, height, displayString, handler);
   }

   public ToolTipButton setToolTip(Component toolTip) {
      this.toolTip = toolTip;
      return this;
   }

   public ToolTipButton setToolTip(String s, Object... args) {
      return this.setToolTip(Component.translatable(s, args));
   }

   public ToolTipButton setToolTip(String s) {
      return this.setToolTip(Component.translatable(s));
   }

   @Override
   public void addToolTip(IC2Screen gui, int x, int y, Consumer<Component> tooltip) {
      if (this.isHoveredOrFocused() && this.toolTip != null) {
         tooltip.accept(this.toolTip);
      }
   }
}
