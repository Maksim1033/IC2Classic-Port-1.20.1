package ic2.core.block.machines.components.lv;

import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MinerComponent extends GuiWidget {
   MinerTileEntity tile;

   public MinerComponent(MinerTileEntity tile) {
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
      ((IconButton)gui.addRenderableWidget(
            0, new IconButton(gui.getGuiLeft() + 151, gui.getGuiTop() + 5, 20, 20, new ItemStack(IC2Blocks.MINER), T -> this.tile.sendToServer(0, 0))
         ))
         .setToolTip("gui.ic2.miner.workgroup");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = !this.tile.isSlave();
   }
}
