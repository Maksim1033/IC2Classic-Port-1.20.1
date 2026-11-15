package ic2.core.item.inv.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ColorButton;
import ic2.core.inventory.gui.components.base.IconCheckBox;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.item.inv.inventory.CardInventory;
import ic2.core.item.tool.infos.BaseTileInfoProvider;
import ic2.core.networking.buffers.data.StringDataBuffer;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CardComponent extends GuiWidget {
   CardInventory inventory;

   public CardComponent(CardInventory inventory) {
      super(Box2i.EMPTY_BOX);
      this.inventory = inventory;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      ImprovedTextWidget name = new ImprovedTextWidget(x + 9, y + 27, 82, 20);
      name.setCanLoseFocus(true);
      name.setMaxLength(128);
      name.setBordered(false);
      name.setValue(this.inventory.name);
      name.setResponder(this::updateName);
      gui.addRenderableWidget(-1, name);

      for (BaseTileInfoProvider.SettingEntry entry : this.inventory.settings) {
         switch (entry.type) {
            case COLOR:
               ((ColorButton)gui.addRenderableWidget(
                     new ColorButton(x + entry.x, y + entry.y, 16, 16, this.inventory.getColor(entry.index), T -> this.togglePaint(entry.index, T))
                  ))
                  .setToolTip(entry.tooltip);
               break;
            case SIMPLE:
               ((ItemCheckBox)gui.addRenderableWidget(
                     entry.index, new ItemCheckBox(x + entry.x, y + entry.y, 16, 16, T -> this.toggleFlag(entry.index), (ItemStack)entry.display)
                  ))
                  .setToolTip(entry.tooltip)
                  .setChecked((this.inventory.flags & 1 << entry.index) != 0);
               break;
            case TEXT:
               ((IconCheckBox)gui.addRenderableWidget(
                     entry.index, new IconCheckBox(x + entry.x, y + entry.y, 16, 16, (Component)entry.display, (this.inventory.flags & 1 << entry.index) == 0)
                  ))
                  .setItemLike(true)
                  .setListener(T -> this.toggleFlag(entry.index))
                  .setToolTip(entry.tooltip);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      for (BaseTileInfoProvider.SettingEntry entry : this.inventory.settings) {
         switch (entry.type) {
            case COLOR:
            default:
               break;
            case SIMPLE:
               ((ItemCheckBox)gui.getCastedButton(entry.index, ItemCheckBox.class)).setChecked((this.inventory.flags & 1 << entry.index) != 0);
               break;
            case TEXT:
               ((IconCheckBox)gui.getCastedButton(entry.index, IconCheckBox.class)).setChecked((this.inventory.flags & 1 << entry.index) != 0);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      return ((ImprovedTextWidget)this.gui.getCastedButton(-1, ImprovedTextWidget.class)).isFocused() && keyCode == 69;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.cards.name"), 8, 17, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.cards.show_text"), 8, 42, 4210752);
   }

   protected void updateName(String name) {
      this.inventory.name = name;
      IC2.NETWORKING.get(false).sendClientItemBuffer(this.inventory.getInventoryStack(), "customName", new StringDataBuffer(0, name));
   }

   protected void toggleFlag(int flag) {
      this.inventory.flags ^= 1 << flag;
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 1, flag);
   }

   @OnlyIn(Dist.CLIENT)
   protected void togglePaint(int flag, ColorButton button) {
      this.handleColorClick(button.getColor(), button, false, T -> {
         button.setColor(T);
         this.inventory.selectedColors[flag] = T.getId();
         IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 3 + flag, this.inventory.selectedColors[flag]);
      });
   }
}
