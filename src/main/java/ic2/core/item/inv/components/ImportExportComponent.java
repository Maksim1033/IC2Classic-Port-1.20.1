package ic2.core.item.inv.components;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.ScrollSlider;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.item.inv.inventory.ImportExportInventory;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ImportExportComponent extends GuiWidget {
   static final ItemStack[] DATA = createItemStack();
   static final Component[] NAMES = createNames();
   ImportExportInventory inventory;

   public ImportExportComponent(ImportExportInventory inventory) {
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
            6, new ItemCheckBox(x + 155, y + 35, 16, 16, T -> this.sendFlags(128), new ItemStack(Items.WATER_BUCKET), (this.inventory.flags & 128) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.fluid");
      ((ItemCheckBox)gui.addRenderableWidget(
            8, new ItemCheckBox(x + 138, y + 35, 16, 16, T -> this.sendFlags(256), new ItemStack(IC2Items.DRILL_DIAMOND), (this.inventory.flags & 256) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.durability");
      ((ItemCheckBox)gui.addRenderableWidget(
            9, new ItemCheckBox(x + 121, y + 35, 16, 16, T -> this.sendFlags(16), new ItemStack(Items.PAPER), (this.inventory.flags & 16) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.nbt");
      ((ItemCheckBox)gui.addRenderableWidget(
            10, new ItemCheckBox(x + 104, y + 35, 16, 16, T -> this.sendWhiteList(), new ItemStack(Items.BOOK), this.inventory.whiteList)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.whitelist");
      ((IconButton)gui.addRenderableWidget(11, new IconButton(x + 87, y + 35, 16, 16, DATA[Math.max(0, this.inventory.durability)], T -> this.sendDurability())))
         .setDurability(this.inventory.durability != 0)
         .setToolTip(NAMES[Math.max(0, this.inventory.durability)])
         .visible = (this.inventory.flags & 256) != 0;
      gui.addRenderableWidget(
         12,
         new ScrollSlider(
               x + 87,
               y + 52,
               84,
               16,
               this.string(),
               this.translate("tooltip.item.ic2.transport_update.items"),
               1.0,
               64.0,
               (double)this.inventory.transfer,
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

   private void sendDurability() {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 1, (this.inventory.durability + 1) % 11);
   }

   private void sendFlags(int flags) {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 2, flags);
   }

   private void sendWhiteList() {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 3, this.inventory.whiteList ? 0 : 1);
   }

   private void sendTransferSize(int value) {
      this.inventory.transfer = value;
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inventory.getInventoryStack(), 4, value);
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
      ((ItemCheckBox)gui.getCastedButton(6, ItemCheckBox.class)).setChecked((this.inventory.flags & 128) != 0);
      ((ItemCheckBox)gui.getCastedButton(8, ItemCheckBox.class)).setChecked((this.inventory.flags & 256) != 0);
      ((ItemCheckBox)gui.getCastedButton(9, ItemCheckBox.class)).setChecked((this.inventory.flags & 16) != 0);
      ((ItemCheckBox)gui.getCastedButton(10, ItemCheckBox.class)).setChecked(this.inventory.whiteList);
      ((IconButton)gui.getCastedButton(11, IconButton.class))
         .setDisplay(DATA[Math.max(0, this.inventory.durability)])
         .setDurability(this.inventory.durability != 0)
         .setToolTip(NAMES[Math.max(0, this.inventory.durability)])
         .visible = (this.inventory.flags & 256) != 0;
      ((ScrollSlider)gui.getCastedButton(12, ScrollSlider.class)).setValue((double)this.inventory.transfer);
   }

   static Component[] createNames() {
      return new Component[]{
         Component.translatable("tooltip.item.ic2.transport_update.durability.exact"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.1a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.1b"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.25a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.25b"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.50a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.50b"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.70a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.70b"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.90a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.90b")
      };
   }

   static ItemStack[] createItemStack() {
      double[] max = new double[]{1.0, 0.9, 0.9, 0.75, 0.75, 0.5, 0.5, 0.25, 0.25, 0.1, 0.0};
      ItemStack[] data = new ItemStack[max.length];

      for (int i = 0; i < max.length; i++) {
         ItemStack stack = new ItemStack(IC2Items.DRILL_DIAMOND);
         int maxEU = ((IElectricItem)stack.getItem()).getCapacity(stack);
         ElectricItem.MANAGER.charge(stack, (int)((double)maxEU * max[i]), 2147483647, true, false);
         data[i] = stack;
      }

      return data;
   }
}
