package ic2.core.block.machines.components.mv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.block.machines.tiles.mv.BaseTeleporterTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.inventory.gui.components.simple.SliderComponent;
import ic2.core.networking.buffers.data.BaseTeleporterBuffer;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class BaseTeleporterComponent extends GuiWidget {
   BaseTeleporterTileEntity tile;
   SliderComponent slider = this.addChild(new SliderComponent(new Box2i(154, 33, 11, 127), new Box2i(0, 166, 12, 15))).setNonEmptyRows(8);
   boolean open = false;

   public BaseTeleporterComponent(BaseTeleporterTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
   }

   @Override
   public boolean isMouseOver(int x, int y) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.clearFlag(1);
      int limit = this.tile.targetList.size();
      List<BaseTeleporterTileEntity.LocalTarget> list = this.tile.targetList.getCasted();
      boolean powered = this.tile.isActive();

      for (int i = 0; i < 8; i++) {
         int index = i;
         ExtendedButton button = new ExtendedButton(
            x + 15,
            y + i * 15 + 34,
            134,
            14,
            list.size() > i ? this.string(list.get(i).getName()) : this.translate("gui.ic2.base_teleporter.unknown"),
            T -> this.teleport(index)
         );
         gui.addRenderableWidget(i, button);
         button.visible = i < limit;
         button.active = powered;
      }

      gui.addRenderableWidget(
         100, new ToolTipButton(x + 154, y + 4, 12, 12, this.string("S"), T -> this.toggle(gui)).setToolTip("gui.ic2.base_teleporter.settings")
      );
      EditBox text = new EditBox(gui.getFont(), x - 93, y + 19, 90, 10, null, this.string());
      text.setCanLoseFocus(true);
      text.setBordered(false);
      text.setValue(this.tile.name);
      text.visible = this.open;
      gui.addRenderableWidget(101, text);
      text = new EditBox(gui.getFont(), x - 93, y + 47, 90, 10, null, this.string());
      text.setCanLoseFocus(true);
      text.setBordered(false);
      text.setValue(this.tile.networkID);
      text.visible = this.open;
      gui.addRenderableWidget(102, text);
      gui.addRenderableWidget(103, new ExtendedButton(x - 96, y + 60, 45, 14, this.translate("gui.ic2.base_teleporter.apply"), T -> this.apply(gui))).visible = this.open;
      gui.addRenderableWidget(104, new ExtendedButton(x - 49, y + 60, 45, 14, this.translate("gui.ic2.base_teleporter.cancel"), T -> this.cancel(gui))).visible = this.open;
   }

   @OnlyIn(Dist.CLIENT)
   public void apply(IC2Screen gui) {
      String name = ((EditBox)gui.getCastedButton(101, EditBox.class)).getValue();
      int flag = 0;
      if (!name.equalsIgnoreCase(this.tile.getCustomName().getString())) {
         flag = 2;
      }

      String network = ((EditBox)gui.getCastedButton(102, EditBox.class)).getValue();
      if (!network.equals(this.tile.networkID)) {
         flag++;
      }

      this.tile.sendToServer("", new BaseTeleporterBuffer(network, name, (byte)flag));
      this.toggle(gui);
   }

   @OnlyIn(Dist.CLIENT)
   public void cancel(IC2Screen gui) {
      ((EditBox)gui.getCastedButton(101, EditBox.class)).setValue(this.tile.name);
      ((EditBox)gui.getCastedButton(102, EditBox.class)).setValue(this.tile.networkID);
      this.toggle(gui);
   }

   @OnlyIn(Dist.CLIENT)
   public void toggle(IC2Screen gui) {
      this.open = !this.open;
      if (!this.open) {
         AbstractWidget widget = gui.getButton(101);
         if (widget.isFocused()) {
            widget.setFocused(false);
         }

         widget = gui.getButton(102);
         if (widget.isFocused()) {
            widget.setFocused(false);
         }
      } else {
         ((EditBox)gui.getCastedButton(101, EditBox.class)).setValue(this.tile.name);
         ((EditBox)gui.getCastedButton(102, EditBox.class)).setValue(this.tile.networkID);
      }

      gui.getButton(101).visible = this.open;
      gui.getButton(102).visible = this.open;
      gui.getButton(103).visible = this.open;
      gui.getButton(104).visible = this.open;
   }

   public void teleport(int index) {
      IC2.NETWORKING.get(false).sendClientTileEvent(this.tile, 1, this.slider.getCurrent() + index);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      return this.open
         && keyCode == 69
         && (((EditBox)this.gui.getCastedButton(101, EditBox.class)).isFocused() || ((EditBox)this.gui.getCastedButton(102, EditBox.class)).isFocused());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      int limit = this.tile.targetList.size();
      boolean powered = this.tile.isActive();
      List<BaseTeleporterTileEntity.LocalTarget> list = this.tile.targetList.getCasted();

      for (int i = 0; i < 8; i++) {
         AbstractWidget widget = gui.getButton(i);
         widget.visible = limit > this.slider.getCurrent() + i;
         widget.active = powered;
         widget.setMessage(widget.visible ? this.string(list.get(this.slider.getCurrent() + i).getName()) : this.translate("gui.ic2.base_teleporter.unknown"));
      }

      this.slider.setMax(limit);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      if (this.open) {
         this.gui.drawTextureRegion(matrix, (float)(this.gui.getGuiLeft() - 100), (float)this.gui.getGuiTop(), 24.0F, 166.0F, 100.0F, 79.0F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      if (this.open) {
         this.gui.drawCenterString(matrix, this.translate("gui.ic2.base_teleporter.name"), -50, 9, ColorUtils.DARK_GRAY);
         this.gui.drawCenterString(matrix, this.translate("gui.ic2.base_teleporter.network"), -50, 37, ColorUtils.DARK_GRAY);
      }

      this.gui.drawCenterString(matrix, this.string(this.tile.name + ":" + this.tile.networkID), 85, 20, ColorUtils.DARK_GRAY);
   }
}
