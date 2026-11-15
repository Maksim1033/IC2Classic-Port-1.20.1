package ic2.core.inventory.gui.components.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.feature.ITooltipProvider;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.RenderUtils;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemCheckBox extends Button implements ITooltipProvider {
   OnPress listener;
   ItemStack display;
   boolean drawBackground = true;
   boolean checked;
   TextureAtlasSprite[] sprites;
   Component tooltip;

   public ItemCheckBox(int x, int y, int width, int height, OnPress onPress, ItemStack display, boolean checked) {
      this(x, y, width, height, onPress, display);
      this.setChecked(checked);
   }

   public ItemCheckBox(int x, int y, int width, int height, OnPress onPress, ItemStack display) {
      super(x, y, width, height, Component.empty(), onPress != null ? onPress : T -> {}, DEFAULT_NARRATION);
      this.listener = onPress;
      this.display = display;
      Map<String, TextureAtlasSprite> sprites = IC2Textures.getMappedEntriesIC2("misc/gui");
      this.sprites = new TextureAtlasSprite[]{sprites.get("yes"), sprites.get("no")};
   }

   public boolean isChecked() {
      return this.checked;
   }

   public ItemCheckBox disableBackground() {
      this.drawBackground = false;
      return this;
   }

   public ItemCheckBox setChecked(boolean checked) {
      this.checked = checked;
      return this;
   }

   public ItemCheckBox setToolTip(Component toolTip) {
      this.tooltip = toolTip;
      return this;
   }

   public ItemCheckBox setToolTip(String s, Object... args) {
      return this.setToolTip(Component.translatable(s, args));
   }

   public ItemCheckBox setToolTip(String s) {
      return this.setToolTip(Component.translatable(s));
   }

   public void onPress() {
      if (this.listener != null) {
         this.listener.onPress(this);
      } else {
         this.checked = !this.checked;
      }
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_230431_4_) {
      if (this.visible) {
         Minecraft mc = Minecraft.getInstance();
         if (this.drawBackground) {
            int k = 1;
            if (!this.active) {
               k = 0;
            } else if (this.isHoveredOrFocused()) {
               k = 2;
            }
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            guiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, 46 + k * 20);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         }

         Component buttonText = this.getMessage();
         if (buttonText != null && !buttonText.getString().isEmpty()) {
            int strWidth = mc.font.width(buttonText);
            int ellipsisWidth = mc.font.width("...");
            if (strWidth > this.width - 6 && strWidth > ellipsisWidth) {
               buttonText = Component.literal(mc.font.substrByWidth(buttonText, this.width - 6 - ellipsisWidth).getString() + "...");
            }
            int fgColor = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
            guiGraphics.drawCenteredString(mc.font, buttonText, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, fgColor);
         }

         RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
         PoseStack mStack = guiGraphics.pose();
         mStack.pushPose();
         mStack.translate((double)this.getX(), (double)this.getY(), 0.0);
         float xScale = 20.0F / (float)this.width;
         float yScale = 20.0F / (float)this.height;
         mStack.scale(1.0F / xScale, 1.0F / yScale, 1.0F);
         RenderUtils.renderGuiItem(mc.getItemRenderer(), mStack, this.display, 2.0F, 2.0F);
         RenderSystem.disableCull();
         int xOffset = this.width - this.width / 2;
         int yOffset = this.height - this.height / 2;
         mStack.popPose();
         IC2Screen.drawTexture(
            mStack,
            (float)(this.getX() + xOffset),
            (float)(this.getY() + yOffset),
            0.0F,
            this.sprites[this.checked ? 0 : 1],
            (float)(this.width / 2),
            (float)(this.height / 2)
         );
      }
   }

   @Override
   public void addToolTip(IC2Screen gui, int x, int y, Consumer<Component> tooltip) {
      if (this.visible && this.isHoveredOrFocused() && this.tooltip != null) {
         tooltip.accept(this.tooltip);
      }
   }
}
