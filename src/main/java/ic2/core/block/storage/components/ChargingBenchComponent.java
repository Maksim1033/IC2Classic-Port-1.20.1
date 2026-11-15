package ic2.core.block.storage.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.base.tiles.impls.BaseChargingBenchTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class ChargingBenchComponent extends GuiWidget {
   BaseChargingBenchTileEntity tile;
   boolean lastMode = false;

   public ChargingBenchComponent(BaseChargingBenchTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      if (this.tile.sortedMode != this.lastMode) {
         this.lastMode = this.tile.sortedMode;
         gui.setFlag(64);
      }

      ((ItemCheckBox)gui.getCastedButton(0, ItemCheckBox.class)).setChecked(this.tile.sortedMode);
      gui.getButton(1).visible = this.tile.sortedMode;
      gui.getButton(2).visible = this.tile.sortedMode;
      gui.getButton(3).visible = this.tile.sortedMode;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         0,
         new ItemCheckBox(x + 130, y + 45, 18, 18, T -> this.sendData(-1), new ItemStack(Items.HOPPER), this.tile.sortedMode).setToolTip("Toggle Sorted Mode")
      );
      ((IconButton)gui.addRenderableWidget(1, new IconButton(x + 130, y + 63, 18, 18, IC2Items.ICON_DISPLAY.create(3), T -> this.sendData(0))))
         .setToolTip("Insert into Bench")
         .visible = this.tile.sortedMode;
      ((IconButton)gui.addRenderableWidget(2, new IconButton(x + 130, y + 81, 18, 18, IC2Items.ICON_DISPLAY.create(5), T -> this.sendData(1))))
         .setToolTip("Swap Items")
         .visible = this.tile.sortedMode;
      ((IconButton)gui.addRenderableWidget(3, new IconButton(x + 130, y + 99, 18, 18, IC2Items.ICON_DISPLAY.create(4), T -> this.sendData(2))))
         .setToolTip("Extract from Bench")
         .visible = this.tile.sortedMode;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.battery_station.eu"), 150, 20, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.battery_station.etc"), 150, 30, 4210752);
      this.gui.drawRightString(matrix, this.translate("gui.ic2.battery_station.rem"), 25, 30, 4210752);
      float scale = this.tile.getMaxEU() >= 10000000 && this.tile.getStoredEU() >= 10000000 ? 0.5F : 1.0F;
      float invScale = 1.0F / scale;
      matrix.scale(scale, scale, scale);
      this.gui
         .drawRightString(
            matrix, this.string(Formatters.EU_FORMAT.format((long)this.tile.getStoredEU())), (int)(80.0F * invScale), (int)(20.0F * invScale), 5635925
         );
      this.gui.drawCenterString(matrix, this.string(" / "), (int)(88.0F * invScale), (int)(20.0F * invScale), 5635925);
      this.gui
         .drawString(matrix, this.string(Formatters.EU_FORMAT.format((long)this.tile.getMaxEU())), (int)(93.0F * invScale), (int)(20.0F * invScale), 5635925);
      matrix.scale(invScale, invScale, invScale);
      Entry missingTuple = this.tile.getMissingEnergy();
      int missing = missingTuple.getIntKey() + (this.tile.getMaxEU() - this.tile.getStoredEU());
      this.gui
         .drawString(
            matrix,
            missing >= 100000000
               ? this.translate("gui.ic2.battery_station.a_lot")
               : this.string(Formatters.EU_FORMAT.format((long)missing)).append(this.translate("gui.ic2.battery_station.eu")),
            35,
            30,
            5635925
         );
      if (missing <= 0) {
         this.gui.drawRightString(matrix, this.translate("gui.ic2.battery_station.done"), 145, 30, 5635925);
      } else {
         int average = Math.min(this.tile.averager.getAverage(), missingTuple.getIntKey() <= 0 ? 2147483647 : missingTuple.getIntValue());
         if (average > 0) {
            this.gui
               .drawRightString(
                  matrix, this.translate(DurationFormatUtils.formatDuration(Math.max(0L, (long)(missing / average) * 50L), "HH:mm:ss")), 145, 30, 5635925
               );
         } else {
            this.gui.drawRightString(matrix, this.translate("gui.ic2.battery_station.never"), 145, 30, 5635925);
         }
      }
   }

   protected void sendData(int index) {
      this.tile.sendToServer(index + 1, 0);
   }
}
