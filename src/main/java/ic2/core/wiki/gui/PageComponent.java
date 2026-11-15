package ic2.core.wiki.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.inventory.gui.WrapperScreen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.wiki.base.book.BuildWiki;
import ic2.core.wiki.base.book.WikiPage;
import ic2.core.wiki.components.IWikiComponent;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PageComponent extends GuiWidget {
   public static final int TEXT_COLOR = -12829907;
   public static final Box2i LEFT_BUTTON = new Box2i(6, 174, 12, 12);
   public static final Box2i RIGHT_BUTTON = new Box2i(238, 174, 12, 12);
   public static final Box2i LEFT_PAGE = new Box2i(119, 175, 8, 10);
   public static final Box2i RIGHT_PAGE = new Box2i(129, 175, 8, 10);
   BuildWiki wiki;
   SharedWikiInfo info;
   int lastMouseX = 0;
   int lastMouseY = 0;

   public PageComponent(BuildWiki wiki, SharedWikiInfo info) {
      super(Box2i.EMPTY_BOX);
      this.wiki = wiki;
      this.info = info;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      if (this.wiki != null) {
         requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
         requests.add(GuiWidget.ActionRequest.TOOLTIP);
         requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
         requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
         requests.add(GuiWidget.ActionRequest.KEY_INPUT);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public KeyMapping getKey(String name) {
      for (KeyMapping binding : this.gui.getMinecraft().options.keyMappings) {
         if (binding.getName().equals(name)) {
            return binding;
         }
      }

      return null;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      this.lastMouseX = mouseX;
      this.lastMouseY = mouseY;
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      if (!this.info.isSearching()) {
         this.render(matrix, mouseX, mouseY, partialTicks, false, this.wiki.getCurrentPage(), x + 8, y + 9);
      }

      this.render(matrix, mouseX, mouseY, partialTicks, false, this.wiki.getNextPage(), x + 130, y + 9);
      this.gui.bindDefaultTexture();
      if (this.wiki.hasPrevPage()) {
         if (LEFT_BUTTON.isInBox(mouseX, mouseY)) {
            this.gui.drawTextureRegion(matrix, (float)(x + 6), (float)(y + 170), 151.0F, 240.0F, 16.0F, 16.0F);
         } else {
            this.gui.drawTextureRegion(matrix, (float)(x + 6), (float)(y + 174), 154.0F, 227.0F, 12.0F, 12.0F);
         }
      }

      if (this.wiki.hasNextPage()) {
         if (RIGHT_BUTTON.isInBox(mouseX, mouseY)) {
            this.gui.drawTextureRegion(matrix, (float)(x + 234), (float)(y + 170), 169.0F, 240.0F, 16.0F, 16.0F);
         } else {
            this.gui.drawTextureRegion(matrix, (float)(x + 238), (float)(y + 174), 167.0F, 227.0F, 12.0F, 12.0F);
         }
      }

      if (this.wiki.hasPageLink(true)) {
         this.gui
            .drawTextureRegion(
               matrix,
               (float)(x + LEFT_PAGE.getX()),
               (float)(y + LEFT_PAGE.getY()),
               31.0F,
               208.0F,
               (float)LEFT_PAGE.getWidth(),
               (float)LEFT_PAGE.getHeight(),
               ColorUtils.darker(-1, LEFT_PAGE.isInBox(mouseX, mouseY) ? 0.75F : 1.0F)
            );
      }

      if (this.wiki.hasPageLink(false)) {
         this.gui
            .drawTextureRegion(
               matrix,
               (float)(x + RIGHT_PAGE.getX()),
               (float)(y + RIGHT_PAGE.getY()),
               31.0F,
               208.0F,
               (float)RIGHT_PAGE.getWidth(),
               (float)RIGHT_PAGE.getHeight(),
               ColorUtils.darker(-1, RIGHT_PAGE.isInBox(mouseX, mouseY) ? 0.75F : 1.0F)
            );
      }

      if (!this.info.isSearching()) {
         this.render(matrix, mouseX, mouseY, partialTicks, true, this.wiki.getCurrentPage(), x + 8, y + 9);
      }

      this.render(matrix, mouseX, mouseY, partialTicks, true, this.wiki.getNextPage(), x + 130, y + 9);
      int current = this.wiki.getPageIndex() + 1;
      int max = this.wiki.getPageCount();
      this.gui.drawCenterString(matrix, this.string(current + "/" + max), x + 65, y + 178, 4210752);
      this.gui.drawCenterString(matrix, this.string(current + 1 + "/" + max), x + 190, y + 178, 4210752);
      RenderSystem.clear(256, Minecraft.ON_OSX);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (!this.info.hasOverlayOpen()) {
         int yOffset = 9;

         for (IWikiComponent comp : this.getPageData(this.wiki.getCurrentPage())) {
            comp.addToolTips(this.gui, 8, yOffset, matrix, mouseX, mouseY, tooltips);
            yOffset += comp.getHeight();
         }

         yOffset = 9;

         for (IWikiComponent comp : this.getPageData(this.wiki.getNextPage())) {
            comp.addToolTips(this.gui, 130, yOffset, matrix, mouseX, mouseY, tooltips);
            yOffset += comp.getHeight();
         }

         if (LEFT_PAGE.isInBox(mouseX, mouseY) && this.wiki.hasPageLink(true)) {
            tooltips.accept(this.translate("tooltip.ic2.wiki.bookmark.read"));
         }

         if (RIGHT_PAGE.isInBox(mouseX, mouseY) && this.wiki.hasPageLink(false)) {
            tooltips.accept(this.translate("tooltip.ic2.wiki.bookmark.read"));
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (this.info.hasOverlayOpen()) {
         return false;
      } else if (mouseButton != 0) {
         if (mouseButton == 1) {
            int result = this.wiki.pollHistory();
            if (result > 1) {
               this.info.setSearching(true);
            }

            return result > 0;
         } else {
            return false;
         }
      } else {
         int yOffset = 0;

         for (IWikiComponent comp : this.getPageData(this.wiki.getCurrentPage())) {
            if (comp.onMouseClick(this.gui, 0, yOffset, mouseX, mouseY, this.wiki)) {
               return true;
            }

            yOffset += comp.getHeight();
         }

         yOffset = 0;

         for (IWikiComponent comp : this.getPageData(this.wiki.getNextPage())) {
            if (comp.onMouseClick(this.gui, 122, yOffset, mouseX, mouseY, this.wiki)) {
               return true;
            }

            yOffset += comp.getHeight();
         }

         if (LEFT_BUTTON.isInBox(mouseX, mouseY) && this.wiki.hasPrevPage()) {
            this.wiki.flipBackwards(Screen.hasShiftDown());
            IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
            return true;
         } else if (RIGHT_BUTTON.isInBox(mouseX, mouseY) && this.wiki.hasNextPage()) {
            this.wiki.flipForward(Screen.hasShiftDown());
            IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
            return true;
         } else {
            if (LEFT_PAGE.isInBox(mouseX, mouseY)) {
               WikiPage page = this.wiki.getCurrentPage();
               if (page != null && this.wiki.tryOpenPage(page.getLink(), 0)) {
                  IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
                  return true;
               }
            }

            if (RIGHT_PAGE.isInBox(mouseX, mouseY)) {
               WikiPage page = this.wiki.getNextPage();
               if (page != null && this.wiki.tryOpenPage(page.getLink(), 0)) {
                  IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
                  return true;
               }
            }

            return false;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      if (!this.info.hasOverlayOpen()) {
         int yOffset = 0;

         for (IWikiComponent comp : this.getPageData(this.wiki.getCurrentPage())) {
            if (comp.onMouseScroll(this.gui, 0, yOffset, mouseX, mouseY, scroll, this.wiki)) {
               return true;
            }

            yOffset += comp.getHeight();
         }

         yOffset = 0;

         for (IWikiComponent comp : this.getPageData(this.wiki.getNextPage())) {
            if (comp.onMouseScroll(this.gui, 122, yOffset, mouseX, mouseY, scroll, this.wiki)) {
               return true;
            }

            yOffset += comp.getHeight();
         }

         if (scroll > 0) {
            this.wiki.flipForward(Screen.hasShiftDown());
            IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
            return true;
         }

         if (scroll < 0) {
            IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
            this.wiki.flipBackwards(Screen.hasShiftDown());
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean onMouseRelease(int mouseX, int mouseY, int mouseButton) {
      int yOffset = 0;

      for (IWikiComponent comp : this.getPageData(this.wiki.getCurrentPage())) {
         if (comp.onMouseRelease(this.gui, 0, yOffset, mouseX, mouseY)) {
            return true;
         }

         yOffset += comp.getHeight();
      }

      yOffset = 0;

      for (IWikiComponent comp : this.getPageData(this.wiki.getNextPage())) {
         if (comp.onMouseRelease(this.gui, 122, yOffset, mouseX, mouseY)) {
            return true;
         }

         yOffset += comp.getHeight();
      }

      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      if (this.info.hasOverlayOpen()) {
         return false;
      } else if (keyCode == 259) {
         int result = this.wiki.pollHistory();
         if (result > 1) {
            this.info.setSearching(true);
         }

         return result > 0;
      } else {
         KeyMapping mapping = this.getKey("key.jei.showRecipe");
         if (mapping != null && mapping.getKey().getValue() == keyCode) {
            ItemStack result = this.create(this.lastMouseX, this.lastMouseY);
            if (!result.isEmpty() && IC2.JEI_PLUGIN.isValid()) {
               this.gui.onClose();
               this.gui.getMinecraft().setScreen(new WrapperScreen(this.gui));
               IC2.JEI_PLUGIN.triggerRecipe(result);
               return true;
            }
         }

         mapping = this.getKey("key.jei.showUses");
         if (mapping != null && mapping.getKey().getValue() == keyCode) {
            ItemStack result = this.create(this.lastMouseX, this.lastMouseY);
            if (!result.isEmpty() && IC2.JEI_PLUGIN.isValid()) {
               this.gui.onClose();
               this.gui.getMinecraft().setScreen(new WrapperScreen(this.gui));
               IC2.JEI_PLUGIN.triggerUsage(result);
               return true;
            }
         }

         if (keyCode == 263 && this.wiki.hasPrevPage()) {
            this.wiki.flipBackwards(Screen.hasShiftDown());
            IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
            return true;
         } else if (keyCode == 262 && this.wiki.hasNextPage()) {
            this.wiki.flipForward(Screen.hasShiftDown());
            IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
            return true;
         } else if (keyCode == 264 && this.wiki.hasPrevPage()) {
            this.wiki.flipBackwards(true);
            IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
            return true;
         } else if (keyCode == 265 && this.wiki.hasNextPage()) {
            this.wiki.flipForward(true);
            IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.BOOK_PAGE_TURN.getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
            return true;
         } else {
            return false;
         }
      }
   }

   protected List<IWikiComponent> getPageData(WikiPage page) {
      return page == null ? Collections.emptyList() : page.getComponents();
   }

   @OnlyIn(Dist.CLIENT)
   public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks, boolean foreground, WikiPage page, int x, int y) {
      if (page != null) {
         int yOffset = 0;

         for (IWikiComponent comp : page.getComponents()) {
            this.gui.bindTexture(IWikiComponent.TEXTURE);
            if (foreground) {
               comp.renderForeground(this.gui, x, y + yOffset, matrix, mouseX, mouseY, partialTicks);
            } else {
               comp.renderBackground(this.gui, x, y + yOffset, matrix, mouseX, mouseY, partialTicks);
            }

            yOffset += comp.getHeight();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public ItemStack create(int mouseX, int mouseY) {
      int x = 8;
      int y = 9;
      int yOffset = 0;

      for (IWikiComponent comp : this.getPageData(this.wiki.getCurrentPage())) {
         ItemStack result = comp.getHoveredStack(this.gui, x, y + yOffset, mouseX, mouseY);
         if (!result.isEmpty()) {
            return result;
         }

         yOffset += comp.getHeight();
      }

      yOffset = 0;

      for (IWikiComponent comp : this.getPageData(this.wiki.getNextPage())) {
         ItemStack result = comp.getHoveredStack(this.gui, x + 122, y + yOffset, mouseX, mouseY);
         if (!result.isEmpty()) {
            return result;
         }

         yOffset += comp.getHeight();
      }

      return ItemStack.EMPTY;
   }
}
