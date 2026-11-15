package ic2.core.inventory.gui.components.base;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.platform.rendering.RenderUtils;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ScreenUtils;

public class ColorButton extends ToolTipButton {
   DyeColor color;
   ItemStack stack = ItemStack.EMPTY;
   Consumer<ColorButton> handler;

   public ColorButton(int xPos, int yPos, int width, int height, DyeColor color, Consumer<ColorButton> handler) {
      super(xPos, yPos, width, height, Component.empty(), null);
      this.handler = handler;
      this.setColor(color);
   }

   public ColorButton setColor(DyeColor color) {
      this.color = color;
      this.stack = SpecialFilters.createDyeItem(color);
      return this;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public void onPress() {
      this.handler.accept(this);
   }

   @Override
   public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
      if (this.visible) {
         super.renderWidget(guiGraphics, mouseX, mouseY, partial);
         if (!this.stack.isEmpty()) {
            PoseStack mStack = guiGraphics.pose();
            mStack.pushPose();
            mStack.translate((double)this.getX(), (double)this.getY(), 0.0);
            float xScale = 20.0F / (float)this.width;
            float yScale = 20.0F / (float)this.height;
            mStack.scale(1.0F / xScale, 1.0F / yScale, 1.0F);
            guiGraphics.renderItem(this.stack, 2, 2);
            mStack.popPose();
         }
      }
   }
}
