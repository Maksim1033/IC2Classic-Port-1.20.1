package ic2.core.item.inv.components;

import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.ScrollSlider;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.item.inv.inventory.FluidImportExportInventory;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidImportExportComponent extends GuiWidget {
   FluidImportExportInventory inventory;

   public FluidImportExportComponent(FluidImportExportInventory inventory) {
      super(Box2i.EMPTY_BOX);
      this.inventory = inventory;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, this.create(x + 7, y + 35, "S", Direction.SOUTH));
      gui.addRenderableWidget(1, this.create(x + 21, y + 35, "U", Direction.UP));
      gui.addRenderableWidget(2, this.create(x + 7, y + 49, "W", Direction.WEST));
      gui.addRenderableWidget(3, this.create(x + 21, y + 49, "N", Direction.NORTH));
      gui.addRenderableWidget(4, this.create(x + 35, y + 49, "E", Direction.EAST));
      gui.addRenderableWidget(5, this.create(x + 21, y + 63, "D", Direction.DOWN));
      ((ItemCheckBox)gui.addRenderableWidget(
            10, new ItemCheckBox(x + 87, y + 35, 16, 16, T -> this.sendWhiteList(), new ItemStack(Items.BOOK), this.inventory.whitelist)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.whitelist");
      gui.addRenderableWidget(
         12,
         new ScrollSlider(
               x + 87,
               y + 52,
               84,
               16,
               this.string(),
               this.translate("mB/t"),
               1.0,
               1000.0,
               (double)this.inventory.transferRate,
               T -> this.sendTransferSize(T.getValueInt())
            )
            .setScrollEffect(1.0)
      );
   }

   @OnlyIn(Dist.CLIENT)
   private AbstractWidget create(int x, int y, String s, Direction dir) {
      AbstractWidget result = new ToolTipButton(x, y, 14, 14, this.string(s), T -> this.sendDirection(dir)).setToolTip(DirectionList.getName(dir));
      result.active = this.inventory.direction != dir.get3DDataValue();
      return result;
   }

   private void sendDirection(Direction dir) {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 0, dir.get3DDataValue());
   }

   private void sendWhiteList() {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 2, this.inventory.whitelist ? 0 : 1);
   }

   private void sendTransferSize(int value) {
      this.inventory.transferRate = value;
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 1, value);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = this.inventory.direction != Direction.SOUTH.get3DDataValue();
      gui.getButton(1).active = this.inventory.direction != Direction.UP.get3DDataValue();
      gui.getButton(2).active = this.inventory.direction != Direction.WEST.get3DDataValue();
      gui.getButton(3).active = this.inventory.direction != Direction.NORTH.get3DDataValue();
      gui.getButton(4).active = this.inventory.direction != Direction.EAST.get3DDataValue();
      gui.getButton(5).active = this.inventory.direction != Direction.DOWN.get3DDataValue();
      ((ItemCheckBox)gui.getCastedButton(10, ItemCheckBox.class)).setChecked(this.inventory.whitelist);
      ((ScrollSlider)gui.getCastedButton(12, ScrollSlider.class)).setValue((double)this.inventory.transferRate);
   }
}
