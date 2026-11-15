package ic2.core.block.transport.fluid;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.util.DirectionList;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.transport.fluid.helper.InventoryData;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidPipeComponent extends GuiWidget {
   BaseTileEntity tile;
   InventoryData data;
   Predicate<Direction> pumping;

   public FluidPipeComponent(BaseTileEntity tile, InventoryData data, Predicate<Direction> pumping) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
      this.data = data;
      this.pumping = pumping;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, this.create(x + 7, y + 42, "D", Direction.DOWN));
      gui.addRenderableWidget(1, this.create(x + 18, y + 42, "U", Direction.UP));
      gui.addRenderableWidget(2, this.create(x + 29, y + 42, "N", Direction.NORTH));
      gui.addRenderableWidget(3, this.create(x + 40, y + 42, "S", Direction.SOUTH));
      gui.addRenderableWidget(4, this.create(x + 51, y + 42, "W", Direction.WEST));
      gui.addRenderableWidget(5, this.create(x + 62, y + 42, "E", Direction.EAST));
      ((ItemCheckBox)gui.addRenderableWidget(
            6, new ItemCheckBox(x + 73, y + 42, 14, 14, T -> this.sendWhiteList(), new ItemStack(Items.BOOK), this.data.isWhiteList())
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.whitelist");
      Component all = this.string()
         .append(this.translate("tooltip.ic2.press_key_description", new Object[]{this.translate("tooltip.ic2.ctrl.name"), "10x"}))
         .append(Character.toString('\n'))
         .append(this.translate("tooltip.ic2.press_key_description", new Object[]{this.translate("tooltip.ic2.shift.name"), "100x"}))
         .append(Character.toString('\n'))
         .append(this.translate("tooltip.ic2.press_key_description", new Object[]{this.translate("tooltip.ic2.alt.name"), "1000x"}));
      gui.addRenderableWidget(new ToolTipButton(x + 97, y + 50, 10, 10, this.string("-"), T -> this.sendPriority(-1)).setToolTip(all));
      gui.addRenderableWidget(new ToolTipButton(x + 159, y + 50, 10, 10, this.string("+"), T -> this.sendPriority(1)).setToolTip(all));
      ((ItemCheckBox)gui.addRenderableWidget(
            7, new ItemCheckBox(x + 152, y + 5, 16, 16, T -> this.sendEmit(), new ItemStack(IC2Items.FLUID_IMPORT_UPGRADE_SIMPLE), this.isPumping())
         ))
         .setToolTip("gui.ic2.extract.fluid");
      ((ItemCheckBox)gui.addRenderableWidget(8, new ItemCheckBox(x + 134, y + 5, 16, 16, T -> this.sendInput(), new ItemStack(Items.BOOK), this.isPumping())))
         .setToolTip("Input");
   }

   private boolean isPumping() {
      return this.pumping.test(Direction.from3DDataValue(this.data.getDir()));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      for (int i = 0; i < 6; i++) {
         gui.getButton(i).active = (this.data.getDirs() & 1 << i) != 0 && i != this.data.getDir();
      }

      ((ItemCheckBox)gui.getCastedButton(6, ItemCheckBox.class)).setChecked(this.data.isWhiteList());
      ((ItemCheckBox)gui.getCastedButton(7, ItemCheckBox.class)).setChecked(this.isPumping());
      ((ItemCheckBox)gui.getCastedButton(8, ItemCheckBox.class)).setChecked(this.data.isInput());
      gui.getButton(6).active = this.data.isInput();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.pipe.priority"), 133, 42, 4210752);
      this.gui.drawCenterString(matrix, this.string(this.data.getPriority() + ""), 133, 52, 4210752);
      RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
      Lighting.setupFor3DItems();
      RenderUtils.renderGuiItem(
         this.gui.getRenderItem(),
         matrix,
         new ItemStack(DirectionList.getNeighborState(this.tile, Direction.from3DDataValue(this.data.getDir())).getBlock()),
         7.0F,
         4.0F
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   private AbstractWidget create(int x, int y, String s, Direction dir) {
      AbstractWidget result = new ToolTipButton(x, y, 10, 10, this.string(s), T -> this.sendDirection(dir)).setToolTip(DirectionList.getName(dir));
      result.active = this.data.getDir() == dir.get3DDataValue() && (this.data.getDirs() & 1 << dir.get3DDataValue()) != 0;
      return result;
   }

   private void sendEmit() {
      this.tile.sendToServer(3, 0);
   }

   private void sendInput() {
      this.tile.sendToServer(4, this.data.isInput() ? 0 : 1);
   }

   private void sendDirection(Direction dir) {
      this.tile.sendToServer(0, dir.get3DDataValue());
   }

   private void sendWhiteList() {
      this.tile.sendToServer(2, this.data.isWhiteList() ? 0 : 1);
   }

   @OnlyIn(Dist.CLIENT)
   private void sendPriority(int value) {
      value *= Screen.hasControlDown() ? 10 : 1;
      value *= Screen.hasShiftDown() ? 100 : 1;
      value *= Screen.hasAltDown() ? 1000 : 1;
      this.tile.sendToServer(1, value);
   }
}
