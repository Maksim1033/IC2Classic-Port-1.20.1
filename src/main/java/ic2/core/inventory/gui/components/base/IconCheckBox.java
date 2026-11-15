package ic2.core.inventory.gui.components.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.feature.ITooltipProvider;
import ic2.core.platform.rendering.IC2Textures;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IconCheckBox extends Button implements ITooltipProvider {
   static final OnPress NOTHING = T -> {
   };
   boolean checked;
   boolean itemLike = false;
   boolean disableLike = false;
   boolean disableScale = false;
   TextureAtlasSprite[] sprites;
   Component toolTip;
   OnPress listener = null;

   public IconCheckBox(int x, int y, int width, int height, Component text, boolean checked) {
      super(x, y, width, height, text, NOTHING, DEFAULT_NARRATION);
      this.checked = checked;
      Map<String, TextureAtlasSprite> sprites = IC2Textures.getMappedEntriesIC2("misc/gui");
      this.setIcons(new TextureAtlasSprite[]{sprites.get("yes"), sprites.get("no")});
   }

   public IconCheckBox setIcons(TextureAtlasSprite[] sprite) {
      if (sprite.length != 2) {
         return this;
      } else {
         this.sprites = sprite;
         return this;
      }
   }

   public IconCheckBox setItemLike(boolean value) {
      this.itemLike = value;
      return this;
   }

   public IconCheckBox disableScale(boolean value) {
      this.disableScale = value;
      return this;
   }

   public IconCheckBox setDisableLike(boolean value) {
      this.disableLike = value;
      return this;
   }

   public IconCheckBox setToolTip(Component toolTip) {
      this.toolTip = toolTip;
      return this;
   }

   public IconCheckBox setToolTip(String s, Object... args) {
      return this.setToolTip(Component.translatable(s, args));
   }

   public IconCheckBox setToolTip(String s) {
      return this.setToolTip(Component.translatable(s));
   }

   public boolean isChecked() {
      return this.checked;
   }

   public IconCheckBox setChecked(boolean checked) {
      this.checked = checked;
      return this;
   }

   public IconCheckBox setListener(OnPress listener) {
      this.listener = listener;
      return this;
   }

   public void onPress() {
      this.checked = !this.checked;
      if (this.listener != null) {
         this.listener.onPress(this);
      }
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (this.visible) {
         Minecraft mc = Minecraft.getInstance();
         int k = 1;
         if (!this.active) {
            k = 0;
         } else if (this.isHoveredOrFocused()) {
            k = 2;
         }
         if (this.disableLike && this.checked && k != 2) {
            k = 0;
         }

         guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         guiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, 46 + k * 20);
         guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

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

         if (!this.disableLike) {
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            PoseStack mStack = guiGraphics.pose();
            if (this.itemLike) {
               int xOffset = this.width - (this.disableScale ? 8 : this.width / 2);
               int yOffset = this.height - (this.disableScale ? 8 : this.height / 2);
               IC2Screen.drawTexture(
                  mStack,
                  (float)(this.getX() + xOffset),
                  (float)(this.getY() + yOffset),
                  0.0F,
                  this.sprites[this.checked ? 0 : 1],
                  this.disableScale ? 8.0F : (float)(this.width / 2),
                  this.disableScale ? 8.0F : (float)(this.height / 2)
               );
            } else {
               IC2Screen.drawTexture(
                  mStack,
                  (float)(this.getX() + 1),
                  (float)(this.getY() + 1),
                  0.0F,
                  this.sprites[this.checked ? 0 : 1],
                  (float)(this.width - 2),
                  (float)(this.height - 2)
               );
            }
         }
      }
   }

   @Override
   public void addToolTip(IC2Screen gui, int x, int y, Consumer<Component> tooltip) {
      if (this.isHoveredOrFocused() && this.visible && this.toolTip != null) {
         tooltip.accept(this.toolTip);
      }
   }
}
