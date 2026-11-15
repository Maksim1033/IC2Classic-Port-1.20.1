package ic2.core.block.transport.item.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.util.DirectionList;
import ic2.core.block.transport.item.tubes.FilterTubeTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.IconCheckBox;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.simple.SliderComponent;
import ic2.core.platform.player.KeyHelper;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class FilterTubeComponent extends GuiWidget {
   public static final Box2i PROGRESS_BOX = new Box2i(155, 17, 11, 112);
   FilterTubeTileEntity tile;
   SliderComponent slider = this.addChild(new SliderComponent(PROGRESS_BOX, new Box2i(176, 0, 12, 15), 7));

   public FilterTubeComponent(FilterTubeTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
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

      for (int i = 0; i < 14; i++) {
         int xOff = i % 7;
         int yOff = i / 7;
         int index = i;
         boolean visible = i < this.tile.stacks.size();
         gui.addRenderableWidget(
               i * 7,
               new IconButton(
                  x + 8 + xOff * 20,
                  y + 17 + yOff * 56,
                  20,
                  20,
                  visible ? ((FilterTubeTileEntity.FilterEntry)this.tile.stacks.get(i)).getStack() : ItemStack.EMPTY,
                  T -> this.removeIndex(index)
               )
            )
            .visible = visible;
         gui.addRenderableWidget(i * 7 + 1, this.create(x + 8 + xOff * 20, y + 37 + yOff * 56, index, "D", Direction.DOWN, 65535));
         gui.addRenderableWidget(i * 7 + 2, this.create(x + 18 + xOff * 20, y + 37 + yOff * 56, index, "U", Direction.UP, 16711680));
         gui.addRenderableWidget(i * 7 + 3, this.create(x + 8 + xOff * 20, y + 49 + yOff * 56, index, "N", Direction.NORTH, 16776960));
         gui.addRenderableWidget(i * 7 + 4, this.create(x + 18 + xOff * 20, y + 49 + yOff * 56, index, "S", Direction.SOUTH, 255));
         gui.addRenderableWidget(i * 7 + 5, this.create(x + 8 + xOff * 20, y + 61 + yOff * 56, index, "W", Direction.WEST, 16711935));
         gui.addRenderableWidget(i * 7 + 6, this.create(x + 18 + xOff * 20, y + 61 + yOff * 56, index, "E", Direction.EAST, 65280));
      }

      gui.addRenderableWidget(110, new ExtendedButton(x + 97, y + 134, 36, 14, this.translate("gui.ic2.color_tube.add"), T -> this.add()));
      ((ItemCheckBox)gui.addRenderableWidget(
            111, new ItemCheckBox(x + 75, y + 132, 18, 18, T -> this.inv(), new ItemStack(Items.BARREL)).setChecked(this.tile.invPriority)
         ))
         .setToolTip(
            this.translate("gui.ic2.tube_priority_inventory")
               .append("\n")
               .append(this.translate("gui.ic2.tube_priority_inventory.desc", new ChatFormatting[]{ChatFormatting.GRAY}))
         );
      gui.addRenderableWidget(120, this.create(x + 7, y + 131, -1, "D", Direction.DOWN, 65535));
      gui.addRenderableWidget(121, this.create(x + 17, y + 131, -1, "U", Direction.UP, 16711680));
      gui.addRenderableWidget(122, this.create(x + 27, y + 131, -1, "N", Direction.NORTH, 16776960));
      gui.addRenderableWidget(123, this.create(x + 37, y + 131, -1, "S", Direction.SOUTH, 255));
      gui.addRenderableWidget(124, this.create(x + 47, y + 131, -1, "W", Direction.WEST, 16711935));
      gui.addRenderableWidget(125, this.create(x + 57, y + 131, -1, "E", Direction.EAST, 65280));
   }

   @OnlyIn(Dist.CLIENT)
   private IconCheckBox create(int x, int y, int index, String text, Direction dir, int color) {
      IconCheckBox box = new IconCheckBox(
            x,
            y,
            10,
            12,
            this.string(text),
            index == -1
               ? this.tile.getEmptyDirs().notContains(dir)
               : this.tile.stacks.size() > index && ((FilterTubeTileEntity.FilterEntry)this.tile.stacks.get(index)).getSides().notContains(dir)
         )
         .setListener(T -> this.toggle(index, dir))
         .setDisableLike(true)
         .setToolTip(DirectionList.getName(dir));
      box.setFGColor(color);
      box.visible = index == -1;
      return box;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      this.slider.setMax(this.tile.stacks.size());

      for (int i = 0; i < 14; i++) {
         boolean visible = i + this.slider.getCurrent() < this.tile.stacks.size();
         ((IconButton)gui.getCastedButton(i * 7, IconButton.class))
            .setDisplay(visible ? ((FilterTubeTileEntity.FilterEntry)this.tile.stacks.get(this.slider.getCurrent() + i)).getStack() : ItemStack.EMPTY)
            .visible = visible;

         for (Direction dir : DirectionList.ALL) {
            ((IconCheckBox)gui.getCastedButton(i * 7 + dir.get3DDataValue() + 1, IconCheckBox.class))
               .setChecked(visible && ((FilterTubeTileEntity.FilterEntry)this.tile.stacks.get(this.slider.getCurrent() + i)).getSides().notContains(dir))
               .visible = visible;
         }
      }

      gui.getButton(110).active = !this.tile.inventory.getStackInSlot(0).isEmpty();
      ((ItemCheckBox)gui.getCastedButton(111, ItemCheckBox.class)).setChecked(this.tile.invPriority);

      for (Direction dir : DirectionList.ALL) {
         ((IconCheckBox)gui.getCastedButton(120 + dir.get3DDataValue(), IconCheckBox.class)).setChecked(this.tile.getEmptyDirs().notContains(dir));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      for (int i = 0; i < 14; i++) {
         IconButton button = this.gui.getCastedButton(i * 7, IconButton.class);
         if (button.isHoveredOrFocused() && button.visible) {
            tooltips.accept(button.getDisplay().getHoverName());
            tooltips.accept(this.buildKeyDescription(KeyHelper.SNEAK_KEY, "gui.ic2.color_tube.to_delete", new Object[0]).withStyle(ChatFormatting.GRAY));
            break;
         }

         for (int x = 1; x < 7; x++) {
            IconCheckBox widget = this.gui.getCastedButton(i * 7 + x, IconCheckBox.class);
            if (widget.isHoveredOrFocused() && widget.visible) {
               tooltips.accept(
                  this.translate(
                     widget.isChecked() ? "gui.ic2.color_tube.enable" : "gui.ic2.color_tube.disable",
                     new ChatFormatting[]{widget.isChecked() ? ChatFormatting.GREEN : ChatFormatting.RED}
                  )
               );
               return;
            }
         }
      }

      for (int xx = 0; xx < 6; xx++) {
         IconCheckBox widget = this.gui.getCastedButton(120 + xx, IconCheckBox.class);
         if (widget.isHoveredOrFocused() && widget.visible) {
            tooltips.accept(
               this.translate(
                  widget.isChecked() ? "gui.ic2.color_tube.enable" : "gui.ic2.color_tube.disable",
                  new ChatFormatting[]{widget.isChecked() ? ChatFormatting.GREEN : ChatFormatting.RED}
               )
            );
            tooltips.accept(this.translate("gui.ic2.color_tube.undefined_item", new ChatFormatting[]{ChatFormatting.DARK_GRAY}));
            return;
         }
      }
   }

   private void removeIndex(int index) {
      if (Screen.hasShiftDown()) {
         this.tile.sendToServer(1, index + this.slider.getCurrent());
      }
   }

   private void toggle(int index, Direction dir) {
      this.tile.sendToServer(10 + dir.get3DDataValue(), index + this.slider.getCurrent());
   }

   private void inv() {
      this.tile.sendToServer(2, 0);
   }

   private void add() {
      this.tile.sendToServer(3, 0);
   }
}
