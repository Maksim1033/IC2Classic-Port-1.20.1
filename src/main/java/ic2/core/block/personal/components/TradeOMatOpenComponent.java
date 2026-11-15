package ic2.core.block.personal.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.components.mv.planner.NamingTabComponent;
import ic2.core.block.personal.tile.TradeOMatTileEntity;
import ic2.core.block.personal.trade.ItemTrade;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconTooltip;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TradeOMatOpenComponent extends BaseTradeOMatComponent {
   TradeOMatTileEntity tile;
   boolean isUpdating = false;

   public TradeOMatOpenComponent(TradeOMatTileEntity tile) {
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
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      ItemTrade trade = this.tile.getTrade();
      gui.addRenderableWidget(0, new IconTooltip(x + 8, y + 37, 14, 14, new ItemStack(IC2Items.COIN)).setToolTip("gui.ic2.trade.money"));
      gui.addRenderableWidget(1, new IconTooltip(x + 8, y + 51, 14, 14, new ItemStack(Items.CLOCK)).setToolTip("gui.ic2.trade.delay"));
      gui.addRenderableWidget(2, new IconTooltip(x + 8, y + 65, 14, 14, new ItemStack(Items.BARRIER)).setToolTip("gui.ic2.trade.cap"));
      gui.addRenderableWidget(
         3,
         new ItemCheckBox(x + 81, y + 65, 14, 14, T -> this.sendDelayReset(), new ItemStack(Items.OAK_BUTTON), trade.tradeReset)
            .setToolTip("gui.ic2.trade.cap_reset")
      );
      gui.addRenderableWidget(
         4,
         new ItemCheckBox(x + 153, y + 74, 14, 14, T -> this.sendInfiniteStock(), new ItemStack(IC2Items.UUMATTER), trade.infinite)
            .setToolTip("gui.ic2.trade.creative")
      );
      gui.addRenderableWidget(
         5, new ItemCheckBox(x + 138, y + 74, 14, 14, T -> this.tile.sendToServer(8, 0), new ItemStack(Items.EMERALD)).setToolTip("Per Player Trades")
      );
      ((ItemCheckBox)gui.addRenderableWidget(
            10, new ItemCheckBox(x + 134, y + 36, 14, 14, T -> this.sendInputFlags(128), new ItemStack(Items.WATER_BUCKET), (this.tile.inputFilter & 128) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.fluid");
      ((ItemCheckBox)gui.addRenderableWidget(
            11, new ItemCheckBox(x + 118, y + 36, 14, 14, T -> this.sendInputFlags(16), new ItemStack(Items.PAPER), (this.tile.inputFilter & 16) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.nbt");
      ((ItemCheckBox)gui.addRenderableWidget(
            15,
            new ItemCheckBox(x + 134, y + 52, 14, 14, T -> this.sendOutputFlags(128), new ItemStack(Items.WATER_BUCKET), (this.tile.inputFilter & 128) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.fluid");
      ((ItemCheckBox)gui.addRenderableWidget(
            16, new ItemCheckBox(x + 118, y + 52, 14, 14, T -> this.sendOutputFlags(16), new ItemStack(Items.PAPER), (this.tile.inputFilter & 16) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.nbt");
      gui.addRenderableWidget(20, this.create(40, trade.moneyNeeded, T -> this.sendValue(T, 2)));
      gui.addRenderableWidget(21, this.create(54, trade.tradeDelay, T -> this.sendValue(T, 3)));
      gui.addRenderableWidget(22, this.create(68, trade.tradeCap, T -> this.sendValue(T, 4)));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onChanged() {
      ItemTrade trade = this.tile.getTrade();
      this.isUpdating = true;
      this.update(20, Integer.toString(trade.moneyNeeded));
      this.update(21, Integer.toString(trade.tradeDelay));
      this.update(22, Integer.toString(trade.tradeCap));
      boolean app = ((ItemCheckBox)this.gui.getCastedButton(3, ItemCheckBox.class)).isChecked();
      if (app && (trade.tradeCap == 0 || trade.tradeDelay == 0)) {
         this.sendDelayReset();
      }

      this.isUpdating = false;
   }

   @OnlyIn(Dist.CLIENT)
   public void update(int index, String newValue) {
      EditBox text = this.gui.getCastedButton(index, EditBox.class);
      if (text != null && !text.getValue().equals(newValue)) {
         text.setValue(newValue);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((ItemCheckBox)gui.getCastedButton(3, ItemCheckBox.class)).setChecked(this.tile.getTrade().tradeReset);
      ((ItemCheckBox)gui.getCastedButton(4, ItemCheckBox.class)).setChecked(this.tile.getTrade().infinite);
      ((ItemCheckBox)gui.getCastedButton(5, ItemCheckBox.class)).setChecked(this.tile.getTrade().perPlayer);
      ((ItemCheckBox)gui.getCastedButton(10, ItemCheckBox.class)).setChecked((this.tile.inputFilter & 128) != 0);
      ((ItemCheckBox)gui.getCastedButton(11, ItemCheckBox.class)).setChecked((this.tile.inputFilter & 16) != 0);
      ((ItemCheckBox)gui.getCastedButton(15, ItemCheckBox.class)).setChecked((this.tile.outputFilter & 128) != 0);
      ((ItemCheckBox)gui.getCastedButton(16, ItemCheckBox.class)).setChecked((this.tile.outputFilter & 16) != 0);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawRightString(matrix, this.translate("gui.ic2.trade.in"), 115, 40, 4210752);
      this.gui.drawRightString(matrix, this.translate("gui.ic2.trade.out"), 115, 55, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   public EditBox create(int yLevel, int originalValue, Consumer<String> listener) {
      ImprovedTextWidget text = new ImprovedTextWidget(this.gui.getGuiLeft() + 27, this.gui.getGuiTop() + yLevel, 60, 12);
      text.setValue(Integer.toString(originalValue));
      text.setMaxLength(8);
      text.setFilter(NamingTabComponent.NUMBERS_ONLY);
      text.setResponder(T -> {
         if (!this.isUpdating) {
            listener.accept(T);
         }
      });
      text.setCanLoseFocus(true);
      text.setBordered(false);
      return text;
   }

   private void sendInfiniteStock() {
      this.tile.sendToServer(6, 0);
   }

   private void sendDelayReset() {
      this.tile.sendToServer(5, 0);
   }

   private void sendValue(String s, int index) {
      this.tile.sendToServer(index, s.isEmpty() ? 0 : Integer.parseInt(s));
   }

   private void sendInputFlags(int flag) {
      this.tile.sendToServer(0, flag);
   }

   private void sendOutputFlags(int flag) {
      this.tile.sendToServer(1, flag);
   }
}
