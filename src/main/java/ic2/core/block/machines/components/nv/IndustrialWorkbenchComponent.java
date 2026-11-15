package ic2.core.block.machines.components.nv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.containers.nv.IndustrialWorkbenchContainer;
import ic2.core.block.machines.tiles.nv.IndustrialWorkbenchTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.slot.MemorySlot;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class IndustrialWorkbenchComponent extends GuiWidget {
   IndustrialWorkbenchTileEntity tile;

   public IndustrialWorkbenchComponent(IndustrialWorkbenchTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
   }

   @Override
   public boolean isMouseOver(int x, int y) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         0, new ExtendedButton(x + 97, y + 16, 40, 10, this.translate("gui.ic2.industrial_workbench.save"), T -> this.tile.sendToServer(2, 0))
      );
      gui.addRenderableWidget(1, new ExtendedButton(x + 84, y + 16, 10, 10, this.string("X"), T -> this.tile.sendToServer(2, 1)));
      ((ItemCheckBox)gui.addRenderableWidget(
            2, new ItemCheckBox(x + 152, y + 54, 16, 16, T -> this.flipRecipeFlag(16), new ItemStack(Items.PAPER), (this.tile.currentRecipe.flags & 16) != 0)
         ))
         .setToolTip("gui.ic2.industrial_workbench.nbt");
      ((ItemCheckBox)gui.addRenderableWidget(
            5, new ItemCheckBox(x + 8, y + 35, 16, 16, T -> this.flipFlag(2), new ItemStack(Items.HOPPER), (this.tile.flags & 2) != 0)
         ))
         .setToolTip("gui.ic2.industrial_workbench.restock");
      ((ItemCheckBox)gui.addRenderableWidget(
            6, new ItemCheckBox(x + 8, y + 53, 16, 16, T -> this.flipFlag(1), new ItemStack(Items.CRAFTING_TABLE), (this.tile.flags & 1) != 0)
         ))
         .setToolTip("gui.ic2.industrial_workbench.craft");
      ((IconButton)gui.addRenderableWidget(new IconButton(x + 98, y + 53, 16, 16, new ItemStack(Items.HOPPER_MINECART), T -> this.tile.sendToServer(6, 0))))
         .setToolTip("gui.ic2.industrial_workbench.tube_net");
   }

   public void flipRecipeFlag(int flag) {
      this.tile.currentRecipe.flags ^= flag;
      this.tile.sendToServer(0, this.tile.currentRecipe.flags);
   }

   public void flipFlag(int flag) {
      this.tile.flags ^= flag;
      this.tile.sendToServer(1, this.tile.flags);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((ItemCheckBox)gui.getCastedButton(2, ItemCheckBox.class)).setChecked((this.tile.currentRecipe.flags & 16) != 0);
      ((ItemCheckBox)gui.getCastedButton(5, ItemCheckBox.class)).setChecked((this.tile.flags & 2) != 0);
      ((ItemCheckBox)gui.getCastedButton(6, ItemCheckBox.class)).setChecked((this.tile.flags & 1) != 0);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      for (Slot slot : this.gui.getCastedContainer(IndustrialWorkbenchContainer.class).slots) {
         if (slot instanceof MemorySlot && !slot.isActive()) {
            this.gui.makeSlotFunction(matrix, slot);
         }
      }
   }
}
