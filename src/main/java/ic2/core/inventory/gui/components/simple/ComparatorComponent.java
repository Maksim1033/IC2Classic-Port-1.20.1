package ic2.core.inventory.gui.components.simple;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.redstone.IComparable;
import ic2.core.block.base.misc.comparator.BaseComparator;
import ic2.core.block.base.misc.comparator.ComparatorListener;
import ic2.core.block.base.misc.comparator.ComparatorManager;
import ic2.core.block.base.misc.comparator.ComparingList;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.OpenerButton;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.inventory.gui.feature.ICloseableComponent;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ComparatorComponent<T extends BaseTileEntity & IComparable> extends GuiWidget implements ICloseableComponent {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/misc/comparator_helper.png");
   public static final Component[] NAMES = new Component[]{
      Component.translatable("gui.ic2.comparator.evaluator.min"),
      Component.translatable("gui.ic2.comparator.evaluator.max"),
      Component.translatable("gui.ic2.comparator.evaluator.average")
   };
   public static final Box2i PRESENT_BOX = new Box2i(-117, 31, 52, 15);
   public static final Box2i COMPARATOR_BOX = new Box2i(-117, 87, 90, 15);
   BaseTileEntity tile;
   ComparatorManager manager;
   int selected = 6;
   SliderComponent selectedSlider;
   SliderComponent optionSlider;
   Vec2i buttonOffset;
   Vec2i offset;
   IntList ourSlots = new IntArrayList();

   public ComparatorComponent(T comparator, Vec2i buttonOffset, Vec2i offset) {
      super(new Box2i(-122 + offset.getX(), offset.getY(), 122, 139));
      this.manager = comparator.getManager();
      this.tile = comparator;
      this.buttonOffset = buttonOffset;
      this.offset = offset;
      this.selectedSlider = this.addChild(
            new SliderComponent(new Box2i(-19 + offset.getX(), 31 + offset.getY(), 12, 30), new Box2i(139, 0, 12, 11)).setNonEmptyRows(1)
         )
         .setCustomTexture(TEXTURE);
      this.optionSlider = this.addChild(
            new SliderComponent(new Box2i(-19 + offset.getX(), 87 + offset.getY(), 12, 45), new Box2i(151, 0, 12, 15)).setNonEmptyRows(2)
         )
         .setCustomTexture(TEXTURE);
      this.setVisible(false);
      this.selectedSlider.setVisible(false);
      this.optionSlider.setVisible(false);
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void closeComponent(IC2Screen screen) {
      if (this.isVisible()) {
         this.setVisible(false);
         this.toggleVisibility(screen, false);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void toggleVisibility(IC2Screen screen, boolean visible) {
      screen.closeAllComponents();
      this.setVisible(visible);
      this.selectedSlider.setVisible(visible);
      this.optionSlider.setVisible(visible);
      this.tile.sendToServer(65534, this.isVisible() ? 1 : 0);
      this.tick(screen);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      this.ourSlots.clear();
      int x = gui.getGuiLeft() - 122 + this.offset.getX();
      int y = gui.getGuiTop() + this.offset.getY();
      this.selectedSlider.setMax(this.getList().size());

      for (int i = 0; i < 4; i++) {
         boolean active = i < this.selectedSlider.getMax() && this.isVisible();
         int index = i;
         int flags = active ? this.getList().get(i).getFlags() : 0;
         this.updateVisibility(
               gui.addRenderableWidget(
                  200 + i * 3,
                  new ItemCheckBox(x + 56, y + 31 + i * 15, 15, 15, T -> this.flipBit(index, 0), new ItemStack(Items.REDSTONE_TORCH), (flags & 1) != 0)
                     .setToolTip("gui.ic2.comparator.invert")
               )
            )
            .visible = active;
         this.updateVisibility(
               gui.addRenderableWidget(
                  200 + i * 3 + 1,
                  new ItemCheckBox(x + 70, y + 31 + i * 15, 15, 15, T -> this.flipBit(index, 1), new ItemStack(Items.REPEATER), (flags & 2) != 0)
                     .setToolTip("gui.ic2.comparator.sign")
               )
            )
            .visible = active;
         this.updateVisibility(
               gui.addRenderableWidget(
                  200 + i * 3 + 2,
                  new ItemCheckBox(x + 84, y + 31 + i * 15, 15, 15, T -> this.flipBit(index, 2), new ItemStack(Items.REDSTONE_TORCH), (flags & 4) != 0)
                     .setToolTip("gui.ic2.comparator.post_invert")
               )
            )
            .visible = active;
         this.ourSlots.add(200 + i * 3);
         this.ourSlots.add(200 + i * 3 + 1);
         this.ourSlots.add(200 + i * 3 + 2);
      }

      String s = "DUNSWEV";
      int[] indexes = new int[]{1, 0, 2, 5, 3, 4, 6};

      for (int i : indexes) {
         this.updateVisibility(
               ((ToolTipButton)gui.addRenderableWidget(
                     250 + i, new ToolTipButton(x + 15 + i * 13, y + 17, 12, 12, this.string(Character.toString(s.charAt(i))), T -> this.changeSide(i))
                  ))
                  .setToolTip(
                     i == 6
                        ? Blocks.COMPARATOR.getName()
                        : DirectionList.getName(Direction.from3DDataValue(i)).append(Character.toString('\n')).append(IC2Blocks.ADVANCED_COMPARATOR.getName())
                  )
            )
            .active = i != 6;
         this.ourSlots.add(250 + i);
      }

      this.ourSlots.add(260);
      this.updateVisibility(
         (ToolTipButton)gui.addRenderableWidget(
            260,
            new ToolTipButton(x + 48, y + 62, 52, 12, NAMES[this.getList().getEvaluator()], T -> this.flipMax()).setToolTip("gui.ic2.comparator.evaluator")
         )
      );
      gui.addRenderableWidget(
         261,
         new OpenerButton(
               gui.getGuiLeft() + 16 + this.buttonOffset.getX(),
               gui.getGuiTop() + 5 + this.buttonOffset.getY(),
               10,
               10,
               this.string("C"),
               T -> this.toggleVisibility(gui, !this.isVisible())
            )
            .setToolTip("gui.ic2.comparator.settings")
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      this.selectedSlider.setMax(this.getList().size());
      this.optionSlider.setMax(this.getVisible().size());
      int current = this.selectedSlider.getCurrent();
      boolean empty = this.getList().size() > 0;

      for (int i = 0; i < 2; i++) {
         boolean active = this.selectedSlider.inRange(i);
         int flags = active ? this.getList().get(i + current).getFlags() : 0;
         ItemCheckBox box = this.updateVisibility(((ItemCheckBox)gui.getCastedButton(200 + i * 3, ItemCheckBox.class)).setChecked((flags & 1) != 0));
         box.visible = active && this.isVisible();
         box.active = empty;
         box = this.updateVisibility(((ItemCheckBox)gui.getCastedButton(200 + i * 3 + 1, ItemCheckBox.class)).setChecked((flags & 2) != 0));
         box.visible = active && this.isVisible();
         box.active = empty;
         box = this.updateVisibility(((ItemCheckBox)gui.getCastedButton(200 + i * 3 + 2, ItemCheckBox.class)).setChecked((flags & 4) != 0));
         box.visible = active && this.isVisible();
         box.active = empty;
      }

      for (int i = 0; i < 7; i++) {
         this.updateVisibility(gui.getButton(250 + i)).active = i != this.selected && this.isVisible();
      }

      this.updateVisibility(gui.getButton(260)).setMessage(NAMES[this.getList().getEvaluator()]);
   }

   @OnlyIn(Dist.CLIENT)
   private <K extends AbstractWidget> K updateVisibility(K button) {
      button.visible = this.isVisible();
      button.active = this.isVisible();
      return button;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      this.selectedSlider.setMax(this.getList().size());
      this.optionSlider.setMax(this.getVisible().size());
      this.gui.bindTexture(TEXTURE);
      int x = this.gui.getGuiLeft() - 122 + this.offset.getX();
      int y = this.gui.getGuiTop() + this.offset.getY();
      this.gui.drawTextureRegion(matrix, (float)x, (float)y, 0.0F, 0.0F, 122.0F, 139.0F);

      for (int i = 0; i < 2 && this.selectedSlider.inRange(i); i++) {
         this.gui
            .drawTextureRegion(
               matrix,
               (float)(x + 5),
               (float)(y + 31 + i * 15),
               163.0F,
               PRESENT_BOX.isInBox(mouseX - this.offset.getX(), mouseY - 15 * i - this.offset.getY()) ? 15.0F : 0.0F,
               52.0F,
               15.0F
            );
      }

      for (int i = 0; i < 3 && this.optionSlider.inRange(i); i++) {
         this.gui
            .drawTextureRegion(
               matrix,
               (float)(x + 5),
               (float)(y + 87 + i * 15),
               163.0F,
               COMPARATOR_BOX.isInBox(mouseX - this.offset.getX(), mouseY - 15 * i - this.offset.getY()) ? 45.0F : 30.0F,
               90.0F,
               15.0F
            );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.comparator.settings"), -62 + this.offset.getX(), 5 + this.offset.getY(), 4210752);
      float scale = 0.5F;
      float invScale = 1.0F / scale;
      this.gui
         .drawString(
            matrix,
            this.translate("gui.ic2.comparator.out", new Object[]{this.getList().getValue()}),
            -117 + this.offset.getX(),
            63 + this.offset.getY(),
            4210752
         );
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.comparator.sources"), -65 + this.offset.getX(), 76 + this.offset.getY(), 4210752);
      matrix.scale(scale, scale, scale);

      for (int i = 0; i < 2 && this.selectedSlider.inRange(i); i++) {
         ComparatorListener listener = this.getList().get(i + this.selectedSlider.getCurrent());
         this.gui
            .drawString(
               matrix,
               listener.getName(),
               (int)((float)(-114 - this.offset.getX()) * invScale),
               (int)((float)(34 + this.offset.getY() + 15 * i) * invScale),
               -1
            );
         this.gui
            .drawString(
               matrix,
               this.translate("gui.ic2.comparator.output.dual", new Object[]{listener.getBaseValue(), listener.getValue()}),
               (int)((float)(-114 - this.offset.getX()) * invScale),
               (int)((float)(39 + this.offset.getY() + 15 * i) * invScale),
               -1
            );
      }

      List<BaseComparator> visible = this.getVisible();
      Direction dir = this.selected == 6 ? null : Direction.from3DDataValue(this.selected);

      for (int i = 0; i < 3 && this.optionSlider.inRange(i); i++) {
         int index = i + this.optionSlider.getCurrent();
         if (index < visible.size()) {
            BaseComparator comp = visible.get(index);
            if (comp != null) {
               this.gui
                  .drawString(
                     matrix,
                     comp.getName(),
                     (int)((float)(-114 - this.offset.getX()) * invScale),
                     (int)((float)(90 + this.offset.getY() + 15 * i) * invScale),
                     -1
                  );
               this.gui
                  .drawString(
                     matrix,
                     this.translate("gui.ic2.comparator.output.single", new Object[]{comp.getValue(dir)}),
                     (int)((float)(-114 - this.offset.getX()) * invScale),
                     (int)((float)(95 + this.offset.getY() + 15 * i) * invScale),
                     -1
                  );
            }
         }
      }

      matrix.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (!this.isVisible()) {
         return false;
      } else {
         IntListIterator i = this.ourSlots.iterator();

         while (i.hasNext()) {
            int id = (Integer)i.next();
            AbstractWidget widget = this.gui.getButton(id);
            if (widget != null && widget.isHoveredOrFocused()) {
               return false;
            }
         }

         for (int ix = 0; ix < 2 && this.selectedSlider.inRange(ix); ix++) {
            if (PRESENT_BOX.isInBox(mouseX - this.offset.getX(), mouseY - 15 * ix - this.offset.getY())) {
               this.tile.sendToServer(65536, ix + this.selectedSlider.getCurrent() << 3 | this.selected);
               return true;
            }
         }

         for (int ixx = 0; ixx < 3 && this.optionSlider.inRange(ixx); ixx++) {
            if (COMPARATOR_BOX.isInBox(mouseX - this.offset.getX(), mouseY - 15 * ixx - this.offset.getY())) {
               this.tile.sendToServer(65535, this.getIndex(ixx + this.optionSlider.getCurrent()) << 3 | this.selected);
               return true;
            }
         }

         return false;
      }
   }

   public ComparingList getList() {
      return this.manager.getList(this.selected);
   }

   public int getIndex(int baseIndex) {
      List<BaseComparator> visible = this.getVisible();
      return baseIndex >= visible.size() ? baseIndex : this.manager.getIndex(visible.get(baseIndex));
   }

   public List<BaseComparator> getVisible() {
      return this.manager.getComparators();
   }

   public void flipMax() {
      this.tile.sendToServer(65538, this.selected);
   }

   public void changeSide(int side) {
      this.selected = side;
      this.selectedSlider.setCurrent(0);
   }

   public void flipBit(int index, int bitIndex) {
      this.tile.sendToServer(65537, index + this.selectedSlider.getCurrent() << 5 | (bitIndex & 3) << 3 | this.selected);
   }
}
