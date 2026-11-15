package ic2.core.block.personal.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.components.mv.planner.NamingTabComponent;
import ic2.core.block.personal.tile.EnergyOMatTileEntity;
import ic2.core.block.personal.trade.EnergyTrade;
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

public class EnergyOMatOpenComponent extends BaseTradeOMatComponent {
   EnergyOMatTileEntity tile;
   boolean isUpdating = false;
   int lastValue;

   public EnergyOMatOpenComponent(EnergyOMatTileEntity tile) {
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
      EnergyTrade trade = this.tile.getTrade();
      gui.addRenderableWidget(0, new IconTooltip(x + 8, y + 36, 14, 14, new ItemStack(IC2Items.COIN)).setToolTip("Use Money"));
      gui.addRenderableWidget(1, new IconTooltip(x + 8, y + 50, 14, 14, new ItemStack(Items.CLOCK)).setToolTip("Trade Delay in Ticks"));
      gui.addRenderableWidget(2, new IconTooltip(x + 8, y + 64, 14, 14, new ItemStack(Items.BARRIER)).setToolTip("Trade Cap"));
      gui.addRenderableWidget(
         3,
         new ItemCheckBox(x + 65, y + 78, 14, 14, T -> this.sendDelayReset(), new ItemStack(Items.OAK_BUTTON), trade.tradeReset).setToolTip("Delay Resets Cap")
      );
      gui.addRenderableWidget(
         4,
         new ItemCheckBox(x + 153, y + 88, 14, 14, T -> this.sendInfiniteStock(), new ItemStack(IC2Items.UUMATTER), trade.infinite)
            .setToolTip("Creative Stock")
      );
      ((ItemCheckBox)gui.addRenderableWidget(
            10, new ItemCheckBox(x + 25, y + 78, 14, 14, T -> this.sendInputFlags(128), new ItemStack(Items.WATER_BUCKET), (this.tile.inputFilter & 128) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.fluid");
      ((ItemCheckBox)gui.addRenderableWidget(
            11, new ItemCheckBox(x + 41, y + 78, 14, 14, T -> this.sendInputFlags(16), new ItemStack(Items.PAPER), (this.tile.inputFilter & 16) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.nbt");
      gui.addRenderableWidget(20, this.create(39, trade.moneyNeeded, T -> this.sendValue(T, 2)));
      gui.addRenderableWidget(21, this.create(53, trade.tradeDelay, T -> this.sendValue(T, 3)));
      gui.addRenderableWidget(22, this.create(67, trade.tradeCap, T -> this.sendValue(T, 4)));
      this.lastValue = trade.offeredEnergy;
      ImprovedTextWidget widget = gui.addRenderableWidget(25, new ImprovedTextWidget(x + 101, y + 22, 50, 12));
      widget.setValue(Integer.toString(trade.offeredEnergy));
      widget.setBordered(false);
      widget.setResponder(this::sendOffer);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onChanged() {
      EnergyTrade trade = this.tile.getTrade();
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
      ((ItemCheckBox)gui.getCastedButton(10, ItemCheckBox.class)).setChecked((this.tile.inputFilter & 128) != 0);
      ((ItemCheckBox)gui.getCastedButton(11, ItemCheckBox.class)).setChecked((this.tile.inputFilter & 16) != 0);
      EnergyTrade trade = this.tile.getTrade();
      if (this.lastValue != trade.offeredEnergy) {
         this.lastValue = trade.offeredEnergy;
         ((ImprovedTextWidget)gui.getCastedButton(25, ImprovedTextWidget.class)).setExternalValue(Integer.toString(this.lastValue));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.trade.eu_buffer"), 85, 47, 4210752);
      this.gui.drawString(matrix, this.eu(this.tile.maxBuffer), 85, 57, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.trade.eu_output"), 85, 68, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.trade.eu_out", new Object[]{this.tile.maxOutput}), 85, 78, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   public EditBox create(int yLevel, int originalValue, Consumer<String> listener) {
      ImprovedTextWidget text = new ImprovedTextWidget(this.gui.getGuiLeft() + 27, this.gui.getGuiTop() + yLevel, 55, 12);
      text.setMaxLength(8);
      text.setValue(Integer.toString(originalValue));
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

   @OnlyIn(Dist.CLIENT)
   private void sendOffer(String s) {
      try {
         this.tile.sendToServer(8, Integer.parseInt(s));
      } catch (Exception var3) {
      }
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
}
