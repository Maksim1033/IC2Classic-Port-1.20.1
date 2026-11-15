package ic2.core.block.machines.components.mv.planner;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.logic.planner.newLogic.ReactorPrediction;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.Formatters;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StatisticsTabComponent extends BaseInfoTabComponent {
   public StatisticsTabComponent(ReactorPlannerTileEntity tile) {
      super(118, tile, Component.translatable("gui.ic2.reactor_planner.statistics"), new ItemStack(IC2Items.COAL_BALL), 1);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   protected void drawSideTab(PoseStack matrix, int xOffset, int yOffset, ItemStack item, boolean enabled) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      this.gui.drawTextureRegion(matrix, (float)(x + xOffset), (float)(y + yOffset), enabled ? 122.0F : 156.0F, 212.0F, enabled ? 31.0F : 28.0F, 28.0F);
      this.gui.drawTextureRegion(matrix, (float)(x + xOffset + 9), (float)(y + yOffset + 7), 90.0F, 228.0F, 14.0F, 13.0F);
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      super.addRequests(requests);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      if (this.isSelected()) {
         ReactorPrediction prediction = this.tile.prediction;
         boolean cooling = prediction.coolingPerTick > prediction.heatPerTick;
         if (this.tile.isSteamReactor()) {
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.statistics.expected.heat", new Object[]{Formatters.EU_FORMAT.format((long)prediction.heatPerTick)}),
                  8,
                  133,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.statistics.expected.vent", new Object[]{Formatters.EU_FORMAT.format((long)prediction.coolingPerTick)}),
                  8,
                  143,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate(
                     "gui.ic2.reactor_planner.statistics.expected.reactor", new Object[]{Formatters.EU_FORMAT.format((long)prediction.reactorCoolingPerTick)}
                  ),
                  8,
                  153,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate(
                     "gui.ic2.reactor_planner.statistics.excess." + (cooling ? "cooling" : "heating"),
                     new Object[]{Formatters.EU_FORMAT.format((long)Math.abs(prediction.coolingPerTick - prediction.heatPerTick))}
                  ),
                  8,
                  163,
                  4210752
               );
         } else {
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.statistics.expected.heat", new Object[]{Formatters.EU_FORMAT.format((long)prediction.heatPerTick)}),
                  8,
                  133,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate(
                     "gui.ic2.reactor_planner.statistics.expected.pack", new Object[]{Formatters.EU_FORMAT.format((long)prediction.heatPackHeatPerTick)}
                  ),
                  8,
                  143,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.statistics.expected.vent", new Object[]{Formatters.EU_FORMAT.format((long)prediction.coolingPerTick)}),
                  8,
                  153,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate(
                     "gui.ic2.reactor_planner.statistics.expected.reactor", new Object[]{Formatters.EU_FORMAT.format((long)prediction.reactorCoolingPerTick)}
                  ),
                  8,
                  163,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate(
                     "gui.ic2.reactor_planner.statistics.excess." + (cooling ? "cooling" : "heating"),
                     new Object[]{Formatters.EU_FORMAT.format((long)Math.abs(prediction.coolingPerTick - prediction.heatPerTick))}
                  ),
                  8,
                  173,
                  4210752
               );
         }
      }
   }
}
