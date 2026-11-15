package ic2.core.block.machines.components.mv;

import ic2.core.IC2;
import ic2.core.block.machines.tiles.mv.SlowGrinderTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class SlowGrinderComponent extends GuiWidget {
   SlowGrinderTileEntity tile;

   public SlowGrinderComponent(SlowGrinderTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
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
      gui.addRenderableWidget(
         0,
         new ExtendedButton(
            x + 99,
            y + 60,
            50,
            12,
            this.translate("gui.ic2.slow_grinder.speed", new Object[]{this.tile.speed}),
            T -> IC2.NETWORKING.get(false).sendClientTileEvent(this.tile, 1, 0)
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).setMessage(this.translate("gui.ic2.slow_grinder.speed", new Object[]{this.tile.speed}));
   }
}
