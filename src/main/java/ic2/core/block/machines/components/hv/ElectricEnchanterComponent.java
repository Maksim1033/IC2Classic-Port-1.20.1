package ic2.core.block.machines.components.hv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.tiles.hv.ElectricEnchanterTileEntity;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class ElectricEnchanterComponent extends GuiWidget {
   static final int COLOR = ColorUtils.darker(ColorUtils.GREEN);
   static final ItemStack[] MODE_ITEMS = new ItemStack[]{new ItemStack(Items.ENCHANTING_TABLE), new ItemStack(Items.ANVIL), new ItemStack(Items.GRINDSTONE)};
   static final Box2i HOVER_BOX = new Box2i(112, 76, 45, 8);
   ElectricEnchanterTileEntity tile;
   DataSlot seed;
   int lastMode = 0;

   public ElectricEnchanterComponent(ElectricEnchanterTileEntity tile, DataSlot seed) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
      this.seed = seed;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, new ExtendedButton(x + 100, y + 75, 10, 10, this.string("-"), T -> this.exp(false)));
      gui.addRenderableWidget(1, new ExtendedButton(x + 159, y + 75, 10, 10, this.string("+"), T -> this.exp(true)));
      gui.addRenderableWidget(2, new ExtendedButton(x + 100, y + 61, 69, 12, this.getMode(), T -> this.start()));
      gui.addRenderableWidget(3, new IconButton(x + 150, y + 5, 20, 20, MODE_ITEMS[this.tile.mode], T -> this.modeSwitch()));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      AbstractWidget widget = gui.getButton(2);
      widget.active = this.tile.valid && !this.tile.running && this.tile.storedExperience >= this.tile.neededExperience;
      widget.setMessage(this.getMode());
      if (this.tile.mode != this.lastMode) {
         this.lastMode = this.tile.mode;
         ((IconButton)gui.getCastedButton(3, IconButton.class)).setDisplay(MODE_ITEMS[this.lastMode]);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      float stored = Mth.clamp((float)this.tile.storedExperience / 1000.0F * 45.0F, 0.0F, 45.0F);
      float needed = Mth.clamp((float)this.tile.neededExperience / 1000.0F * 45.0F, 0.0F, 45.0F);
      if (stored > 0.0F && needed > 0.0F) {
         if (needed > stored) {
            this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 112), (float)(this.gui.getGuiTop() + 76), needed, 8.0F, ColorUtils.RED);
            this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 112), (float)(this.gui.getGuiTop() + 76), stored, 8.0F, COLOR);
         } else {
            this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 112), (float)(this.gui.getGuiTop() + 76), stored, 8.0F, COLOR);
            this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 112), (float)(this.gui.getGuiTop() + 76), needed, 8.0F, ColorUtils.RED);
         }
      } else if (stored > 0.0F) {
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 112), (float)(this.gui.getGuiTop() + 76), stored, 8.0F, COLOR);
      } else if (needed > 0.0F) {
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 112), (float)(this.gui.getGuiTop() + 76), needed, 8.0F, ColorUtils.RED);
      }

      AbstractWidget widget = this.gui.getButton(2);
      if (widget != null && widget.active) {
         Slot slot = ((IC2Container)this.gui.getMenu()).getSlot(6);
         if (!slot.hasItem()) {
            ItemStack stack = this.tile.getPotentialOutput(this.seed.get());
            if (!stack.isEmpty()) {
               this.gui.drawItemStack(matrix, (float)(this.gui.getGuiLeft() + 124), (float)(this.gui.getGuiTop() + 37), stack);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (HOVER_BOX.isInBox(mouseX, mouseY)) {
         tooltips.accept(
            this.translate("gui.ic2.electric_enchanter.xp.stored", new Object[]{Formatters.EU_FORMAT.format((long)this.tile.storedExperience), 1000})
         );
         if (this.tile.neededExperience > 0) {
            tooltips.accept(this.translate("gui.ic2.electric_enchanter.xp.needed", new Object[]{Formatters.EU_FORMAT.format((long)this.tile.neededExperience)}));
         }
      }

      AbstractWidget widget = this.gui.getButton(2);
      if (widget != null && widget.active) {
         Slot slot = ((IC2Container)this.gui.getMenu()).getSlot(6);
         if (!slot.hasItem() && this.gui.isSlotHovered(slot, mouseX, mouseY)) {
            ItemStack stack = this.tile.getPotentialOutput(this.seed.get());
            if (!stack.isEmpty()) {
               stack.getTooltipLines(this.gui.getPlayer(), this.gui.getMinecraft().options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL)
                  .forEach(tooltips);
            }
         }
      }
   }

   private void modeSwitch() {
      this.tile.sendToServer(0, 0);
   }

   private void exp(boolean consume) {
      this.tile.sendToServer(1, (consume ? 1 : -1) * this.getType());
   }

   private void start() {
      this.tile.sendToServer(2, this.seed.get());
   }

   @OnlyIn(Dist.CLIENT)
   public int getType() {
      if (Screen.hasControlDown()) {
         return 1000;
      } else if (Screen.hasShiftDown()) {
         return 100;
      } else {
         return Screen.hasAltDown() ? 10 : 1;
      }
   }

   private Component getMode() {
      switch (this.tile.mode) {
         case 0:
            ItemStack stack = (ItemStack)this.tile.inventory.get(5);
            return this.translate("gui.ic2.electric_enchanter." + (stack.getItem() != Items.ENCHANTED_BOOK && !stack.isEnchanted() ? "enchant" : "reroll"));
         case 1:
            return this.translate("gui.ic2.electric_enchanter.combine");
         case 2:
            return this.translate("gui.ic2.electric_enchanter.disenchant");
         default:
            return this.translate("tooltip.item.ic2.error");
      }
   }
}
