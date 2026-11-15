package ic2.core.block.transport.item.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.core.block.machines.components.mv.planner.NamingTabComponent;
import ic2.core.block.transport.item.tubes.FilteredExtractionTubeTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class FilteredExtractionTubeComponent extends GuiWidget {
   static final ItemStack[] DATA = createItemStack();
   static final Component[] NAMES = createNames();
   FilteredExtractionTubeTileEntity tile;
   int last;
   IHasInventory inv;

   public FilteredExtractionTubeComponent(FilteredExtractionTubeTileEntity tile, IHasInventory inv) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
      this.inv = inv;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();

      for (int i = 0; i < 16; i++) {
         this.inv
            .setStackInSlot(
               i, i < this.tile.filters.size() ? ((FilteredExtractionTubeTileEntity.FilterEntry)this.tile.filters.get(i)).getStack() : ItemStack.EMPTY
            );
      }

      ((ItemCheckBox)gui.addRenderableWidget(
            20, new ItemCheckBox(x + 106, y + 35, 16, 16, T -> this.sendFlags(128), new ItemStack(Items.WATER_BUCKET), (this.tile.flags & 128) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.fluid");
      ((ItemCheckBox)gui.addRenderableWidget(
            22, new ItemCheckBox(x + 88, y + 35, 16, 16, T -> this.sendFlags(16), new ItemStack(Items.PAPER), (this.tile.flags & 16) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.nbt");
      ((ItemCheckBox)gui.addRenderableWidget(
            23, new ItemCheckBox(x + 124, y + 35, 16, 16, T -> this.sendFlags(256), new ItemStack(IC2Items.DRILL_DIAMOND), (this.tile.flags & 256) != 0)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.durability");
      ((IconButton)gui.addRenderableWidget(24, new IconButton(x + 142, y + 35, 16, 16, DATA[Math.max(0, this.tile.durability)], T -> this.sendDurability())))
         .setDurability(this.tile.durability != 0)
         .setToolTip(NAMES[Math.max(0, this.tile.durability)])
         .visible = (this.tile.flags & 256) != 0;
      gui.addRenderableWidget(
            26, new ExtendedButton(x + 88, y + 18, 52, 14, this.translate("gui.ic2.filter_extraction_tube.save"), T -> this.tile.sendToServer(6, 0))
         )
         .active = this.tile.selectedIndex != -1;
      ((ItemCheckBox)gui.addRenderableWidget(
            27, new ItemCheckBox(x + 169, y + 62, 16, 16, T -> this.toggleWhitelist(), new ItemStack(Items.BOOK), this.tile.whitelist)
         ))
         .setToolTip("tooltip.item.ic2.transport_update.compare.whitelist");
      EditBox text = gui.addRenderableWidget(28, new ImprovedTextWidget(gui.getFont(), x + 89, y + 66, 32, 14, this.string()));
      text.setBordered(false);
      text.setMaxLength(4);
      text.setFilter(NamingTabComponent.NUMBERS_ONLY);
      text.setCanLoseFocus(true);
      text.insertText(Integer.toString(this.tile.keepItems));
      text.setResponder(this::setKeepItems);
      this.last = this.tile.keepItems;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((ItemCheckBox)gui.getCastedButton(20, ItemCheckBox.class)).setChecked((this.tile.flags & 128) != 0);
      ((ItemCheckBox)gui.getCastedButton(22, ItemCheckBox.class)).setChecked((this.tile.flags & 16) != 0);
      ((ItemCheckBox)gui.getCastedButton(23, ItemCheckBox.class)).setChecked((this.tile.flags & 256) != 0);
      ((IconButton)gui.getCastedButton(24, IconButton.class))
         .setDisplay(DATA[Math.max(0, this.tile.durability)])
         .setDurability(this.tile.durability != 0)
         .setToolTip(NAMES[Math.max(0, this.tile.durability)])
         .visible = (this.tile.flags & 256) != 0;

      for (int i = 0; i < 16; i++) {
         this.inv
            .setStackInSlot(
               i, i < this.tile.filters.size() ? ((FilteredExtractionTubeTileEntity.FilterEntry)this.tile.filters.get(i)).getStack() : ItemStack.EMPTY
            );
      }

      ((ItemCheckBox)gui.getCastedButton(27, ItemCheckBox.class)).setChecked(this.tile.whitelist);
      gui.getButton(26).active = !this.tile.filterInventory.getStackInSlot(0).isEmpty() && (this.tile.selectedIndex != -1 || this.tile.filters.size() < 16);
      ImprovedTextWidget widget = gui.getCastedButton(28, ImprovedTextWidget.class);
      widget.tick();
      if (this.last != this.tile.keepItems) {
         this.last = this.tile.keepItems;
         widget.setExternalValue(Integer.toString(this.tile.keepItems));
      }
   }

   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (mouseX >= 7 && mouseY >= 17) {
         int xIndex = (mouseX - 7) / 18;
         int yIndex = (mouseY - 17) / 18;
         if (xIndex <= 3 && yIndex <= 3) {
            this.selectIndex(yIndex * 4 + xIndex);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void sendDurability() {
      this.tile.sendToServer(2, (this.tile.durability + 1) % 11);
   }

   private void sendFlags(int flags) {
      this.tile.sendToServer(1, flags);
   }

   private void setKeepItems(String value) {
      try {
         this.tile.sendToServer(3, Integer.parseInt(value));
      } catch (Exception var3) {
      }
   }

   private void toggleWhitelist() {
      this.tile.sendToServer(5, this.tile.whitelist ? 1 : 0);
   }

   private void selectIndex(int index) {
      if (index != -1 && Screen.hasShiftDown()) {
         this.tile.sendToServer(7, index);
      } else {
         this.tile.sendToServer(4, index);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.filter_extraction_tube.keep"), 88, 55, 4210752);
      this.gui.drawRightString(matrix, this.gui.getPlayer().getInventory().getName(), 177, 82, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (mouseX >= 7 && mouseY >= 17) {
         int xIndex = (mouseX - 7) / 18;
         int yIndex = (mouseY - 17) / 18;
         if (xIndex <= 3 && yIndex <= 3) {
            int index = yIndex * 4 + xIndex;
            if (this.tile.filters.size() > index) {
               FilteredExtractionTubeTileEntity.FilterEntry entry = (FilteredExtractionTubeTileEntity.FilterEntry)this.tile.filters.get(index);
               tooltips.accept(entry.getStack().getHoverName());
               tooltips.accept(this.buildKeyDescription(KeyHelper.SNEAK_KEY, "gui.ic2.color_tube.to_delete", new Object[0]).withStyle(ChatFormatting.GRAY));
               if (entry.getKeepItems() > 0) {
                  tooltips.accept(this.translate("gui.ic2.filter_extraction_tube.items_to_keep", new Object[]{entry.getKeepItems()}));
               }
            }
         }
      }
   }

   static Component[] createNames() {
      return new Component[]{
         Component.translatable("tooltip.item.ic2.transport_update.durability.exact"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.1a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.1b"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.25a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.25b"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.50a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.50b"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.70a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.70b"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.90a"),
         Component.translatable("tooltip.item.ic2.transport_update.durability.90b")
      };
   }

   static ItemStack[] createItemStack() {
      double[] max = new double[]{1.0, 0.9, 0.9, 0.75, 0.75, 0.5, 0.5, 0.25, 0.25, 0.1, 0.0};
      ItemStack[] data = new ItemStack[max.length];

      for (int i = 0; i < max.length; i++) {
         ItemStack stack = new ItemStack(IC2Items.DRILL_DIAMOND);
         int maxEU = ((IElectricItem)stack.getItem()).getCapacity(stack);
         ElectricItem.MANAGER.charge(stack, (int)((double)maxEU * max[i]), 2147483647, true, false);
         data[i] = stack;
      }

      return data;
   }
}
