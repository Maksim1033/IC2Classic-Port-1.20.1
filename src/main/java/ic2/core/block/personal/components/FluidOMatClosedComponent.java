package ic2.core.block.personal.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.personal.tile.FluidOMatTileEntity;
import ic2.core.block.personal.trade.Trade;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class FluidOMatClosedComponent extends GuiWidget {
   public static Box2i INFO = new Box2i(85, 72, 8, 14);
   FluidOMatTileEntity tile;

   public FluidOMatClosedComponent(FluidOMatTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(new ToolTipButton(x + 61, y + 87, 54, 12, this.translate("gui.ic2.trade.buy"), T -> this.tile.sendToServer(7, 0)));
      gui.hideSlot(0, 1, 2, 3);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (INFO.isInBox(mouseX, mouseY)) {
         Trade trade = this.tile.getTrade();
         MutableComponent text = this.translate("gui.ic2.trade.input_infos").append("\n");
         if (trade.moneyNeeded > 0) {
            text.append(this.translate("gui.ic2.trade.check.money_request", new Object[]{trade.moneyNeeded})).append("\n");
         }

         text.append(this.translate((this.tile.inputFilter & 16) != 0 ? "gui.ic2.trade.check.nbt_needed" : "gui.ic2.trade.check.nbt_ignored")).append("\n");
         if ((this.tile.inputFilter & 128) != 0) {
            text.append(this.translate("gui.ic2.trade.check.fluid")).append("\n");
         }

         tooltips.accept(text);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      Trade trade = this.tile.getTrade();
      this.gui
         .drawString(
            matrix,
            this.translate("gui.ic2.trade.stock", new Object[]{Formatters.EU_FORMAT.format((long)trade.getStock(this.tile.stock, null))}),
            70,
            100,
            4210752
         );
      long timeLeft = trade.lastTrade + (long)trade.tradeDelay - this.tile.getLevel().getGameTime();
      if (timeLeft > 0L && trade.tradeDelay > 0) {
         this.gui
            .drawString(
               matrix,
               this.translate("gui.ic2.trade.restock")
                  .append(DurationFormatUtils.formatDuration(Duration.ofSeconds(timeLeft / 20L + 1L).toMillis(), "HH:mm:ss")),
               70,
               90,
               4210752
            );
      }
   }
}
