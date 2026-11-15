package ic2.core.block.machines.containers.hv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.tiles.hv.RocketMinerTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RocketMinerComponent extends GuiWidget {
   private static final int COLOR = ColorUtils.darker(ColorUtils.CYAN);
   private static final Box2i CELL_BOX = new Box2i(152, 15, 6, 36);
   private static final Box2i MINING_BOX = new Box2i(164, 15, 6, 36);
   RocketMinerTileEntity tile;

   public RocketMinerComponent(RocketMinerTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      ((ToolTipButton)gui.addRenderableWidget(0, new ToolTipButton(x + 145, y + 4, 10, 10, Component.literal("D"), T -> this.drainCells())))
         .setToolTip("gui.ic2.rocket_miner.cells.drain");
      ((ToolTipButton)gui.addRenderableWidget(1, new ToolTipButton(x + 162, y + 4, 10, 10, Component.literal("D"), T -> this.drainPipes())))
         .setToolTip("gui.ic2.rocket_miner.pipes.drain");
   }

   private void drainPipes() {
      this.tile.sendToServer(1, 0);
   }

   private void drainCells() {
      this.tile.sendToServer(2, 0);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = this.tile.storedCells > 0;
      gui.getButton(1).active = this.tile.storedMiningPipes > 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      float progress = (float)this.tile.storedCells / 1024.0F;
      this.gui.drawColoredRegion(matrix, (float)(x + 153), (float)(y + 16 + 34) - 34.0F * progress, 2.0F, 34.0F * progress, COLOR);
      progress = (float)this.tile.storedMiningPipes / 512.0F;
      this.gui.drawColoredRegion(matrix, (float)(x + 165), (float)(y + 16 + 34) - 34.0F * progress, 2.0F, 34.0F * progress, COLOR);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (CELL_BOX.isInBox(mouseX, mouseY)) {
         tooltips.accept(this.translate("gui.ic2.rocket_miner.cells", new Object[]{this.tile.storedCells}));
      }

      if (MINING_BOX.isInBox(mouseX, mouseY)) {
         tooltips.accept(this.translate("gui.ic2.rocket_miner.pipes", new Object[]{this.tile.storedMiningPipes}));
      }
   }
}
