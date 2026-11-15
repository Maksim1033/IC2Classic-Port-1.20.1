package ic2.core.block.machines.components.hv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.tiles.hv.OreScannerTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class ScannerComponent extends GuiWidget {
   OreScannerTileEntity tile;

   public ScannerComponent(OreScannerTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, new ExtendedButton(x + 127, y + 30, 40, 11, this.translate("gui.ic2.scanner.scan"), T -> this.start()));
      gui.addRenderableWidget(1, new ExtendedButton(x + 8, y + 119, 10, 10, this.string("+"), T -> this.move(1)));
      gui.addRenderableWidget(1, new ExtendedButton(x + 157, y + 119, 10, 10, this.string("-"), T -> this.move(-1)));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = this.tile.state != 1;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      for (int i = 0; i < 5; i++) {
         ItemStack stack = this.tile.results.getStackInSlot(i);
         if (!stack.isEmpty()) {
            this.gui.drawString(matrix, this.string(StackUtil.getNbtData(stack).getInt("ore_scanner_count") + "x"), 30, 35 + 18 * i, ColorUtils.GRAY);
         }
      }

      this.gui
         .drawCenterString(
            matrix,
            this.translate(
               "gui.ic2.scanner.scan.progress",
               new Object[]{Formatters.EU_FORMAT.format((long)this.tile.blocks), Formatters.EU_FORMAT.format((long)this.tile.maxBlocks)}
            ),
            90,
            120,
            ColorUtils.GRAY
         );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      this.move(-scroll);
      return true;
   }

   protected void start() {
      this.tile.sendToServer(0, 0);
   }

   protected void move(int amount) {
      this.tile.sendToServer(1, amount);
   }
}
