package ic2.core.wiki.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.utils.tooltips.ILangHelper;
import ic2.core.wiki.base.managers.IWikiProvider;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BaseWikiComponent implements IWikiComponent, ILangHelper {
   int height;

   public BaseWikiComponent(int height) {
      this.height = height;
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen screen) {
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addSearchString(Consumer<Component> listener) {
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderForeground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(IC2Screen screen, int x, int y, int mouseX, int mouseY, IWikiProvider provider) {
      return false;
   }

   @Override
   public boolean onMouseRelease(IC2Screen screen, int x, int y, int mouseX, int mouseY) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(IC2Screen screen, int x, int y, int mouseX, int mouseY, int scroll, IWikiProvider provider) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTips(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, Consumer<Component> tooltips) {
   }

   @OnlyIn(Dist.CLIENT)
   public void renderHovers(IC2Screen screen, PoseStack stack, Style component, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (component != null) {
         HoverEvent event = component.getHoverEvent();
         if (event != null) {
            if (event.getAction() == Action.SHOW_TEXT) {
               tooltips.accept((Component)event.getValue(Action.SHOW_TEXT));
            } else {
               GuiGraphics gg = screen.guiGraphics != null ? screen.guiGraphics : new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
               screen.renderComponentHoverEffect(gg, component, mouseX, mouseY);
            }
         }
      }
   }
}
