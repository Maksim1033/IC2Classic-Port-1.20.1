package ic2.core.block.storage.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.base.tiles.impls.BaseBatteryStationTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class BatteryStationComponent extends GuiWidget {
   BaseBatteryStationTileEntity tile;

   public BatteryStationComponent(BaseBatteryStationTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.battery_station.eu"), 150, 20, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.battery_station.etc"), 150, 30, 4210752);
      this.gui.drawRightString(matrix, this.translate("gui.ic2.battery_station.left"), 25, 30, 4210752);
      float scale = this.tile.getMaxEU() >= 10000000 ? 0.5F : 1.0F;
      float invScale = 1.0F / scale;
      matrix.scale(scale, scale, scale);
      this.gui
         .drawRightString(
            matrix, this.string(Formatters.EU_FORMAT.format((long)this.tile.getStoredEU())), (int)(80.0F * invScale), (int)(20.0F * invScale), 5635925
         );
      this.gui.drawCenterString(matrix, this.string(" / "), (int)(88.0F * invScale), (int)(20.0F * invScale), 5635925);
      this.gui
         .drawString(matrix, this.string(Formatters.EU_FORMAT.format((long)this.tile.getMaxEU())), (int)(95.0F * invScale), (int)(20.0F * invScale), 5635925);
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
                  matrix, this.string(DurationFormatUtils.formatDuration(Math.max(0L, (long)(missing / average) * 50L), "HH:mm:ss")), 145, 30, 5635925
               );
         } else {
            this.gui.drawRightString(matrix, this.translate("gui.ic2.battery_station.never"), 145, 30, 5635925);
         }
      }
   }
}
