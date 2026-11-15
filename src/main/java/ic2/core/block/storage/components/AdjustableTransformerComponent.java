package ic2.core.block.storage.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.energy.EnergyNet;
import ic2.core.block.storage.tiles.transformer.AdjustableTransformerTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class AdjustableTransformerComponent extends GuiWidget {
   public static final String[] TIERS = new String[]{"UV", "LV", "MV", "HV", "EV", "IV", "LuV"};
   AdjustableTransformerTileEntity tile;
   int lastPacket;
   int lastEnergy;

   public AdjustableTransformerComponent(AdjustableTransformerTileEntity tile) {
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
      this.lastEnergy = this.tile.energyPacket;
      this.lastPacket = this.tile.packetCount;
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      Component ctrl = this.translate("tooltip.ic2.press_key_description", new Object[]{this.translate("tooltip.ic2.ctrl.name"), "10x"});
      Component shift = this.translate("tooltip.ic2.press_key_description", new Object[]{this.translate("tooltip.ic2.shift.name"), "100x"});
      Component alt = this.translate("tooltip.ic2.press_key_description", new Object[]{this.translate("tooltip.ic2.alt.name"), "1000x"});
      Component all = this.string().append(ctrl).append(Character.toString('\n')).append(shift).append(Character.toString('\n')).append(alt);
      gui.addRenderableWidget(0, new ToolTipButton(x + 157, y + 18, 12, 12, this.string("+"), T -> this.onPacket(1)).setToolTip(ctrl));
      gui.addRenderableWidget(1, new ToolTipButton(x + 157, y + 35, 12, 12, this.string("+"), T -> this.onEnergy(1)).setToolTip(all));
      gui.addRenderableWidget(2, new ToolTipButton(x + 7, y + 18, 12, 12, this.string("-"), T -> this.onPacket(-1)).setToolTip(ctrl));
      gui.addRenderableWidget(3, new ToolTipButton(x + 7, y + 35, 12, 12, this.string("-"), T -> this.onEnergy(-1)).setToolTip(all));
      gui.addRenderableWidget(
            4, new ToolTipButton(x + 119, y + 70, 50, 12, this.translate("gui.ic2.creative_source.confirm"), T -> this.confirmChange()).setToolTip(all)
         )
         .active = false;
      int xOffset = 13;

      for (int i = 0; i < TIERS.length; i++) {
         int index = i;
         int extra = gui.getFont().width(TIERS[i]) + 6;
         gui.addRenderableWidget(new ExtendedButton(x + xOffset, y + 55, extra, 12, this.string(TIERS[i]), T -> this.sendTier(index)));
         xOffset += extra + 3;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void confirmChange() {
      this.tile.sendToServer(0, this.tile.packetCount);
      this.tile.sendToServer(1, this.tile.energyPacket);
      this.lastPacket = this.tile.packetCount;
      this.lastEnergy = this.tile.energyPacket;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = this.tile.packetCount < 32;
      gui.getButton(1).active = this.tile.energyPacket <= 32767;
      gui.getButton(2).active = this.tile.packetCount > 1;
      gui.getButton(3).active = this.tile.energyPacket > 1;
      gui.getButton(4).active = this.tile.energyPacket != this.lastEnergy || this.tile.packetCount != this.lastPacket;
   }

   @OnlyIn(Dist.CLIENT)
   public void onPacket(int effect) {
      effect *= Screen.hasControlDown() ? 10 : 1;
      this.tile.packetCount = Mth.clamp(this.tile.packetCount + effect, 1, 32);
   }

   public void sendTier(int tier) {
      this.tile.energyPacket = EnergyNet.INSTANCE.getPowerFromTier(tier);
   }

   @OnlyIn(Dist.CLIENT)
   public void onEnergy(int effect) {
      effect *= Screen.hasControlDown() ? 10 : 1;
      effect *= Screen.hasShiftDown() ? 100 : 1;
      effect *= Screen.hasAltDown() ? 1000 : 1;
      this.tile.energyPacket = Mth.clamp(this.tile.energyPacket + effect, 1, 32768);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui
         .drawCenterString(
            matrix, this.translate("gui.ic2.creative_source.eu", new Object[]{Formatters.EU_FORMAT.format((long)this.tile.energyPacket)}), 83, 37, 4210752
         );
      this.gui
         .drawCenterString(
            matrix, this.translate("gui.ic2.creative_source.packets", new Object[]{Formatters.EU_FORMAT.format((long)this.tile.packetCount)}), 83, 20, 4210752
         );
   }
}
