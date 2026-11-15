package ic2.core.block.machines.components.mv.planner;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.block.machines.logic.planner.newLogic.ReactorLogic;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LiveDataTabComponent extends BaseInfoTabComponent {
   public LiveDataTabComponent(ReactorPlannerTileEntity tile) {
      super(88, tile, Component.translatable("gui.ic2.reactor_planner.active_info"), new ItemStack(Items.FURNACE), 0);
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
         ReactorLogic logic = this.tile.simulation;
         int base;
         if (this.tile.isSteamReactor()) {
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.production.steam", new Object[]{Formatters.EU_FORMAT.format((long)logic.steamProduction)}),
                  8,
                  130,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.produced.steam", new Object[]{Formatters.EU_FORMAT.format(logic.totalSteamProduced)}),
                  8,
                  140,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.consume.water", new Object[]{Formatters.EU_FORMAT.format(logic.totalWaterConsumed)}),
                  8,
                  150,
                  4210752
               );
            base = 160;
         } else {
            this.gui
               .drawString(
                  matrix,
                  this.translate("gui.ic2.reactor_planner.production.eu", new Object[]{Formatters.EU_FORMAT.format((double)logic.getEnergyOutput())}),
                  8,
                  133,
                  4210752
               );
            this.gui
               .drawString(
                  matrix,
                  this.translate(
                     "gui.ic2.reactor_planner.produced.eu",
                     new Object[]{Formatters.EU_FORMAT.format(logic.totalProducedPower * (long)IC2.CONFIG.reactorOutput.get())}
                  ),
                  8,
                  143,
                  4210752
               );
            base = 153;
         }

         this.gui
            .drawString(
               matrix, this.translate("gui.ic2.reactor_planner.core_heat", new Object[]{Formatters.EU_FORMAT.format((long)logic.heat)}), 8, base, 4210752
            );
         this.gui
            .drawString(
               matrix,
               this.translate("gui.ic2.reactor_planner.melting_heat", new Object[]{Formatters.EU_FORMAT.format((long)logic.maxHeat)}),
               8,
               base + 10,
               4210752
            );
         this.gui
            .drawString(
               matrix,
               this.translate("gui.ic2.reactor_planner.heat_effect", new Object[]{Formatters.EU_FORMAT.format((double)(logic.heatEffectModifier * 100.0F))}),
               8,
               base + 20,
               4210752
            );
         this.gui
            .drawString(
               matrix, this.translate("gui.ic2.reactor_planner.time", new Object[]{Formatters.EU_FORMAT.format((long)logic.tick)}), 8, base + 30, 4210752
            );
         this.gui
            .drawString(
               matrix,
               this.translate(
                  "gui.ic2.reactor_planner.time_left",
                  new Object[]{Formatters.EU_FORMAT.format((long)logic.ticksLeft), Formatters.EU_FORMAT.format((long)logic.totalTicks)}
               ),
               8,
               base + 40,
               4210752
            );
      }
   }
}
