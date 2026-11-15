package ic2.core.wiki.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.wiki.base.managers.IWikiProvider;
import ic2.core.wiki.components.builders.CategoryObj;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CategoryComponent extends BaseWikiComponent {
   Component comp;
   CategoryObj.Link headerLink;
   List<CategoryObj.Link> links;
   float[] animationProgress;

   public CategoryComponent(Component comp, CategoryObj.Link headerLink, List<CategoryObj.Link> links) {
      super(16 + Mth.ceil((float)links.size() / 4.0F) * 20);
      this.comp = comp;
      this.headerLink = headerLink;
      this.links = links;
      this.animationProgress = new float[links.size()];
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(IC2Screen screen, int x, int y, int mouseX, int mouseY, IWikiProvider provider) {
      if (this.links.isEmpty()) {
         return false;
      } else {
         if (this.headerLink != null) {
            int width = screen.getFont().width(this.comp);
            int min = 59 - width / 2;
            if (mouseX >= x + min && mouseX <= x + min + width && mouseY >= y && mouseY <= y + 10) {
               return provider.handleClickEvent(new ClickEvent(Action.CHANGE_PAGE, "redirect;" + this.headerLink.getLink().toString()));
            }
         }

         int offset = 70 - Math.min(this.links.size() / 2, 2) * 25;
         int i = 0;

         for (int m = this.links.size(); i < m; i++) {
            int xOff = i % 4 * 25 + x + offset;
            int yOff = i / 4 * 20 + y + 24;
            if (mouseX >= xOff && mouseX <= xOff + 20 && mouseY >= yOff && mouseY <= yOff + 20) {
               return provider.handleClickEvent(new ClickEvent(Action.CHANGE_PAGE, "redirect;" + this.links.get(i).getLink().toString()));
            }
         }

         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTips(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (!this.links.isEmpty()) {
         if (this.headerLink != null) {
            int width = screen.getFont().width(this.comp);
            int min = 59 - width / 2;
            if (mouseX >= x + min && mouseX <= x + min + width && mouseY >= y && mouseY <= y + 10) {
               tooltips.accept(this.headerLink.getTooltip());
            }
         }

         int offset = 59 - Math.min(this.links.size() / 2, 2) * 25;
         int i = 0;

         for (int m = this.links.size(); i < m; i++) {
            int xOff = i % 4 * 25 + x + offset;
            int yOff = i / 4 * 20 + y + 12;
            if (mouseX >= xOff && mouseX <= xOff + 20 && mouseY >= yOff && mouseY <= yOff + 20) {
               tooltips.accept(this.links.get(i).getTooltip());
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      if (!this.links.isEmpty()) {
         screen.drawCenterString(stack, this.comp, x + 59, y, -16777216);
         RenderSystem.setShaderTexture(0, screen.getActiveTexture().getId());
         screen.drawTextureRegion(stack, (float)(x + 1), (float)(y + 10), 0.0F, 248.0F, 116.0F, 4.0F);
         screen.drawTextureRegion(stack, (float)(x + 1), (float)(y + 10 + Mth.ceil((float)this.links.size() / 4.0F) * 20), 0.0F, 252.0F, 116.0F, 4.0F);
         int offset = 59 - Math.min(2, this.links.size() / 2) * 25;
         int i = 0;

         for (int m = this.links.size(); i < m; i++) {
            screen.drawItemStack(stack, (float)(x + offset + i % 4 * 25), (float)(y + 12 + i / 4 * 20), this.links.get(i).getItem());
         }

         RenderSystem.setShaderTexture(0, screen.getActiveTexture().getId());
         RenderSystem.enableBlend();
         mouseX += screen.getGuiLeft();
         mouseY += screen.getGuiTop();
         i = 0;

         for (int m = this.links.size(); i < m; i++) {
            int xOff = i % 4 * 25 + x + offset;
            int yOff = i / 4 * 20 + y + 12;
            if (mouseX >= xOff && mouseX <= xOff + 20 && mouseY >= yOff && mouseY <= yOff + 20) {
               this.animationProgress[i] = Math.min(1.0F, this.animationProgress[i] + partialTicks * 0.1F);
            } else if (this.animationProgress[i] > 0.0F) {
               this.animationProgress[i] = Math.max(0.0F, this.animationProgress[i] - partialTicks * 0.1F);
            }

            if (this.animationProgress[i] <= 0.0F) {
               screen.drawTextureRegion(
                  stack, (float)xOff, (float)yOff, (float)(xOff - screen.getGuiLeft()), (float)(yOff - screen.getGuiTop()), 20.0F, 20.0F, -1342177281
               );
            } else if (this.animationProgress[i] < 1.0F) {
               float forward = 20.0F * this.animationProgress[i] * 0.5F;
               screen.drawTextureRegion(
                  stack,
                  (float)xOff,
                  (float)yOff,
                  (float)(xOff - screen.getGuiLeft()),
                  (float)(yOff - screen.getGuiTop()),
                  10.0F - forward,
                  10.0F - forward,
                  -1342177281
               );
               screen.drawTextureRegion(
                  stack,
                  (float)(xOff + 10) + forward,
                  (float)yOff,
                  (float)(xOff - screen.getGuiLeft()),
                  (float)(yOff - screen.getGuiTop()),
                  10.0F - forward,
                  10.0F - forward,
                  -1342177281
               );
               screen.drawTextureRegion(
                  stack,
                  (float)xOff,
                  (float)(yOff + 10) + forward,
                  (float)(xOff - screen.getGuiLeft()),
                  (float)(yOff - screen.getGuiTop()),
                  10.0F - forward,
                  10.0F - forward,
                  -1342177281
               );
               screen.drawTextureRegion(
                  stack,
                  (float)(xOff + 10) + forward,
                  (float)(yOff + 10) + forward,
                  (float)(xOff - screen.getGuiLeft()),
                  (float)(yOff - screen.getGuiTop()),
                  10.0F - forward,
                  10.0F - forward,
                  -1342177281
               );
            }
         }

         RenderSystem.disableBlend();
      }
   }
}
