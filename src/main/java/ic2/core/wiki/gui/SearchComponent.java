package ic2.core.wiki.gui;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;
import ic2.core.inventory.gui.IC2Font;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.wiki.base.book.BuildWiki;
import ic2.core.wiki.helper.PageVisit;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SearchComponent extends GuiWidget {
   public Box2i NEW_MARK = new Box2i(25, 186, 10, 29);
   public Box2i CONTROLS = new Box2i(40, 186, 10, 29);
   BuildWiki wiki;
   SharedWikiInfo info;
   List<PageVisit> foundElements = CollectionUtils.<PageVisit>createList();
   long[] offsets = new long[0];
   int offset = 0;

   public SearchComponent(BuildWiki wiki, SharedWikiInfo info) {
      super(Box2i.EMPTY_BOX);
      this.wiki = wiki;
      this.info = info;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      EditBox text = gui.addRenderableWidget(-1, new ImprovedTextWidget(new IC2Font(gui.getFont()), 11 + gui.getGuiLeft(), 23 + gui.getGuiTop(), 108, 12));
      text.setTooltipDelay(950);
      text.setMaxLength(128);
      text.setBordered(false);
      text.setCanLoseFocus(true);
      text.setValue(this.wiki.getSearchKey());
      text.setTextColor(-12829907);
      text.setTextColorUneditable(-12829907);
      text.setResponder(T -> {
         this.foundElements.clear();
         this.foundElements.addAll(this.wiki.getPages(T));
         this.wiki.setSearchKey(T);
         this.updateValues();
      });
      text.visible = false;
      text.active = false;
      this.foundElements.clear();
      this.foundElements.addAll(this.wiki.getPages(this.wiki.getSearchKey()));
      this.updateValues();
   }

   private void updateValues() {
      this.offsets = new long[this.foundElements.size()];
      long time = GuiUtils.currentMillseconds();
      Arrays.setAll(this.offsets, T -> time);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ImprovedTextWidget widget = gui.getCastedButton(-1, ImprovedTextWidget.class);
      if (widget.visible != this.info.isSearching() && this.info.isSearching()) {
         gui.setFocused(widget);
         widget.setFocused(true);
         this.offset = 0;
      }

      widget.visible = this.info.isSearching();
      widget.active = this.info.isSearching();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      int currentY = this.gui.getYSize() + this.gui.getGuiTop() - 20;
      this.gui.bindDefaultTexture();
      matrix.pushPose();
      matrix.translate((double)x, (double)currentY, 0.0);
      matrix.mulPose(Axis.ZP.rotationDegrees(-90.0F));
      matrix.translate((double)(-x), (double)(-currentY), 0.0);
      boolean extend = this.CONTROLS.isInBox(mouseX, mouseY);
      if (this.wiki.getControls() != null) {
         this.gui
            .drawTextureRegion(
               matrix, (float)(x - 34 - (extend ? 8 : 0)), (float)(currentY + 40), 0.0F, 238.0F, (float)(20 + (extend ? 8 : 0)), 11.0F, -4827644
            );
         this.gui.bindTexture(BookMarkComponent.TEXTURE);
         matrix.popPose();
         this.gui.drawTextureRegion(matrix, (float)x + 42.5F, (float)(currentY + 15 + (extend ? 8 : 0)), 124.01F, 57.0F, 4.0F, 10.0F, 6.0F, 15.0F);
         matrix.pushPose();
         matrix.translate((double)x, (double)currentY, 0.0);
         matrix.mulPose(Axis.ZP.rotationDegrees(-90.0F));
         matrix.translate((double)(-x), (double)(-currentY), 0.0);
         this.gui.bindDefaultTexture();
      }

      extend = this.NEW_MARK.isInBox(mouseX, mouseY);
      this.gui
         .drawTextureRegion(matrix, (float)(x - 34 - (extend ? 8 : 0)), (float)(currentY + 25), 0.0F, 238.0F, (float)(20 + (extend ? 8 : 0)), 11.0F, -16664603);
      this.gui.bindTexture(BookMarkComponent.TEXTURE);
      this.gui.drawTextureRegion(matrix, (float)(x - 25 - (extend ? 8 : 0)), (float)(currentY + 26), 124.0F, 30.0F, 10.0F, 10.0F, 12.0F, 12.0F);
      matrix.popPose();
      if (this.info.isSearching()) {
         this.gui.bindDefaultTexture();
         this.gui.drawTextureRegion(matrix, (float)(x + 6), (float)(y + 6), 6.0F, 6.0F, 122.0F, 180.0F);
         this.gui.drawTextureRegion(matrix, (float)(x + 9), (float)(y + 35), 0.0F, 248.0F, 116.0F, 4.0F);
         this.gui.drawTextureRegion(matrix, (float)(x + 9), (float)(y + 180), 0.0F, 252.0F, 116.0F, 4.0F);
         this.gui.bindTexture(BookMarkComponent.TEXTURE);
         this.gui.drawTextureRegion(matrix, (float)(x + 10), (float)(y + 20), 0.0F, 94.0F, 114.0F, 12.0F, 74.0F, 11.0F);
      }
   }

   private long startTime(int index) {
      return index >= 0 && index < this.offsets.length ? this.offsets[index] : GuiUtils.currentMillseconds();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      if (this.info.isSearching()) {
         this.gui.drawString(matrix, this.string("Search:"), 10, 10, -12829907);
         int i = 0;

         GuiGraphics gg = this.gui.guiGraphics != null ? this.gui.guiGraphics : new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
         for (int j = this.offset; j < this.foundElements.size() && i < 14; j++) {
            GuiUtils.drawScrollingText(
               gg,
               this.gui.getFont(),
               this.foundElements.get(j).getSearch(this.gui.getFont()),
               10.0F,
               (float)(40 + i * 10),
               118.0F,
               10.0F,
               Align.START,
               -12829907,
               this.startTime(j)
            );
            i++;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (this.NEW_MARK.isInBox(mouseX, mouseY)) {
         this.info.setSearching(!this.info.isSearching());
         return true;
      } else if (this.wiki.getControls() != null && this.CONTROLS.isInBox(mouseX, mouseY)) {
         this.wiki.openPage(this.wiki.getControls(), true);
         return true;
      } else if (!this.info.isSearching()) {
         return false;
      } else if (mouseX >= 10 && mouseX <= 118) {
         int index = (mouseY - 40) / 10;
         if (index >= 0 && index < this.foundElements.size()) {
            if (this.wiki.openPage(this.foundElements.get(index), true, true)) {
               this.info.setSearching(false);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      if (!this.info.isSearching()) {
         return false;
      } else {
         EditBox text = this.gui.getCastedButton(-1, EditBox.class);
         return text != null && text.isFocused() && keyCode == 69;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      if (!this.info.isSearching()) {
         return false;
      } else if (mouseX >= 10 && mouseX <= 118 && mouseY >= 40) {
         this.offset = Mth.clamp(this.offset - scroll, 0, Math.max(0, this.foundElements.size() - 14));
         return true;
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (this.info.isSearching()) {
         if (mouseX >= 10 && mouseX <= 118) {
            int index = (mouseY - 40) / 10;
            if (index + this.offset >= 0 && index + this.offset < this.foundElements.size()) {
               Style style = IC2Screen.getComponentNew(
                  ObjectLists.singleton(this.foundElements.get(index + this.offset).getSearch(this.gui.getFont()).getVisualOrderText()),
                  mouseX - 10,
                  mouseY - (40 + index * 10)
               );
               if (style != null) {
                  HoverEvent event = style.getHoverEvent();
                  if (event != null) {
                     Component comp = (Component)event.getValue(Action.SHOW_TEXT);
                     if (comp != null) {
                        tooltips.accept(comp);
                     }
                  }
               }
            }
         }
      }
   }
}
