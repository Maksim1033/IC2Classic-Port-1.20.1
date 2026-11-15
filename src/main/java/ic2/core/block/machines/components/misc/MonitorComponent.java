package ic2.core.block.machines.components.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.core.block.cables.mointor.MonitorTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ColorButton;
import ic2.core.inventory.gui.components.base.IconCheckBox;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class MonitorComponent extends GuiWidget {
   MonitorTileEntity tile;

   public MonitorComponent(MonitorTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         0,
         new IconCheckBox(x + 120, y + 58, 49, 14, this.translate("gui.ic2.monitor.lock"), this.tile.isLocked())
            .setListener(T -> this.toggleLock())
            .setItemLike(true)
            .disableScale(true)
            .setToolTip("gui.ic2.monitor.lock.desc")
      );
      gui.addRenderableWidget(
            1, new ExtendedButton(x + 7, y + 58, 35, 14, this.translate("gui.ic2.monitor.left"), T -> this.toggleAlignment(IDisplayInfo.Alignment.LEFT))
         )
         .active = this.tile.align != IDisplayInfo.Alignment.LEFT;
      gui.addRenderableWidget(
            2, new ExtendedButton(x + 43, y + 58, 40, 14, this.translate("gui.ic2.monitor.center"), T -> this.toggleAlignment(IDisplayInfo.Alignment.CENTER))
         )
         .active = this.tile.align != IDisplayInfo.Alignment.CENTER;
      gui.addRenderableWidget(
            3, new ExtendedButton(x + 84, y + 58, 35, 14, this.translate("gui.ic2.monitor.right"), T -> this.toggleAlignment(IDisplayInfo.Alignment.RIGHT))
         )
         .active = this.tile.align != IDisplayInfo.Alignment.RIGHT;
      ((ToolTipButton)gui.addRenderableWidget(4, new ToolTipButton(x + 107, y + 36, 12, 12, this.string("-"), T -> this.changeScale(-1.0F))))
         .setToolTip("gui.ic2.monitor.modifiers");
      ((ToolTipButton)gui.addRenderableWidget(5, new ToolTipButton(x + 152, y + 36, 12, 12, this.string("+"), T -> this.changeScale(1.0F))))
         .setToolTip("gui.ic2.monitor.modifiers");
      gui.addRenderableWidget(6, new ColorButton(x + 7, y + 37, 16, 16, this.tile.getScreenColor(), this::setColor));
      ((ItemCheckBox)gui.addRenderableWidget(
            7, new ItemCheckBox(x + 153, y + 5, 16, 16, T -> this.tile.sendToServer(4, 0), new ItemStack(Items.REDSTONE_TORCH), this.tile.requireRedstone)
         ))
         .setToolTip("gui.ic2.monitor.require");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((IconCheckBox)gui.getCastedButton(0, IconCheckBox.class)).setChecked(this.tile.isLocked());
      gui.getButton(1).active = this.tile.align != IDisplayInfo.Alignment.LEFT;
      gui.getButton(2).active = this.tile.align != IDisplayInfo.Alignment.CENTER;
      gui.getButton(3).active = this.tile.align != IDisplayInfo.Alignment.RIGHT;
      gui.getButton(4).active = this.tile.scale > 1.0F;
      gui.getButton(5).active = this.tile.scale < 10.0F;
      ((ColorButton)gui.getCastedButton(6, ColorButton.class)).setColor(this.tile.getScreenColor());
      ((ItemCheckBox)gui.getCastedButton(7, ItemCheckBox.class)).setChecked(this.tile.requireRedstone);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.monitor.scale"), 136, 24, 4210752);
      this.gui.drawCenterString(matrix, this.string(Formatters.F_POINT_2.format((double)this.tile.scale) + "x"), 136, 38, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   private void setColor(ColorButton button) {
      this.handleColorClick(button.getColor(), button, false, T -> {
         this.tile.setScreenColor(T);
         this.tile.sendToServer(3, T.getId());
      });
   }

   private void toggleLock() {
      this.tile.sendToServer(0, 0);
   }

   private void toggleAlignment(IDisplayInfo.Alignment align) {
      this.tile.align = align;
      this.tile.sendToServer(1, align.ordinal());
   }

   @OnlyIn(Dist.CLIENT)
   private void changeScale(float scale) {
      if (Screen.hasShiftDown()) {
         scale *= 0.1F;
      } else if (Screen.hasControlDown()) {
         scale *= 0.01F;
      }

      this.tile.sendToServer(2, Float.floatToIntBits(scale));
      this.tile.scale = Mth.clamp(this.tile.scale + scale, 1.0F, 10.0F);
   }
}
