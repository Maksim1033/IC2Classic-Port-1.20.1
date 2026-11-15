package ic2.core.block.machines.components.nv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.tiles.nv.UUMatterExpansionTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UUExpansionComponent extends GuiWidget {
   UUMatterExpansionTileEntity tile;

   public UUExpansionComponent(UUMatterExpansionTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      ((IconButton)gui.addRenderableWidget(0, new IconButton(x + 162, y + 15, 10, 10, new ItemStack(IC2Items.UUMATTER), T -> this.onClick())))
         .setToolTip("gui.ic2.uu_expansion.take");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = this.tile.uuMatter >= 1000;
   }

   @OnlyIn(Dist.CLIENT)
   public void onClick() {
      this.tile.sendToServer(1, Screen.hasShiftDown() ? (Screen.hasControlDown() ? -1 : 10) : (Screen.hasControlDown() ? 64 : 1));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui
         .drawString(
            matrix,
            this.translate(
               "gui.ic2.uu_expansion.stored",
               new Object[]{
                  Formatters.EU_READER_FORMAT.format((double)((float)this.tile.uuMatter / 1000.0F)),
                  Formatters.EU_READER_FORMAT.format((double)((float)this.tile.maxUUMatter / 1000.0F))
               }
            ),
            45,
            18,
            4210752
         );
   }
}
