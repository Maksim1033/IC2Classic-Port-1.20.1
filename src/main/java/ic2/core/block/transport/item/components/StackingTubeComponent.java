package ic2.core.block.transport.item.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.transport.item.tubes.StackingTubeTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.simple.SliderComponent;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class StackingTubeComponent extends GuiWidget {
   public static final Box2i TEXTURE = new Box2i(176, 0, 12, 15);
   public static final Box2i POSITION = new Box2i(156, 29, 12, 80);
   StackingTubeTileEntity tile;
   SliderComponent slider = this.addChild(new SliderComponent(POSITION, TEXTURE, 9).setNonEmptyRows(4));

   public StackingTubeComponent(StackingTubeTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, new ExtendedButton(x + 60, y + 121, 14, 14, this.string("+"), T -> this.trigger(1)));
      gui.addRenderableWidget(1, new ExtendedButton(x + 110, y + 121, 14, 14, this.string("-"), T -> this.trigger(-1)));
      ((ItemCheckBox)gui.addRenderableWidget(2, new ItemCheckBox(x + 155, y + 121, 14, 14, T -> this.tile.sendToServer(3, 0), new ItemStack(IC2Items.PAINTER))))
         .setToolTip("gui.ic2.stacking_tube.ignore_color");

      for (int i = 0; i < 45; i++) {
         int index = i;
         int xOff = i % 9;
         int yOff = i / 9;
         boolean showItem = i < this.tile.cached.size();
         gui.addRenderableWidget(
               10 + i,
               new IconButton(
                     x + 6 + xOff * 16,
                     y + 29 + yOff * 16,
                     16,
                     16,
                     showItem ? ((StackingTubeTileEntity.StackingStack)this.tile.cached.get(i)).getClientStack() : ItemStack.EMPTY,
                     T -> this.sendItem(index)
                  )
                  .setDurability(true)
            )
            .visible = showItem;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = this.tile.stacking < 64;
      gui.getButton(1).active = this.tile.stacking > 1;
      ((ItemCheckBox)gui.getCastedButton(2, ItemCheckBox.class)).setChecked(this.tile.ignoreColors);

      for (int i = 0; i < 45; i++) {
         int index = i + this.slider.getCurrent();
         IconButton button = gui.getCastedButton(10 + i, IconButton.class);
         button.visible = index < this.tile.cached.size();
         button.setDisplay(button.visible ? ((StackingTubeTileEntity.StackingStack)this.tile.cached.get(index)).getClientStack() : ItemStack.EMPTY);
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected void trigger(int size) {
      if (Screen.hasShiftDown()) {
         size *= 10;
      }

      this.tile.stacking = Mth.clamp(this.tile.stacking + size, 1, 64);
      this.tile.sendToServer(1, size);
   }

   protected void sendItem(int index) {
      this.tile.sendToServer(2, index + this.slider.getCurrent());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.slider.setMax(this.tile.cached.size());
      this.gui.drawString(matrix, this.translate("gui.ic2.stacking_tube.items"), 5, 20, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.stacking_tube.threshold"), 67, 112, 4210752);
      String s = this.tile.stacking + "";
      this.gui.drawCenterString(matrix, this.string(s), 92, 125, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      for (int i = 0; i < 45; i++) {
         AbstractWidget widget = this.gui.getButton(10 + i);
         if (widget.isHoveredOrFocused() && widget.visible && this.slider.getCurrent() + i < this.tile.cached.size()) {
            for (Component comp : ((StackingTubeTileEntity.StackingStack)this.tile.cached.get(i + this.slider.getCurrent()))
               .getClientStack()
               .getTooltipLines(this.gui.getPlayer(), Default.NORMAL)) {
               tooltips.accept(comp);
            }

            tooltips.accept(
               this.translate(
                  "gui.ic2.stacking_tube.stored",
                  new Object[]{((StackingTubeTileEntity.StackingStack)this.tile.cached.get(i)).getAmount(), ChatFormatting.GRAY}
               )
            );
            tooltips.accept(this.translate("gui.ic2.stacking_tube.click", new ChatFormatting[]{ChatFormatting.GRAY}));
         }
      }
   }
}
