package ic2.core.inventory.gui.components.base;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.feature.ITooltipProvider;
import ic2.core.platform.rendering.RenderUtils;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class IconButton extends ExtendedButton implements ITooltipProvider {
   ItemStack display;
   Component toolTip;
   boolean durability = false;
   boolean iconOnly = false;

   public IconButton(int xPos, int yPos, int width, int height, ItemStack display, OnPress handler) {
      super(xPos, yPos, width, height, Component.empty(), handler);
      this.display = display;
   }

   public IconButton setToolTip(Component toolTip) {
      this.toolTip = toolTip;
      return this;
   }

   public IconButton setToolTip(String s, Object... args) {
      return this.setToolTip(Component.translatable(s, args));
   }

   public IconButton setToolTip(String s) {
      return this.setToolTip(Component.translatable(s));
   }

   public IconButton setIconOnly() {
      this.iconOnly = true;
      return this;
   }

   public IconButton setDisplay(ItemStack stack) {
      this.display = stack;
      return this;
   }

   public IconButton setDurability(boolean show) {
      this.durability = show;
      return this;
   }

   public ItemStack getDisplay() {
      return this.display;
   }

   @Override
   public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
      if (this.visible) {
         if (!this.iconOnly) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partial);
         }

         Minecraft mc = Minecraft.getInstance();
         RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
         PoseStack mStack = guiGraphics.pose();
         mStack.pushPose();
         mStack.translate((double)this.getX(), (double)this.getY(), 0.0);
         float xScale = 20.0F / (float)this.width;
         float yScale = 20.0F / (float)this.height;
         mStack.scale(1.0F / xScale, 1.0F / yScale, 1.0F);
         Lighting.setupFor3DItems();
         RenderSystem.disableBlend();
         RenderUtils.renderGuiItem(mc.getItemRenderer(), mStack, this.display, 2.0F, 2.0F);
         if (this.durability) {
            RenderUtils.renderGuiItemDecorations(mStack, mc.font, this.display, 2, 2);
         }

         RenderSystem.disableCull();
         mStack.popPose();
      }
   }

   @Override
   public void addToolTip(IC2Screen gui, int x, int y, Consumer<Component> tooltip) {
      if (this.visible && this.isHoveredOrFocused() && this.toolTip != null) {
         tooltip.accept(this.toolTip);
      }
   }
}
