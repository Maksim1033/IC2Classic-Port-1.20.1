package ic2.core.block.storage.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.base.tiles.impls.BaseEnergyStorageTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyStorageComponent extends GuiWidget {
   public static final Component[] NAMES = new Component[]{
      Component.translatable("tooltip.block.ic2.energy_storage.nothing"),
      Component.translatable("tooltip.block.ic2.energy_storage.full"),
      Component.translatable("tooltip.block.ic2.energy_storage.part"),
      Component.translatable("tooltip.block.ic2.energy_storage.half_more"),
      Component.translatable("tooltip.block.ic2.energy_storage.half_less"),
      Component.translatable("tooltip.block.ic2.energy_storage.empty"),
      Component.translatable("tooltip.block.ic2.energy_storage.no_output"),
      Component.translatable("tooltip.block.ic2.energy_storage.no_output_unless_full")
   };
   protected BaseEnergyStorageTileEntity tile;
   int lastMode = -1;

   public EnergyStorageComponent(BaseEnergyStorageTileEntity block) {
      super(Box2i.EMPTY_BOX);
      this.tile = block;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
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

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int eu = this.tile.getStoredEU();
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      int offset = this.tile.getGuiOffset();
      this.gui.drawTextureRegion(matrix, (float)(x + 55 + offset), (float)(y + 16), 0.0F, 166.0F, 52.0F, 54.0F);
      if (eu > 0) {
         float per = Math.min(1.0F, (float)eu / (float)this.tile.getMaxEU());
         float lvl = per * 24.0F;
         if (lvl > 0.0F) {
            this.gui.drawTextureRegion(matrix, (float)(x + 79 + offset), (float)(y + 34), 176.0F, 14.0F, lvl, 16.0F);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      int offset = this.tile.getGuiOffset();
      this.gui.drawString(matrix, this.translate("gui.ic2.storage.power"), 85 + offset, 23, 4210752);
      int max = this.tile.getMaxEU();
      int eu = Math.min(this.tile.getStoredEU(), max);
      this.gui.drawString(matrix, this.translate("misc.ic2.eu", new Object[]{Formatters.EU_FORMAT.format((long)eu)}), 109 + offset, 35, 4210752);
      this.gui
         .drawString(
            matrix, this.string("/").append(this.translate("misc.ic2.eu", new Object[]{Formatters.EU_FORMAT.format((long)max)})), 109 + offset, 45, 4210752
         );
      this.gui
         .drawString(
            matrix, this.translate("gui.ic2.storage.output", new Object[]{Formatters.EU_FORMAT.format((long)this.tile.output)}), 85 + offset, 58, 4210752
         );
   }
}
