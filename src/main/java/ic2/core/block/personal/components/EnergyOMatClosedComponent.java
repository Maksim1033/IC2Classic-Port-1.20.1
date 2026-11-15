package ic2.core.block.personal.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.personal.tile.EnergyOMatTileEntity;
import ic2.core.block.personal.trade.EnergyTrade;
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

public class EnergyOMatClosedComponent extends GuiWidget {
   public static final Box2i INFO = new Box2i(9, 54, 8, 14);
   EnergyOMatTileEntity tile;

   public EnergyOMatClosedComponent(EnergyOMatTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(new ToolTipButton(x + 25, y + 81, 36, 14, this.translate("gui.ic2.trade.buy"), T -> this.tile.sendToServer(7, 0)));
      gui.hideSlot(0, 1, 2, 3);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      float value = this.tile.lastPayed <= 0 ? 0.0F : (float)(this.tile.payedEnergy + this.tile.storedEU) / (float)this.tile.lastPayed * 24.0F;
      if (value > 0.0F) {
         this.gui.drawTextureRegion(matrix, (float)(this.gui.getGuiLeft() + 97), (float)(this.gui.getGuiTop() + 54), 176.0F, 0.0F, value, 16.0F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      EnergyTrade trade = this.tile.getTrade();
      long timeLeft = trade.lastTrade + (long)trade.tradeDelay - this.tile.getLevel().getGameTime();
      if (timeLeft > 0L && trade.tradeDelay > 0) {
         this.gui
            .drawString(
               matrix,
               this.translate("gui.ic2.trade.restock")
                  .append(DurationFormatUtils.formatDuration(Duration.ofSeconds(timeLeft / 20L + 1L).toMillis(), "HH:mm:ss")),
               70,
               96,
               4210752
            );
      }

      this.gui.drawString(matrix, this.eu(trade.offeredEnergy), 110, 26, 4210752);
      this.gui
         .drawString(matrix, this.translate("gui.ic2.storage.output", new Object[]{Formatters.EU_FORMAT.format((long)this.tile.maxOutput)}), 70, 85, 4210752);
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

      Box2i box = new Box2i(112, 55, 28, 14);
      if (box.isInBox(mouseX, mouseY)) {
         tooltips.accept(this.translate("gui.ic2.trade.eu_left").append(this.eu(this.tile.payedEnergy + this.tile.storedEU)));
      }
   }
}
