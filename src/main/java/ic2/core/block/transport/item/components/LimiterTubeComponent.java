package ic2.core.block.transport.item.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.transport.item.tubes.LimiterTubeTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.platform.player.KeyHelper;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class LimiterTubeComponent extends GuiWidget {
   LimiterTubeTileEntity tile;
   IHasInventory inv;

   public LimiterTubeComponent(LimiterTubeTileEntity tile, IHasInventory inv) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
      this.inv = inv;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();

      for (int i = 0; i < 16; i++) {
         int xOff = i % 7;
         int yOff = i / 7;
         int index = i;
         boolean visible = i < this.tile.usedColors.size();
         gui.addRenderableWidget(
               i,
               new IconButton(
                  x + 8 + xOff * 20,
                  y + 17 + yOff * 20,
                  20,
                  20,
                  visible ? new ItemStack(DyeItem.byColor((DyeColor)this.tile.usedColors.get(i))) : ItemStack.EMPTY,
                  T -> this.removeIndex(index)
               )
            )
            .visible = visible;
      }

      gui.addRenderableWidget(110, new ExtendedButton(x + 97, y + 82, 36, 14, this.translate("gui.ic2.color_tube.add"), T -> this.add()));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      for (int i = 0; i < 16; i++) {
         boolean visible = i < this.tile.usedColors.size();
         ((IconButton)gui.getCastedButton(i, IconButton.class))
            .setDisplay(visible ? new ItemStack(DyeItem.byColor((DyeColor)this.tile.usedColors.get(i))) : ItemStack.EMPTY)
            .visible = visible;
      }

      gui.getButton(110).active = !this.inv.getStackInSlot(0).isEmpty();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      for (int i = 0; i < 16; i++) {
         IconButton button = this.gui.getCastedButton(i, IconButton.class);
         if (button.isHoveredOrFocused() && button.visible) {
            tooltips.accept(button.getDisplay().getHoverName());
            tooltips.accept(this.buildKeyDescription(KeyHelper.SNEAK_KEY, "gui.ic2.color_tube.to_delete", new Object[0]).withStyle(ChatFormatting.GRAY));
            break;
         }
      }
   }

   private void removeIndex(int index) {
      if (Screen.hasShiftDown()) {
         this.tile.sendToServer(1, index);
      }
   }

   private void add() {
      if (!this.inv.getStackInSlot(0).isEmpty()) {
         DyeColor color = DyeColor.getColor(this.inv.getStackInSlot(0));
         if (color != null) {
            this.tile.sendToServer(2, color.getId());
         }
      }
   }
}
