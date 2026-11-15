package ic2.core.inventory.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.feature.ICloseableComponent;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ComponentContainerScreen extends IC2Screen {
   List<GuiWidget>[] components = CollectionUtils.createLists(GuiWidget.ActionRequest.values().length);
   List<GuiWidget> allComponents = CollectionUtils.<GuiWidget>createList();
   List<ICloseableComponent> closeable = CollectionUtils.<ICloseableComponent>createList();

   public ComponentContainerScreen(ContainerComponent<?> container) {
      super(container, container.getPlayer().getInventory(), container.getName());
      this.setTexture(container.getTexture());
      this.registerComponents(container.getComponents());
      container.onGuiLoaded(this);
   }

   public void registerComponents(List<GuiWidget> comps) {
      for (GuiWidget comp : comps) {
         this.registerComponent(comp);
      }
   }

   public void registerComponent(GuiWidget comp) {
      comp.setOwner(this);

      for (GuiWidget.ActionRequest event : comp.createListeners()) {
         this.components[event.getIndex()].add(comp);
      }

      this.allComponents.add(comp);
      if (comp instanceof ICloseableComponent) {
         this.closeable.add((ICloseableComponent)comp);
      }

      for (GuiWidget child : comp.children) {
         this.registerComponent(child);
      }
   }

   public List<GuiWidget> getComponents(GuiWidget.ActionRequest event) {
      return this.components[event.getIndex()];
   }

   @Override
   public List<Component> getTooltipFromContainerItem(ItemStack stack) {
      List<Component> comp = super.getTooltipFromContainerItem(stack);
      Slot slot = this.getSlotUnderMouse();

      for (GuiWidget entry : this.getComponents(GuiWidget.ActionRequest.ITEM_TOOLTIP)) {
         entry.addItemTooltip(slot, stack, comp);
      }

      return comp;
   }

   public List<GuiWidget> getComponents() {
      return this.allComponents;
   }

   public <T extends GuiWidget> T getComponentFromClass(Class<T> clz) {
      for (GuiWidget comp : this.allComponents) {
         if (clz.isInstance(comp)) {
            return (T)comp;
         }
      }

      return null;
   }

   @Override
   public void closeAllComponents() {
      int i = 0;

      for (int m = this.closeable.size(); i < m; i++) {
         this.closeable.get(i).closeComponent(this);
      }
   }

   @Override
   protected void init() {
      super.init();

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.GUI_INIT)) {
         comp.init(this);
      }
   }

   @Override
   public void removed() {
      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.GUI_CLOSE)) {
         comp.onClose();
      }

      super.removed();
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
      if (this.hasFlag(64)) {
         this.clearFlag(64);
         this.setTexture(this.getCastedContainer(ContainerComponent.class).getTexture());
      }

      super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
      PoseStack stack = guiGraphics.pose();
      int x = mouseX - this.getGuiLeft();
      int y = mouseY - this.getGuiTop();

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.DRAW_BACKGROUND)) {
         if (comp.isEnabled() && comp.isVisible()) {
            this.bindDefaultTexture();
            comp.drawBackground(stack, x, y, partialTicks);
         }
      }
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderLabels(guiGraphics, mouseX, mouseY);
      PoseStack stack = guiGraphics.pose();
      int x = mouseX - this.getGuiLeft();
      int y = mouseY - this.getGuiTop();

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.DRAW_FOREGROUND)) {
         if (comp.isEnabled() && comp.isVisible()) {
            comp.drawForeground(stack, x, y);
         }
      }
   }

   @Override
   public void onPostDraw(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
      int x = mouseX - this.getGuiLeft();
      int y = mouseY - this.getGuiTop();

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.DRAW_POST)) {
         if (comp.isEnabled() && comp.isVisible()) {
            comp.postDraw(stack, x, y, partialTicks);
         }
      }
   }

   @Override
   public void onTooltipCollection(PoseStack stack, int mouseX, int mouseY, Consumer<Component> tooltips) {
      int x = mouseX - this.getGuiLeft();
      int y = mouseY - this.getGuiTop();

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.TOOLTIP)) {
         if (comp.isEnabled() && comp.isVisible()) {
            comp.addTooltips(stack, x, y, tooltips);
         }
      }
   }

   @Override
   public void containerTick() {
      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.GUI_TICK)) {
         if (comp.isEnabled()) {
            comp.tick(this);
         }
      }

      super.containerTick();
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
      int x = (int)(mouseX - (double)this.getGuiLeft());
      int y = (int)(mouseY - (double)this.getGuiTop());

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.MOUSE_INPUT)) {
         if (comp.isEnabled() && comp.isVisible() && comp.isMouseOver(x, y) && comp.onMouseClick(x, y, mouseButton)) {
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double motionX, double motionY) {
      int x = (int)(mouseX - (double)this.getGuiLeft());
      int y = (int)(mouseY - (double)this.getGuiTop());

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.MOUSE_INPUT)) {
         if (comp.isEnabled() && comp.isVisible() && comp.onMouseDragged(x, y, mouseButton)) {
            return true;
         }
      }

      return super.mouseDragged(mouseX, mouseY, mouseButton, motionX, motionY);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
      int x = (int)(mouseX - (double)this.getGuiLeft());
      int y = (int)(mouseY - (double)this.getGuiTop());

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.MOUSE_INPUT)) {
         if (comp.isEnabled() && comp.isVisible() && comp.onMouseRelease(x, y, mouseButton)) {
            return true;
         }
      }

      return super.mouseReleased(mouseX, mouseY, mouseButton);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
      int x = (int)(mouseX - (double)this.getGuiLeft());
      int y = (int)(mouseY - (double)this.getGuiTop());

      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.MOUSE_SCROLL)) {
         if (comp.isEnabled() && comp.isVisible() && comp.isMouseOver(x, y) && comp.onMouseScroll(x, y, (int)scroll)) {
            return true;
         }
      }

      return super.mouseScrolled(mouseX, mouseY, scroll);
   }

   public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.KEY_INPUT)) {
         if (comp.isEnabled() && comp.onKeyTyped(key)) {
            return true;
         }
      }

      return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
   }

   public boolean charTyped(char letter, int letterCode) {
      for (GuiWidget comp : this.getComponents(GuiWidget.ActionRequest.KEY_INPUT)) {
         if (comp.isEnabled() && comp.onCharTyped(letter, letterCode)) {
            return true;
         }
      }

      return super.charTyped(letter, letterCode);
   }
}
