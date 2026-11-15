package ic2.core.block.machines.components.nv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.components.mv.planner.NamingTabComponent;
import ic2.core.block.machines.containers.nv.IndustrialWorkbenchContainer;
import ic2.core.block.machines.tiles.nv.IndustrialWorkbenchTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ColorButton;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.inventory.gui.feature.ICloseableComponent;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Box2i;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class IndustrialWorkbenchRequest extends GuiWidget implements ICloseableComponent {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_work_table_request_expansion.png");
   IndustrialWorkbenchTileEntity tile;
   MutableBoolean viewOpen;
   DyeColor lastColor;

   public IndustrialWorkbenchRequest(IndustrialWorkbenchTileEntity tile, MutableBoolean viewOpen) {
      super(new Box2i(-118, 0, 118, 83));
      this.tile = tile;
      this.viewOpen = viewOpen;
   }

   @Override
   public void closeComponent(IC2Screen screen) {
      this.viewOpen.setFalse();
      this.gui.getButton(8).visible = this.viewOpen.booleanValue();
      this.gui.getButton(9).visible = this.viewOpen.booleanValue();
      this.gui.getButton(10).visible = this.viewOpen.booleanValue();
      this.gui.getButton(11).visible = this.viewOpen.booleanValue();
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.ITEM_TOOLTIP);
   }

   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      Slot slot = this.gui.getSlotUnderMouse();
      if (slot instanceof IndustrialWorkbenchContainer.RequestSlot && slot.hasItem() && Screen.hasShiftDown()) {
         this.tile.sendToServer(5, slot.getSlotIndex());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isVisible() {
      return this.viewOpen.isTrue();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(7, new ItemCheckBox(x + 8, y + 17, 16, 16, T -> this.toggleVisible(), new ItemStack(IC2Blocks.REQUEST_TUBE), false));
      ImprovedTextWidget widget = gui.addRenderableWidget(8, new ImprovedTextWidget(x - 79, y + 15, 32, 14));
      widget.visible = this.viewOpen.isTrue();
      widget.setMaxLength(3);
      widget.setFilter(NamingTabComponent.NUMBERS_ONLY);
      widget.setValue("0");
      widget.setBordered(false);
      widget.setCanLoseFocus(true);
      gui.addRenderableWidget(9, new ToolTipButton(x - 43, y + 13, 16, 12, this.string("+"), T -> this.addAmount())).visible = this.viewOpen.isTrue();
      ((ColorButton)gui.addRenderableWidget(10, new ColorButton(x - 25, y + 23, 16, 16, this.tile.requestColor, this::toggleColor)))
         .setToolTip(
            "gui.ic2.industrial_workbench.request_color",
            new Object[]{this.tile.requestColor == null ? this.translate("gui.ic2.none") : this.tile.requestColor.getName()}
         )
         .visible = this.viewOpen.isTrue();
      gui.addRenderableWidget(11, new ToolTipButton(x - 81, y + 27, 54, 12, this.translate("gui.ic2.industrial_workbench.request"), T -> this.sendRequest())).visible = this.viewOpen
         .isTrue();
   }

   @Override
   public void addItemTooltip(Slot slot, ItemStack stack, List<Component> tooltips) {
      if (slot instanceof IndustrialWorkbenchContainer.RequestSlot) {
         int state = StackUtil.getNbtData(stack).getInt("state");
         switch (state) {
            case 0:
               tooltips.add(this.translate("gui.ic2.industrial_workbench.requesting", new ChatFormatting[]{ChatFormatting.GRAY}));
               break;
            case 1:
               tooltips.add(this.translate("gui.ic2.industrial_workbench.requested", new ChatFormatting[]{ChatFormatting.GRAY}));
               break;
            case 2:
               tooltips.add(
                  this.translate("gui.ic2.industrial_workbench.awaiting", ChatFormatting.GRAY, new Object[]{StackUtil.getNbtData(stack).getInt("awaiting")})
               );
         }

         tooltips.add(
            this.buildKeyDescription(
               KeyHelper.SNEAK_KEY, new Object[]{this.translate("gui.ic2.industrial_workbench.request_remove").withStyle(ChatFormatting.UNDERLINE)}
            )
         );
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void toggleVisible() {
      boolean last = !this.viewOpen.booleanValue();
      this.gui.closeAllComponents();
      this.viewOpen.setValue(last);
   }

   @OnlyIn(Dist.CLIENT)
   private void sendRequest() {
      int value = this.getValue();
      if (value > 0 && !this.tile.requestedItem.getStackInSlot(0).isEmpty()) {
         this.tile.sendToServer(4, value);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void addAmount() {
      int value = (Screen.hasControlDown() ? 2 : 1) * (Screen.hasShiftDown() ? 16 : 1) * (Screen.hasAltDown() ? 64 : 1);
      ((ImprovedTextWidget)this.gui.getCastedButton(8, ImprovedTextWidget.class)).setValue(Integer.toString(Mth.clamp(value + this.getValue(), 0, 512)));
   }

   @OnlyIn(Dist.CLIENT)
   private void toggleColor(ColorButton button) {
      this.handleColorClick(button.getColor(), button, true, T -> {
         this.tile.sendToServer(3, T == null ? 0 : T.getId() + 1);
         this.tile.requestColor = T;
      });
   }

   private int getValue() {
      try {
         return Integer.parseInt(((ImprovedTextWidget)this.gui.getCastedButton(8, ImprovedTextWidget.class)).getValue());
      } catch (Exception var2) {
         return 0;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((ItemCheckBox)gui.getCastedButton(7, ItemCheckBox.class)).setChecked(this.viewOpen.booleanValue());
      int value = this.getValue();
      gui.getButton(11).active = value > 0 && value <= 512 && !this.tile.requestedItem.getStackInSlot(0).isEmpty();
      if (this.tile.requestColor != this.lastColor) {
         this.lastColor = this.tile.requestColor;
         ((ColorButton)gui.getCastedButton(10, ColorButton.class))
            .setColor(this.tile.requestColor)
            .setToolTip(
               "gui.ic2.industrial_workbench.request_color",
               new Object[]{this.tile.requestColor == null ? this.translate("gui.ic2.none") : this.tile.requestColor.getName()}
            );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      this.gui.getButton(8).visible = this.viewOpen.booleanValue();
      this.gui.getButton(9).visible = this.viewOpen.booleanValue();
      this.gui.getButton(10).visible = this.viewOpen.booleanValue();
      this.gui.getButton(11).visible = this.viewOpen.booleanValue();
      if (!this.viewOpen.isFalse()) {
         this.gui.bindTexture(TEXTURE);
         int x = this.gui.getGuiLeft();
         int y = this.gui.getGuiTop();
         this.gui.drawTextureRegion(matrix, (float)(x - 118), (float)y, 0.0F, 0.0F, 118.0F, 83.0F);
      }
   }
}
