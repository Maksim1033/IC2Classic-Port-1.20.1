package ic2.core.inventory.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.ComponentContainerScreen;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.gui.components.simple.IFilterTarget;
import ic2.core.item.tool.PainterTool;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.tooltips.ILangHelper;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class GuiWidget implements ILangHelper {
   public Box2i box;
   private boolean enabled = true;
   private boolean visible = true;
   public List<GuiWidget> children = CollectionUtils.<GuiWidget>createList();
   @OnlyIn(Dist.CLIENT)
   protected IC2Screen gui;

   public GuiWidget(Box2i box) {
      this.box = box;
   }

   protected <T extends GuiWidget> T addChild(T comp) {
      this.children.add(comp);
      return comp;
   }

   @OnlyIn(Dist.CLIENT)
   public final void setOwner(IC2Screen screen) {
      this.gui = screen;
   }

   public Box2i getBox() {
      return this.box;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean value) {
      this.visible = value;
   }

   public void setEnabled(boolean value) {
      this.enabled = value;
   }

   public boolean isMouseOver(int x, int y) {
      return this.box == Box2i.EMPTY_BOX || this.box.isInBox(x, y);
   }

   public final Set<GuiWidget.ActionRequest> createListeners() {
      Set<GuiWidget.ActionRequest> requests = CollectionUtils.<GuiWidget.ActionRequest>createLinkedSet();
      this.addRequests(requests);
      return requests;
   }

   protected abstract void addRequests(Set<GuiWidget.ActionRequest> var1);

   @OnlyIn(Dist.CLIENT)
   public void init(IC2Screen gui) {
   }

   @OnlyIn(Dist.CLIENT)
   public void tick(IC2Screen gui) {
   }

   @OnlyIn(Dist.CLIENT)
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
   }

   @OnlyIn(Dist.CLIENT)
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
   }

   @OnlyIn(Dist.CLIENT)
   public void postDraw(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
   }

   @OnlyIn(Dist.CLIENT)
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
   }

   @OnlyIn(Dist.CLIENT)
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean onMouseDragged(int mouseX, int mouseY, int mouseButton) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean onMouseRelease(int mouseX, int mouseY, int mouseButton) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean onKeyTyped(int keyCode) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean onCharTyped(char letter, int letterCode) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public void onClose() {
   }

   @OnlyIn(Dist.CLIENT)
   public void addItemTooltip(@Nullable Slot slot, ItemStack stack, List<Component> tooltips) {
   }

   @OnlyIn(Dist.CLIENT)
   protected final void handleColorClick(DyeColor currentColor, AbstractWidget highlight, boolean allowNull, Consumer<DyeColor> resultColor) {
      FilterComponent component = ((ComponentContainerScreen)this.gui).getComponentFromClass(FilterComponent.class);
      if (component != null && !component.isHelper()) {
         component.setGhost(
            IFilterTarget.simpleNonEmpty(
               highlight.getX() - this.gui.getGuiLeft(),
               highlight.getY() - this.gui.getGuiTop(),
               highlight.getWidth(),
               highlight.getHeight(),
               allowNull ? SpecialFilters.EMPTY_PAINTER : SpecialFilters.PAINTER,
               T -> {
                  if (T.getItem() instanceof PainterTool) {
                     DyeColor color = ((PainterTool)T.getItem()).getColor();
                     if (color != null || allowNull) {
                        resultColor.accept(color);
                     }
                  }
               }
            )
         );
      } else {
         if (Screen.hasControlDown()) {
            resultColor.accept(SpecialFilters.getPrevColor(currentColor, allowNull));
         } else {
            resultColor.accept(SpecialFilters.getNextColor(currentColor, allowNull));
         }
      }
   }

   protected final boolean hasEUReader() {
      return PlayerHandler.getClientHandler().hasEUReader();
   }

   protected final boolean hasCropScanner() {
      return PlayerHandler.getClientHandler().hasCropScanner();
   }

   protected final boolean hasThermometer() {
      return PlayerHandler.getClientHandler().hasThermometer();
   }

   public static enum ActionRequest implements CollectionUtils.IIndexEnum {
      GUI_INIT(0),
      GUI_TICK(1),
      GUI_CLOSE(2),
      DRAW_BACKGROUND(3),
      DRAW_FOREGROUND(4),
      DRAW_POST(5),
      TOOLTIP(6),
      MOUSE_INPUT(7),
      MOUSE_SCROLL(8),
      KEY_INPUT(9),
      ITEM_TOOLTIP(10);

      final int index;

      private ActionRequest(int index) {
         this.index = index;
      }

      @Override
      public int getIndex() {
         return this.index;
      }
   }
}
