package ic2.core.item.inv.components;

import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.item.inv.inventory.CraftingUpgradeInventory;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class CraftingUpgradeComponent extends GuiWidget {
   CraftingUpgradeInventory inv;

   public CraftingUpgradeComponent(CraftingUpgradeInventory inv) {
      super(Box2i.EMPTY_BOX);
      this.inv = inv;
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
      gui.addRenderableWidget(new ExtendedButton(x + 100, y + 15, 60, 12, this.translate("gui.ic2.crafting_upgrade.validate"), T -> this.validateRecipe()));
      String s = "DUNSWE";
      int i = 0;

      for (int m = s.length(); i < m; i++) {
         gui.addRenderableWidget(i, this.create(x + 3 + i % 2 * 13, y + 16 + i / 2 * 13, Character.toString(s.charAt(i)), Direction.from3DDataValue(i)));
      }

      ((ItemCheckBox)gui.addRenderableWidget(
            6, new ItemCheckBox(x + 152, y + 60, 16, 16, T -> this.sendFlags(128), new ItemStack(Items.WATER_BUCKET), (this.inv.flags & 128) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.fluid");
      ((ItemCheckBox)gui.addRenderableWidget(
            7, new ItemCheckBox(x + 135, y + 60, 16, 16, T -> this.sendFlags(32), new ItemStack(Items.WRITABLE_BOOK), (this.inv.flags & 32) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.tag");
      ((ItemCheckBox)gui.addRenderableWidget(
            8, new ItemCheckBox(x + 118, y + 60, 16, 16, T -> this.sendFlags(256), new ItemStack(IC2Items.DRILL_DIAMOND), (this.inv.flags & 256) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.durability");
      ((ItemCheckBox)gui.addRenderableWidget(
            9, new ItemCheckBox(x + 101, y + 60, 16, 16, T -> this.sendFlags(16), new ItemStack(Items.PAPER), (this.inv.flags & 16) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.nbt");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      for (int i = 0; i < 6; i++) {
         gui.getButton(i).active = i != this.inv.direction;
      }

      ((ItemCheckBox)gui.getCastedButton(6, ItemCheckBox.class)).setChecked((this.inv.flags & 128) != 0);
      ((ItemCheckBox)gui.getCastedButton(7, ItemCheckBox.class)).setChecked((this.inv.flags & 32) != 0);
      ((ItemCheckBox)gui.getCastedButton(8, ItemCheckBox.class)).setChecked((this.inv.flags & 256) != 0);
      ((ItemCheckBox)gui.getCastedButton(9, ItemCheckBox.class)).setChecked((this.inv.flags & 16) != 0);
   }

   @OnlyIn(Dist.CLIENT)
   private AbstractWidget create(int x, int y, String s, Direction dir) {
      AbstractWidget result = new ToolTipButton(x, y, 12, 12, this.string(s), T -> this.sendDirection(dir)).setToolTip(DirectionList.getName(dir));
      result.active = this.inv.direction != dir.get3DDataValue();
      return result;
   }

   private void validateRecipe() {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inv.getInventoryStack(), 0, 0);
   }

   private void sendFlags(int flag) {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inv.getInventoryStack(), 1, flag);
   }

   private void sendDirection(Direction dir) {
      IC2.NETWORKING.get(false).sendClientItemEvent(this.inv.getInventoryStack(), 2, dir.get3DDataValue());
   }
}
