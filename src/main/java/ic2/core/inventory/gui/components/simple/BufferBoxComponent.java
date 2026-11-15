package ic2.core.inventory.gui.components.simple;

import ic2.core.IC2;
import ic2.core.block.machines.tiles.lv.MachineTankTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BufferBoxComponent extends GuiWidget {
   BufferBoxComponent.IBufferBox buffer;

   public BufferBoxComponent(BufferBoxComponent.IBufferBox buffer) {
      super(Box2i.EMPTY_BOX);
      this.buffer = buffer;
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
      int pos = this.buffer instanceof MachineTankTileEntity ? 129 : 123;
      gui.addRenderableWidget(
         0,
         new ItemCheckBox(x + pos, y + 20, 20, 20, T -> this.onButtonClick(T, 0), new ItemStack(Items.CLOCK), this.buffer.isTicking())
            .setToolTip("gui.ic2.buffer_box.tick")
      );
      gui.addRenderableWidget(
         1,
         new ItemCheckBox(x + pos, y + 45, 20, 20, T -> this.onButtonClick(T, 1), new ItemStack(Items.REDSTONE), this.buffer.isTickNotified())
            .setToolTip("gui.ic2.buffer_box.notify")
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((ItemCheckBox)gui.getCastedButton(0, ItemCheckBox.class)).setChecked(this.buffer.isTicking());
      ((ItemCheckBox)gui.getCastedButton(1, ItemCheckBox.class)).setChecked(this.buffer.isTickNotified());
   }

   @OnlyIn(Dist.CLIENT)
   public void onButtonClick(Button button, int index) {
      IC2.NETWORKING.get(false).sendClientTileEvent((BlockEntity)this.buffer, 1 + index, ((ItemCheckBox)button).isChecked() ? 0 : 1);
   }

   public interface IBufferBox {
      boolean isTicking();

      boolean isTickNotified();
   }
}
