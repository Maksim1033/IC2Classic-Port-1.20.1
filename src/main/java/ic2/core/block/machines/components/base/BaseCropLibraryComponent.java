package ic2.core.block.machines.components.base;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.crops.ICropSeed;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.base.tiles.impls.BaseCropLibraryTileEntity;
import ic2.core.block.machines.tiles.mv.SimpleCropLibraryTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.feature.ICloseableComponent;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class BaseCropLibraryComponent extends GuiWidget implements ICloseableComponent {
   protected BaseCropLibraryTileEntity tile;

   public BaseCropLibraryComponent(BaseCropLibraryTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @Override
   public boolean isMouseOver(int x, int y) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, new ExtendedButton(x + 3, y - 22, 15, 20, this.string("<"), T -> this.tile.sendToServer(0, -1)));
      gui.addRenderableWidget(1, new ExtendedButton(x + 162, y - 22, 15, 20, this.string(">"), T -> this.tile.sendToServer(0, 1)));
      gui.addRenderableWidget(2, new ExtendedButton(x + 135, y + 90, 15, 10, this.string("D"), T -> this.tile.sendToServer(2, 1)));
      gui.addRenderableWidget(3, new ExtendedButton(x + 152, y + 90, 15, 10, this.string("U"), T -> this.tile.sendToServer(2, -1)));
      gui.addRenderableWidget(4, new ItemCheckBox(x + 5, y + 91, 14, 14, T -> this.setVisible(!this.tile.sorting), new ItemStack(Items.COMPARATOR)));
      gui.addRenderableWidget(
            5, new ExtendedButton(x - 69, y + 115, 30, 13, this.translate("gui.ic2.base_crop_library.next"), T -> this.tile.sendToServer(4, 0))
         )
         .visible = this.tile.sorting;
      gui.addRenderableWidget(
            6, new ItemCheckBox(x - 30, y + 110, 20, 20, T -> this.tile.sendToServer(5, 0), new ItemStack(Items.REDSTONE_TORCH), this.tile.inverting)
         )
         .visible = this.tile.sorting;
      gui.addOpenerWidget(4);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void closeComponent(IC2Screen screen) {
      this.tile.sendToServer(3, 0);
      this.tile.sorting = false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void setVisible(boolean visible) {
      this.gui.closeAllComponents();
      this.tile.sendToServer(3, visible ? 1 : 0);
      this.tile.sorting = visible;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ((ItemCheckBox)gui.getCastedButton(4, ItemCheckBox.class)).setChecked(this.tile.sorting);
      gui.getButton(5).visible = this.tile.sorting;
      ((ItemCheckBox)gui.getCastedButton(6, ItemCheckBox.class)).setChecked(this.tile.inverting).visible = this.tile.sorting;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      if (this.tile.sorting) {
         this.gui.drawTextureRegion(matrix, (float)(x - 73), (float)(y + 90), 176.0F, 0.0F, 74.0F, 44.0F);
      }

      for (int i = 0; i < 5; i++) {
         int index = this.tile.cropOffset * 5 + i;
         if (index <= this.tile.syncer.size()) {
            ResourceLocation location = index == 0 ? null : this.tile.syncer.get(index - 1);
            this.drawTopTab(
               matrix,
               20 + i * 28,
               -28,
               location == null ? new ItemStack(Items.BOOK) : ICropRegistry.INSTANCE.getDisplayItem(location),
               this.tile.crop == null ? location == null : this.tile.crop.id().equals(location)
            );
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      if (this.tile.syncer.isActive()) {
         ItemStack stack = this.getCrop(this.gui.getSlotUnderMouse());
         if (!stack.isEmpty()) {
            ICropSeed seed = (ICropSeed)stack.getItem();
            ICrop crop = seed.getCrop(stack);
            this.gui.guiGraphics.drawWordWrap(this.gui.getFont(), crop.getName(), 5, 5, 80, 16777215);
            int height = this.gui.getFont().split(crop.getName(), 80).size() * 9 - 9;
            this.gui
               .drawString(matrix, this.translate("gui.ic2.base_crop_library.tier", new Object[]{crop.getProperties().getTier()}), 5, 15 + height, 16777215);
            this.gui.drawString(matrix, this.translate("gui.ic2.base_crop_library.growth", new Object[]{seed.getGrowth(stack)}), 5, 30 + height, 11403055);
            this.gui.drawString(matrix, this.translate("gui.ic2.base_crop_library.gain", new Object[]{seed.getGain(stack)}), 5, 40 + height, 15649024);
            this.gui.drawString(matrix, this.translate("gui.ic2.base_crop_library.resistance", new Object[]{seed.getResistance(stack)}), 5, 50 + height, 52945);
            int count = StackUtil.getNbtData(stack).getInt("crop_count");
            if (count > 255) {
               this.gui.drawString(matrix, this.translate("gui.ic2.base_crop_library.count_enough"), 5, 65 + height, 16777215);
               return;
            }

            this.gui.drawString(matrix, this.translate("gui.ic2.base_crop_library.count", new Object[]{count}), 5, 65 + height, 16777215);
         }
      } else {
         this.gui.drawString(matrix, this.translate("gui.ic2.base_crop_library.no_power"), 5, 5, 16777215);
      }

      if (this.tile.sorting) {
         this.gui.drawString(matrix, SimpleCropLibraryTileEntity.SORTERS.get(this.tile.sorter).getName(), -70, 95, 4210752);
      }
   }

   public ItemStack getCrop(Slot slot) {
      if (slot instanceof LockedSlot) {
         ItemStack stack = slot.getItem();
         if (stack.getItem() instanceof ICropSeed) {
            return stack;
         }
      }

      return ItemStack.EMPTY;
   }

   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      for (int i = 0; i < 5; i++) {
         int index = this.tile.cropOffset * 5 + i;
         if (index > this.tile.syncer.size()) {
            break;
         }

         int xPos = 20 + i * 28;
         if (mouseY >= -28 && mouseY <= 0 && mouseX >= xPos && mouseX <= xPos + 28) {
            if (index == 0) {
               tooltips.accept(this.translate("gui.ic2.base_crop_library.crop.all"));
            } else {
               ICrop crop = ICropRegistry.INSTANCE.getCrop(this.tile.syncer.get(index - 1));
               tooltips.accept((Component)(crop == null ? this.translate("gui.ic2.base_crop_library.crop.unknown") : crop.getName()));
            }
         }
      }
   }

   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      if (this.gui.getSlotUnderMouse() instanceof LockedSlot) {
         this.tile.sendToServer(2, -scroll);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      for (int i = 0; i < 5; i++) {
         int index = this.tile.cropOffset * 5 + i;
         if (index > this.tile.syncer.size()) {
            return false;
         }

         int xPos = 20 + i * 28;
         if (mouseY >= -28 && mouseY <= 0 && mouseX >= xPos && mouseX <= xPos + 28) {
            IC2.AUDIO.playSound(this.tile, SoundEvents.UI_BUTTON_CLICK.value().getLocation(), AudioManager.SoundType.STATIC, 0.2F, 1.0F);
            this.tile.sendToServer(1, index);
            return true;
         }
      }

      return false;
   }

   @OnlyIn(Dist.CLIENT)
   private void drawTopTab(PoseStack matrix, int xOffset, int yOffset, ItemStack item, boolean enabled) {
      if (item.isEmpty()) {
         item = new ItemStack(IC2Blocks.NUKE);
      }

      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      matrix.pushPose();
      this.gui.drawTextureRegion(matrix, (float)(x + xOffset), (float)(y + yOffset), enabled ? 28.0F : 0.0F, 222.0F, 28.0F, enabled ? 31.0F : 28.0F);
      this.gui.drawItemStack(matrix, (float)(x + xOffset + 6), (float)(y + yOffset + 8), item);
      matrix.popPose();
      this.gui.bindDefaultTexture();
   }
}
