package ic2.core.block.machines.components.mv;

import ic2.core.IC2;
import ic2.core.block.machines.tiles.mv.InductionFurnaceTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InductionFurnaceComponent extends GuiWidget {
   InductionFurnaceTileEntity tile;

   public InductionFurnaceComponent(InductionFurnaceTileEntity tile) {
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
         new ItemCheckBox(x + 152, y + 64, 16, 16, T -> this.onToggle(), new ItemStack(Items.HOPPER), this.tile.split)
            .setToolTip("gui.ic2.induction_furnace.split")
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((ItemCheckBox)gui.getCastedButton(0, ItemCheckBox.class)).setChecked(this.tile.split);
   }

   public void onToggle() {
      IC2.NETWORKING.get(false).sendClientTileEvent(this.tile, 1, this.tile.split ? 0 : 1);
      this.tile.split = !this.tile.split;
   }
}
