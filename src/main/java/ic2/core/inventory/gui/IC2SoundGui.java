package ic2.core.inventory.gui;

import carbonconfiglib.config.ConfigEntry.DoubleValue;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.inventory.gui.components.base.ScrollSlider;
import ic2.core.utils.tooltips.ILangHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class IC2SoundGui extends Screen implements ILangHelper {
   Screen parent;

   public IC2SoundGui(Screen parent) {
      super(Component.literal("IC2 Sound Options"));
      this.parent = parent;
   }

   protected void init() {
      int x = this.width / 2;
      int y = this.height / 2;
      ((ScrollSlider)this.addRenderableWidget(
            new ScrollSlider(
               x - 155,
               y - 80,
               310,
               20,
               this.translate("gui.ic2.audio.master"),
               this.string("%"),
               0.0,
               100.0,
               IC2.CONFIG.masterVolume.get() * 100.0,
               T -> this.onSettingChanged(IC2.CONFIG.masterVolume, (double)T.getValueInt())
            )
         ))
         .setScrollEffect(1.0);
      ((ScrollSlider)this.addRenderableWidget(
            new ScrollSlider(
               x - 155,
               y - 55,
               150,
               20,
               this.translate("gui.ic2.audio.block"),
               this.string("%"),
               0.0,
               100.0,
               IC2.CONFIG.blockVolume.get() * 100.0,
               T -> this.onSettingChanged(IC2.CONFIG.blockVolume, (double)T.getValueInt())
            )
         ))
         .setScrollEffect(1.0);
      ((ScrollSlider)this.addRenderableWidget(
            new ScrollSlider(
               x + 5,
               y - 55,
               150,
               20,
               this.translate("gui.ic2.audio.item"),
               this.string("%"),
               0.0,
               100.0,
               IC2.CONFIG.itemVolume.get() * 100.0,
               T -> this.onSettingChanged(IC2.CONFIG.itemVolume, (double)T.getValueInt())
            )
         ))
         .setScrollEffect(1.0);
      ((ScrollSlider)this.addRenderableWidget(
            new ScrollSlider(
               x - 155,
               y - 30,
               150,
               20,
               this.translate("gui.ic2.audio.back"),
               this.string("%"),
               0.0,
               100.0,
               IC2.CONFIG.backVolume.get() * 100.0,
               T -> this.onSettingChanged(IC2.CONFIG.backVolume, (double)T.getValueInt())
            )
         ))
         .setScrollEffect(1.0);
      this.addRenderableWidget(new ExtendedButton(x + 5, y - 30, 150, 20, this.translate("gui.ic2.audio.clear"), T -> this.clearSounds()));
      this.addRenderableWidget(new ExtendedButton(x - 100, y, 200, 20, CommonComponents.GUI_DONE, T -> this.minecraft.setScreen(this.parent)));
   }

   public void clearSounds() {
      IC2.AUDIO.resetAll();
   }

   public void onSettingChanged(DoubleValue value, double newValue) {
      value.set(newValue * 0.01);
      IC2.CONFIG.save();
   }

   @Override
   public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(guiGraphics);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
      super.render(guiGraphics, mouseX, mouseY, partialTicks);
   }
}
