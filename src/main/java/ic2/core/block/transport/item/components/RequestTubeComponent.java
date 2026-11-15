package ic2.core.block.transport.item.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.components.mv.planner.NamingTabComponent;
import ic2.core.block.transport.item.tubes.RequestTubeTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ColorButton;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.platform.player.KeyHelper;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class RequestTubeComponent extends GuiWidget {
   RequestTubeTileEntity tile;
   IHasInventory inv;
   int lastAmount;
   DyeColor lastColor;

   public RequestTubeComponent(RequestTubeTileEntity tile, IHasInventory inv) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
      this.inv = inv;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         new ExtendedButton(x + 80, y + 79, 34, 14, this.translate("gui.ic2.filter_extraction_tube.save"), T -> this.tile.sendToServer(0, 0))
      );
      ((ItemCheckBox)gui.addRenderableWidget(
            0, new ItemCheckBox(x + 152, y + 78, 16, 16, T -> this.tile.sendToServer(1, 0), new ItemStack(Items.PAPER), this.tile.nbt)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.nbt");
      ((ItemCheckBox)gui.addRenderableWidget(
            2, new ItemCheckBox(x + 152, y + 60, 16, 16, T -> this.tile.sendToServer(3, 0), new ItemStack(Items.BARRIER), this.tile.dump)
         ))
         .setToolTip("gui.ic2.request_tube.accept_anything");
      ((ColorButton)gui.addRenderableWidget(3, new ColorButton(x + 134, y + 78, 16, 16, this.tile.color, this::handleColor)))
         .setToolTip(
            "gui.ic2.industrial_workbench.request_color", new Object[]{this.tile.color == null ? this.translate("gui.ic2.none") : this.tile.color.getName()}
         );
      ((ToolTipButton)gui.addRenderableWidget(
            new ToolTipButton(x + 8, y + 60, 34, 14, this.translate("gui.ic2.request_tube.reset"), T -> this.tile.sendToServer(8, 0))
         ))
         .setToolTip("gui.ic2.request_tube.reset_requests");
      ImprovedTextWidget widget = gui.addRenderableWidget(4, new ImprovedTextWidget(x + 81, y + 64, 32, 14));
      widget.setMaxLength(4);
      widget.setFilter(NamingTabComponent.NUMBERS_ONLY);
      widget.setValue(Integer.toString(this.tile.amountRequested));
      widget.setBordered(false);
      widget.setCanLoseFocus(true);
      widget.setResponder(this::setAmount);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((ItemCheckBox)gui.getCastedButton(0, ItemCheckBox.class)).setChecked(this.tile.nbt);
      ((ItemCheckBox)gui.getCastedButton(2, ItemCheckBox.class)).setChecked(this.tile.dump);
      if (this.tile.color != this.lastColor) {
         this.lastColor = this.tile.color;
         ((ColorButton)gui.getCastedButton(3, ColorButton.class))
            .setColor(this.tile.color)
            .setToolTip(
               "gui.ic2.industrial_workbench.request_color", new Object[]{this.tile.color == null ? this.translate("gui.ic2.none") : this.tile.color.getName()}
            );
      }

      int size = this.tile.filters.size();

      for (int i = 0; i < 18; i++) {
         this.inv.setStackInSlot(i, size > i ? ((RequestTubeTileEntity.RequestEntry)this.tile.filters.get(i)).getStack() : ItemStack.EMPTY);
      }

      ImprovedTextWidget text = gui.getCastedButton(4, ImprovedTextWidget.class);
      text.tick();
      if (this.lastAmount != this.tile.amountRequested) {
         this.lastAmount = this.tile.amountRequested;
         text.setExternalValue(Integer.toString(this.tile.amountRequested));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.industrial_workbench.request"), 97, 54, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (mouseX >= 7 && mouseY >= 17) {
         int xIndex = (mouseX - 7) / 18;
         int yIndex = (mouseY - 17) / 18;
         if (xIndex <= 8 && yIndex < 2) {
            int index = yIndex * 9 + xIndex;
            if (this.tile.filters.size() > index) {
               RequestTubeTileEntity.RequestEntry entry = (RequestTubeTileEntity.RequestEntry)this.tile.filters.get(index);
               tooltips.accept(entry.getStack().getHoverName());
               tooltips.accept(this.buildKeyDescription(KeyHelper.SNEAK_KEY, "gui.ic2.color_tube.to_delete", new Object[0]).withStyle(ChatFormatting.GRAY));
               tooltips.accept(this.translate("gui.ic2.request_tube.requested", new Object[]{entry.getAmount()}));
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (mouseX >= 7 && mouseY >= 17) {
         int xIndex = (mouseX - 7) / 18;
         int yIndex = (mouseY - 17) / 18;
         if (xIndex <= 8 && yIndex < 2) {
            this.selectIndex(yIndex * 9 + xIndex);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void setAmount(String value) {
      try {
         this.tile.sendToServer(4, Integer.parseInt(value));
      } catch (Exception var3) {
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void handleColor(ColorButton button) {
      this.handleColorClick(button.getColor(), button, true, T -> {
         this.tile.color = T;
         this.tile.sendToServer(7, T == null ? 0 : T.getId() + 1);
      });
   }

   @OnlyIn(Dist.CLIENT)
   private void selectIndex(int index) {
      if (index != -1 && Screen.hasShiftDown()) {
         this.tile.sendToServer(6, index);
      } else {
         this.tile.sendToServer(5, index);
      }
   }
}
