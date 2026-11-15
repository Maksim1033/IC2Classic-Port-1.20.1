package ic2.core.block.transport.item.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.util.DirectionList;
import ic2.core.block.transport.item.tubes.RoundRobinTubeTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class RoundRobinComponent extends GuiWidget {
   public static final int[] DIRECTION_COLORS = new int[]{65535, 16711680, 16776960, 255, 16711935, 65280};
   RoundRobinTubeTileEntity tile;

   public RoundRobinComponent(RoundRobinTubeTileEntity tile) {
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

      for (int i = 0; i < 6; i++) {
         int index = i;
         gui.addRenderableWidget(i * 2, new ExtendedButton(x + 20, y + 20 + i * 13, 12, 12, this.string("-"), T -> this.remove(index))).active = this.tile.cap[i]
            > 0;
         gui.addRenderableWidget(i * 2 + 1, new ExtendedButton(x + 108, y + 20 + i * 13, 12, 12, this.string("+"), T -> this.add(index))).active = this.tile.cap[i]
            < 1000;
      }
   }

   private void add(int index) {
      this.tile.sendToServer(Screen.hasShiftDown() ? 2 : 0, index);
   }

   private void remove(int index) {
      this.tile.sendToServer(Screen.hasShiftDown() ? 3 : 1, index);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      for (int i = 0; i < 6; i++) {
         gui.getButton(i * 2).active = this.tile.cap[i] > 0;
         gui.getButton(i * 2 + 1).active = this.tile.cap[i] < 1000;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      for (int i = 0; i < 6; i++) {
         this.gui
            .drawCenterString(matrix, DirectionList.getName(Direction.from3DDataValue(i)).append(": " + this.tile.cap[i]), 72, 22 + i * 13, DIRECTION_COLORS[i]);
      }
   }
}
