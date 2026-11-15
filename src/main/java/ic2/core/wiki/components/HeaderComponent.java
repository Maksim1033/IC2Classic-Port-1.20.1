package ic2.core.wiki.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HeaderComponent extends BaseWikiComponent {
   Component header;
   int offset;

   public HeaderComponent(Component header) {
      this(header, Minecraft.getInstance().font.split(header, 118).size());
   }

   public HeaderComponent(Component header, int lines) {
      super(6 + lines * 9);
      this.header = header;
      this.offset = lines * 9;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addSearchString(Consumer<Component> listener) {
      listener.accept(this.header);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      screen.drawColoredRegion(stack, (float)x, (float)(y + this.offset), 118.0F, 1.0F, -12829907);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderForeground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      for (FormattedCharSequence process : screen.getFont().split(this.header, 118)) {
         screen.drawCenterString(stack, process, x + 62, y, -12829907);
         y += 9;
      }
   }

   public Component getHeader() {
      return this.header;
   }
}
