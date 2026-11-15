package ic2.core.inventory.gui.components.base;

import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

public class OpenerButton extends ToolTipButton {
   public OpenerButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
      super(xPos, yPos, width, height, displayString, handler);
   }

   public void onPress() {
      super.onPress();
      this.setFocused(false);
   }
}
