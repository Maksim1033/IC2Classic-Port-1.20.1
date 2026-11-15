package ic2.core.wiki.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Quaternionf;
import ic2.core.inventory.gui.IC2Font;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.MathUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.wiki.base.book.BuildWiki;
import ic2.core.wiki.helper.BookMark;
import ic2.core.wiki.helper.PageVisit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class BookMarkComponent extends GuiWidget {
   public static final int[] COLOR_SELECTION = new int[]{-1, -16777216, -16087809, -13577896, -10593050, -24822, -51366, -4236558, -47814, -10169601, -10742};
   public static final Box2i NEW_MARK = new Box2i(10, 186, 10, 29);
   public static final Predicate<BookMark> DELETER = BookMark::isPermanent;
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/wiki/book_components.png");
   public static final int TEXT_COLOR = -12829907;
   List<BookMark> bookmarks = CollectionUtils.<BookMark>createList();
   SharedWikiInfo info;
   BuildWiki wiki;

   public BookMarkComponent(BuildWiki wiki, SharedWikiInfo info) {
      super(Box2i.EMPTY_BOX);
      this.wiki = wiki;
      this.info = info;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      if (this.wiki != null) {
         requests.add(GuiWidget.ActionRequest.GUI_INIT);
         requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
         requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
         requests.add(GuiWidget.ActionRequest.KEY_INPUT);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      EditBox text = gui.addRenderableWidget(0, new ImprovedTextWidget(new IC2Font(gui.getFont()), 100 + gui.getGuiLeft(), 51 + gui.getGuiTop(), 90, 20));
      text.setTooltipDelay(950);
      text.setMaxLength(13);
      text.setBordered(false);
      text.setCanLoseFocus(true);
      text.setTextColor(-12829907);
      text.setTextColorUneditable(-12829907);
      text = gui.addRenderableWidget(1, new ImprovedTextWidget(new IC2Font(gui.getFont()), 102 + gui.getGuiLeft(), 66 + gui.getGuiTop(), 90, 20));
      text.setTooltipDelay(950);
      text.setFilter(MathUtils::isHexNumber);
      text.setMaxLength(6);
      text.setBordered(false);
      text.setCanLoseFocus(true);
      text.setTextColor(-12829907);
      text.setTextColorUneditable(-12829907);
      gui.addRenderableWidget(
         2,
         new ExtendedButton(
            77 + gui.getGuiLeft(),
            95 + gui.getGuiTop(),
            50,
            15,
            this.translate("tooltip.ic2.wiki.bookmark.confirm"),
            T -> {
               BookMark mark = new BookMark(
                  new PageVisit(this.wiki.getCurrentChapter(), this.wiki.getPage()),
                  color(((EditBox)gui.getCastedButton(1, EditBox.class)).getValue()),
                  this.string(((EditBox)gui.getCastedButton(0, EditBox.class)).getValue())
               );
               this.bookmarks.add(mark);
               this.toggleMark(gui, false);
               this.onMarksChanged();
            }
         )
      );
      gui.addRenderableWidget(
         3,
         new ExtendedButton(
            130 + gui.getGuiLeft(), 95 + gui.getGuiTop(), 50, 15, this.translate("tooltip.ic2.wiki.bookmark.cancel"), T -> this.toggleMark(gui, false)
         )
      );
      this.toggleMark(gui, false);
      this.bookmarks.clear();
      this.bookmarks.add(new BookMark(this.wiki.getMainPage(), ColorUtils.YELLOW, this.translate("tooltip.ic2.wiki.bookmark.main")).setPermanent());
      this.bookmarks.addAll(this.wiki.getBookMarks());

      while (this.bookmarks.size() > 14) {
         this.bookmarks.remove(this.bookmarks.size() - 1);
      }
   }

   public void onMarksChanged() {
      List<BookMark> marks = new ObjectArrayList(this.bookmarks);
      marks.removeIf(DELETER);
      this.wiki.updateLocations(marks);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop() + 10;
      RenderSystem.disableDepthTest();
      int i = 0;

      for (int m = this.bookmarks.size(); i < m; i++) {
         BookMark mark = this.bookmarks.get(i);
         if (i > 0 && this.bookmarks.get(i - 1).isPermanent() && !mark.isPermanent()) {
            y += 6;
         }

         int width = this.gui.getFont().width(mark.getName().getVisualOrderText());
         if (this.gui.isOverArea(mouseX, mouseY, -width - 5, y - this.gui.getGuiTop(), width + 10, 12)) {
            this.gui.drawTextureRegion(matrix, (float)(x - 4 - width), (float)y, 0.0F, 238.0F, (float)(width + 10), 10.0F, mark.getColor());
            this.gui.drawString(matrix, mark.getName(), x - width + 5, y + 1, ColorUtils.needsDarkColor(mark.getColor()) ? -12829907 : -1);
            this.gui.bindDefaultTexture();
         } else {
            this.gui.drawTextureRegion(matrix, (float)(x - 8), (float)y, 0.0F, 238.0F, 14.0F, 10.0F, mark.getColor());
         }

         y += 12;
      }

      if (this.bookmarks.size() < 14) {
         i = this.gui.getYSize() + this.gui.getGuiTop() - 20;
         this.gui.bindDefaultTexture();
         matrix.pushPose();
         matrix.translate((double)x, (double)i, 0.0);
         matrix.mulPose(Axis.ZP.rotationDegrees(-90.0F));
         matrix.translate((double)(-x), (double)(-i), 0.0);
         boolean extend = NEW_MARK.isInBox(mouseX, mouseY);
         this.gui
            .drawTextureRegion(matrix, (float)(x - 34 - (extend ? 8 : 0)), (float)(i + 10), 0.0F, 238.0F, (float)(20 + (extend ? 8 : 0)), 11.0F, -16737997);
         this.gui.drawString(matrix, this.string("+"), x - 23 - (extend ? 8 : 0), i + 11, -12829907);
         matrix.popPose();
      }

      if (this.info.isBookmarking()) {
         matrix.pushPose();
         matrix.translate(0.0, 0.0, 1000.0);
         x = this.gui.getGuiLeft();
         y = this.gui.getGuiTop();
         this.gui.bindTexture(TEXTURE);
         this.gui.drawTextureRegion(matrix, (float)(x + 65), (float)(y + 30), 0.0F, 0.0F, 124.0F, 93.0F);
         this.gui.drawTextureRegion(matrix, (float)(x + 98), (float)(y + 49), 0.0F, 94.0F, 81.0F, 12.0F, 74.0F, 11.0F);
         this.gui.drawTextureRegion(matrix, (float)(x + 100), (float)(y + 64), 0.0F, 94.0F, 40.0F, 12.0F, 74.0F, 11.0F);

         for (int ix = 0; ix < COLOR_SELECTION.length; ix++) {
            int posX = x + 72 + 10 * ix;
            int posY = y + 80;
            this.gui.drawTextureRegion(matrix, (float)posX, (float)posY, 76.0F, 95.0F, 9.0F, 9.0F);
            this.gui.drawColoredRegion(matrix, (float)(posX + 1), (float)(posY + 1), 7.0F, 7.0F, COLOR_SELECTION[ix]);
         }

         this.gui.drawTextureRegion(matrix, (float)(x + 145), (float)(y + 63), 76.0F, 95.0F, 13.0F, 13.0F, 9.0F, 9.0F);
         this.gui
            .drawColoredRegion(matrix, (float)(x + 146), (float)(y + 64), 11.0F, 11.0F, color(((EditBox)this.gui.getCastedButton(1, EditBox.class)).getValue()));
         this.gui.drawCenterString(matrix, this.translate("tooltip.ic2.wiki.bookmark.new"), x + 127, y + 35, -12829907);
         this.gui.drawString(matrix, this.translate("tooltip.ic2.wiki.bookmark.name"), x + 70, y + 50, -12829907);
         this.gui.drawString(matrix, this.translate("tooltip.ic2.wiki.bookmark.color"), x + 70, y + 65, -12829907);
         matrix.popPose();
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (this.info.isBookmarking()) {
         for (int i = 0; i < COLOR_SELECTION.length; i++) {
            int posX = 72 + 10 * i;
            if (mouseX >= posX && mouseX <= posX + 9 && mouseY >= 80 && mouseY <= 89) {
               ((EditBox)this.gui.getCastedButton(1, EditBox.class))
                  .setValue(Integer.toHexString(16777216 | COLOR_SELECTION[i] & 16777215).substring(1).toUpperCase(Locale.ROOT));
               return true;
            }
         }
      } else {
         if (NEW_MARK.isInBox(mouseX, mouseY) && this.bookmarks.size() < 14) {
            this.toggleMark(this.gui, !this.info.hasOverlayOpen());
            if (this.info.hasOverlayOpen()) {
               Component comp = this.wiki.getCurrentHeader();
               String value = comp != null ? ChatFormatting.stripFormatting(comp.getString()) : "";
               ((EditBox)this.gui.getCastedButton(0, EditBox.class)).setValue(value.length() > 13 ? value.substring(0, 13) : value);
            }

            return true;
         }

         boolean moved = false;

         for (int ix = 0; ix < this.bookmarks.size(); ix++) {
            BookMark mark = this.bookmarks.get(ix);
            int width = this.gui.getFont().width(mark.getName().getVisualOrderText());
            if (this.gui.isOverArea(mouseX, mouseY, 4 - width, 10 + ix * 12 + (moved ? 6 : 0), width + 10, 12)) {
               if (!Screen.hasShiftDown()) {
                  this.wiki.openPage(mark.getLocation(), true);
                  return true;
               }

               if (!mark.isPermanent()) {
                  this.bookmarks.remove(ix);
                  this.onMarksChanged();
                  return true;
               }
               break;
            }

            if (ix > 0 && this.bookmarks.get(ix - 1).isPermanent() && !mark.isPermanent()) {
               moved = true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean onKeyTyped(int keyCode) {
      if (this.info.isBookmarking()) {
         if (keyCode == 69) {
            return true;
         }

         if (keyCode == 256) {
            this.toggleMark(this.gui, false);
            return true;
         }
      }

      return false;
   }

   @OnlyIn(Dist.CLIENT)
   protected void toggleMark(IC2Screen screen, boolean result) {
      this.info.setBookMarking(result);
      screen.getButton(0).visible = result;
      screen.getButton(1).visible = result;
      screen.getButton(2).visible = result;
      screen.getButton(3).visible = result;
   }

   public static int color(String s) {
      try {
         return Integer.decode("#" + s) | 0xFF000000;
      } catch (Exception var2) {
         return -12829907;
      }
   }
}
