package ic2.core.block.storage.components;

import ic2.core.block.base.tiles.impls.BaseElectricUnloaderTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UnloaderComponent extends GuiWidget {
   public static final Component[] NAMES = new Component[]{
      Component.translatable("tooltip.block.ic2.unloader.nothing"),
      Component.translatable("tooltip.block.ic2.unloader.empty"),
      Component.translatable("tooltip.block.ic2.unloader.transferring"),
      Component.translatable("tooltip.block.ic2.unloader.half_more"),
      Component.translatable("tooltip.block.ic2.unloader.half_less"),
      Component.translatable("tooltip.block.ic2.unloader.output_if_full")
   };
   BaseElectricUnloaderTileEntity tile;
   int lastMode;

   public UnloaderComponent(BaseElectricUnloaderTileEntity tile) {
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
      this.lastMode = this.tile.redstoneMode;
      ((IconButton)gui.addRenderableWidget(
            0, new IconButton(gui.getGuiLeft() + 152, gui.getGuiTop() + 4, 20, 20, new ItemStack(Items.REDSTONE), T -> this.onClick())
         ))
         .setToolTip(NAMES[this.lastMode]);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      if (this.lastMode != this.tile.redstoneMode) {
         this.lastMode = this.tile.redstoneMode;
         ((IconButton)gui.getCastedButton(0, IconButton.class)).setToolTip(NAMES[this.lastMode]);
      }
   }

   private void onClick() {
      this.tile.sendToServer(0, 0);
   }
}
