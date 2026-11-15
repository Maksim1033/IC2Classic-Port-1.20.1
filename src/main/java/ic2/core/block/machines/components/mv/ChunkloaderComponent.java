package ic2.core.block.machines.components.mv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.block.machines.tiles.mv.ChunkloaderTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.utils.math.MathUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChunkloaderComponent extends GuiWidget {
   static final Component[] COMPONENTS = new Component[]{
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.public")}),
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.protected")}),
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.private")})
   };
   static final ItemStack[] COMPONENT_ITEMS = new ItemStack[]{new ItemStack(Items.GOLDEN_SWORD), new ItemStack(Items.BOW), new ItemStack(Items.SHIELD)};
   private ChunkloaderTileEntity entity;
   int lastRadius;
   int lastMode;
   boolean lastProcessing;

   public ChunkloaderComponent(ChunkloaderTileEntity entity) {
      super(Box2i.EMPTY_BOX);
      this.entity = entity;
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
      gui.addRenderableWidget(0, new ToolTipButton(gui.getGuiLeft() + 28, gui.getGuiTop() + 40, 16, 16, this.string("-"), T -> this.modifyRadius(-1)));
      gui.addRenderableWidget(1, new ToolTipButton(gui.getGuiLeft() + 75, gui.getGuiTop() + 40, 16, 16, this.string("+"), T -> this.modifyRadius(1)));
      gui.addRenderableWidget(
         2,
         new ToolTipButton(
            gui.getGuiLeft() + 5,
            gui.getGuiTop() + 79,
            110,
            14,
            this.translate(this.entity.doesChunkProcessing ? "gui.ic2.chunkloader.tick.yes" : "gui.ic2.chunkloader.tick.no"),
            T -> this.modifyProcessing()
         )
      );
      gui.addRenderableWidget(
         3, new ToolTipButton(gui.getGuiLeft() + 5, gui.getGuiTop() + 94, 110, 14, this.translate("gui.ic2.chunkloader.confirm"), T -> this.validate())
      );
      gui.addRenderableWidget(
         4,
         new IconButton(x + 106, y - 16, 16, 16, COMPONENT_ITEMS[this.lastMode], T -> this.entity.sendToServer(1, 0)).setToolTip(COMPONENTS[this.entity.mode])
      );
      this.lastRadius = this.entity.radius;
      this.lastProcessing = this.entity.doesChunkProcessing;
      this.lastMode = this.entity.mode;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(2).setMessage(this.translate(this.entity.doesChunkProcessing ? "gui.ic2.chunkloader.tick.yes" : "gui.ic2.chunkloader.tick.no"));
      gui.getButton(3).active = this.lastProcessing != this.entity.doesChunkProcessing || this.lastRadius != this.entity.radius;
   }

   private void modifyRadius(int change) {
      this.entity.radius = Mth.clamp(this.entity.radius + change, 0, ChunkloaderTileEntity.POWER_COST.length - 1);
   }

   private void modifyProcessing() {
      this.entity.doesChunkProcessing = !this.entity.doesChunkProcessing;
   }

   public void onSync() {
      this.lastRadius = this.entity.radius;
      this.lastProcessing = this.entity.doesChunkProcessing;
   }

   private void validate() {
      IC2.NETWORKING.get(false).sendClientTileEvent(this.entity, 0, MathUtils.putInt(this.entity.radius, this.entity.doesChunkProcessing ? 1 : 0));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      int value = this.entity.radius + this.entity.radius + 1;
      this.gui.drawCenterString(matrix, this.string(Integer.toString(this.entity.getRadius())), 60, 41 + 9 / 2, 4210752);
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.chunkloader.loaded_chunks", new Object[]{value * value}), 60, 60, 4210752);
      this.gui
         .drawCenterString(
            matrix,
            this.translate(
               "gui.ic2.chunkloader.cost", new Object[]{ChunkloaderTileEntity.POWER_COST[this.entity.radius] * (this.entity.doesChunkProcessing ? 2 : 1)}
            ),
            60,
            70,
            4210752
         );
   }
}
