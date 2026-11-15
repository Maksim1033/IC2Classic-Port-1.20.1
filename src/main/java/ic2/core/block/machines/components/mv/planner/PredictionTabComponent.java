package ic2.core.block.machines.components.mv.planner;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.logic.planner.newLogic.ReactorPrediction;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.SanityHelper;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PredictionTabComponent extends BaseInfoTabComponent {
   public PredictionTabComponent(ReactorPlannerTileEntity tile) {
      super(148, tile, Component.translatable("gui.ic2.reactor_planner.prediction"), new ItemStack(Items.ENDER_EYE), 2);
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
         int base;
         if (this.tile.isSteamReactor()) {
            this.gui
               .drawString(
                  matrix,
                  this.translate(
                     "gui.ic2.reactor_planner.prediction.steam.expected", new Object[]{Formatters.EU_FORMAT.format((double)prediction.steamPerTick)}
                  ),
                  8,
                  133,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.prediction.steam.produced", new Object[]{Formatters.EU_FORMAT.format(prediction.totalSteamProduced)}),
                  8,
                  143,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.prediction.water.consumed", new Object[]{Formatters.EU_FORMAT.format(prediction.totalWaterConsumed)}),
                  8,
                  153,
                  4210752
               );
            base = 163;
         } else {
            this.gui
               .drawString(
                  matrix,
                  this.translate(
                     "gui.ic2.reactor_planner.prediction.eu.expected", new Object[]{Formatters.EU_FORMAT.format((long)Mth.floor(prediction.energyPerTick))}
                  ),
                  8,
                  133,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.prediction.eu.produced", new Object[]{Formatters.EU_FORMAT.format(prediction.totalEnergyProduced)}),
                  8,
                  143,
                  4210752
               );
            base = 153;
         }

         this.gui
            .drawString(
               matrix,
               this.translate("gui.ic2.reactor_planner.prediction.heat_created", new Object[]{Formatters.EU_FORMAT.format(prediction.totalHeatProduced)}),
               8,
               base,
               4210752
            );
         this.gui
            .drawString(
               matrix,
               this.translate(
                  "gui.ic2.reactor_planner.prediction.explosion_strength", new Object[]{Formatters.EU_FORMAT.format((double)prediction.totalExplosionPower)}
               ),
               8,
               base + 10,
               4210752
            );
         this.gui
            .drawString(
               matrix,
               this.translate(
                  "gui.ic2.reactor_planner.prediction.efficiency",
                  new Object[]{
                     Formatters.EU_READER_FORMAT.format((double)prediction.efficiency), Formatters.EU_READER_FORMAT.format((double)prediction.totalEfficiency)
                  }
               ),
               8,
               base + 20,
               4210752
            );
         this.gui
            .drawString(
               matrix,
               this.translate(
                  "gui.ic2.reactor_planner.prediction.breeder", new Object[]{SanityHelper.firstLetterUppercase(Boolean.toString(prediction.breeder))}
               ),
               8,
               base + 30,
               4210752
            );
      }
   }
}
