package ic2.core.inventory.gui.components.simple;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconCheckBox;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.OpenerButton;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.inventory.gui.feature.ICloseableComponent;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InventorySettingsComponent extends GuiWidget implements ICloseableComponent {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/misc/inventory_helper.png");
   public static final List<Component> RULE_BUTTONS = ObjectArrayList.wrap(
      new Component[]{Component.literal("D"), Component.literal("I"), Component.literal("E"), Component.literal("B")}
   );
   private static final Component SELECTED = Component.translatable("gui.ic2.inventory.selected").withStyle(ChatFormatting.YELLOW);
   private static final Component CONFIGURE = Component.translatable("gui.ic2.inventory.configure").withStyle(ChatFormatting.YELLOW);
   InventoryHandler handler;
   int realSlot = -1;
   Vec2i offset;
   Vec2i buttonOffset;
   IntList ourSlots = new IntArrayList();

   public InventorySettingsComponent(InventoryHandler handler, Vec2i offset, Vec2i buttonOffset) {
      super(new Box2i(-100 + offset.getX(), offset.getY(), 100, 113));
      this.handler = handler;
      this.offset = offset;
      this.buttonOffset = buttonOffset;
      this.setVisible(false);
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
   }

   public InventoryHandler getHandler() {
      return this.handler;
   }

   public Vec2i getOffset() {
      return this.offset;
   }

   @Override
   public boolean isMouseOver(int x, int y) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int xOff = gui.getGuiLeft();
      int yOff = gui.getGuiTop();
      this.ourSlots.clear();

      for (int i = 0; i < 6; i++) {
         int x = xOff - 95 + 15 * i + this.offset.getX();
         int y = yOff + 30 + this.offset.getY();
         gui.addRenderableWidget(
            100 + i, new IconCheckBox(x, y, 14, 14, this.string(), true).setListener(new InventorySettingsComponent.PressHandler(this, i, true))
         );
         gui.addRenderableWidget(110 + i, new ToolTipButton(x, y + 15, 14, 14, this.string("B"), new InventorySettingsComponent.PressHandler(this, i, false)));
         this.ourSlots.add(100 + i);
         this.ourSlots.add(110 + i);
      }

      this.ourSlots.add(120);
      this.ourSlots.add(121);
      this.ourSlots.add(122);
      gui.addRenderableWidget(
         120,
         new ToolTipButton(
            xOff - 95 + this.offset.getX(),
            yOff + 60 + this.offset.getY(),
            89,
            14,
            this.translate("gui.ic2.inventory.reset"),
            new InventorySettingsComponent.ResetHandler(this)
         )
      );
      ((ItemCheckBox)gui.addRenderableWidget(
            121,
            new ItemCheckBox(
               xOff - 95 + this.offset.getX(),
               yOff + 75 + this.offset.getY(),
               14,
               14,
               new InventorySettingsComponent.FlagHandler(this, 1),
               IC2Items.ICON_DISPLAY.create(6),
               this.handler.isSingleItem()
            )
         ))
         .setToolTip("1 Item per Slot");
      ((ItemCheckBox)gui.addRenderableWidget(
            122,
            new ItemCheckBox(
               xOff - 80 + this.offset.getX(),
               yOff + 75 + this.offset.getY(),
               14,
               14,
               new InventorySettingsComponent.FlagHandler(this, 2),
               IC2Items.ICON_DISPLAY.create(7),
               this.handler.isSingleStack()
            )
         ))
         .setToolTip("1 Stack per Item");
      gui.addRenderableWidget(
         1100,
         new OpenerButton(
               xOff + 5 + this.buttonOffset.getX(),
               yOff + 5 + this.buttonOffset.getY(),
               10,
               10,
               this.string("I"),
               new InventorySettingsComponent.ActivateHandler(this, gui)
            )
            .setToolTip("gui.ic2.inventory.button")
      );
      this.tick(gui);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void closeComponent(IC2Screen screen) {
      this.setVisible(false);
      this.realSlot = -1;
      this.tick(screen);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      boolean notLocked = !this.handler.isLocked();

      for (int i = 0; i < 6; i++) {
         AbstractWidget widget = gui.getButton(100 + i);
         widget.active = this.isVisible() && notLocked;
         widget.visible = this.isVisible();
         widget = gui.getButton(110 + i);
         widget.active = this.isVisible() && notLocked;
         widget.visible = this.isVisible();
      }

      AbstractWidget widget = gui.getButton(120);
      widget.active = this.isVisible() && notLocked;
      widget.visible = this.isVisible();
      ItemCheckBox check = gui.getCastedButton(121, ItemCheckBox.class);
      check.active = this.isVisible() && notLocked;
      check.visible = this.isVisible();
      check.setChecked(this.handler.isSingleItem());
      check = gui.getCastedButton(122, ItemCheckBox.class);
      check.active = this.isVisible() && notLocked;
      check.visible = this.isVisible();
      check.setChecked(this.handler.isSingleStack());
      if (this.isVisible()) {
         if (this.realSlot != -1) {
            this.loadData(this.realSlot);
         } else {
            for (Direction dir : DirectionList.ALL) {
               ((ToolTipButton)gui.getCastedButton(110 + dir.get3DDataValue(), ToolTipButton.class))
                  .setToolTip("gui.ic2.inventory.access.rule", DirectionList.getName(dir), this.handler.getBlockAccess(dir).getName())
                  .setMessage(RULE_BUTTONS.get(this.handler.getBlockAccess(dir).getIndex()));
               ((IconCheckBox)gui.getCastedButton(100 + dir.get3DDataValue(), IconCheckBox.class))
                  .setToolTip(
                     this.handler.isBlockSideUnlocked(dir) ? "gui.ic2.inventory.access.enabled" : "gui.ic2.inventory.access.disabled",
                     DirectionList.getName(dir)
                  )
                  .setChecked(this.handler.isBlockSideUnlocked(dir));
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      if (this.isVisible()) {
         this.gui.bindTexture(TEXTURE);
         this.gui
            .drawTextureRegion(
               matrix,
               (float)(this.gui.getGuiLeft() - 100 + this.offset.getX()),
               (float)(this.gui.getGuiTop() + this.offset.getY()),
               0.0F,
               0.0F,
               100.0F,
               113.0F
            );
         RenderSystem.enableBlend();
         if (this.realSlot != -1) {
            Slot slot = this.findSlot(this.realSlot);
            if (slot != null) {
               this.gui
                  .drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + slot.x - 2), (float)(this.gui.getGuiTop() + slot.y - 2), 20.0F, 20.0F, -2130720768);
            }
         }

         IntListIterator var8 = this.handler.getAllSlots().iterator();

         while (var8.hasNext()) {
            int slot = (Integer)var8.next();
            if (slot != this.realSlot) {
               Slot data = this.findSlot(slot);
               if (data != null) {
                  this.gui
                     .drawColoredRegion(
                        matrix, (float)(this.gui.getGuiLeft() + data.x - 2), (float)(this.gui.getGuiTop() + data.y - 2), 20.0F, 20.0F, -2145473563
                     );
               }
            }
         }

         RenderSystem.defaultBlendFunc();
      }
   }

   private Slot findSlot(int slotId) {
      for (Slot slot : ((IC2Container)this.gui.getMenu()).slots) {
         if (slot.getSlotIndex() == slotId && this.handler.containsSlot(slot)) {
            return slot;
         }
      }

      return null;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      IntListIterator var5 = this.handler.getAllSlots().iterator();

      while (var5.hasNext()) {
         int slot = (Integer)var5.next();
         Slot data = this.findSlot(slot);
         if (data != null && data.x <= mouseX && data.x + 20 >= mouseX && data.y <= mouseY && data.y + 20 >= mouseY) {
            SlotType type = this.handler.getSlotType(slot);
            if (type != null) {
               tooltips.accept(type.getName().copy().withStyle(ChatFormatting.YELLOW));
            }

            if (slot == this.realSlot) {
               tooltips.accept(SELECTED);
            } else {
               tooltips.accept(CONFIGURE);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      if (this.isVisible()) {
         Component text = (Component)(this.realSlot == -1 ? this.translate("gui.ic2.inventory.machine") : this.handler.getSlotType(this.realSlot).getName());
         this.gui.drawCenterString(matrix, text, -50 + this.offset.getX(), 7 + this.offset.getY(), 4210752);
         String s = "DUNSWE";

         for (int i = 0; i < s.length(); i++) {
            this.gui.drawString(matrix, this.string(s.substring(i, i + 1)), -90 + i * 15 + this.offset.getX(), 20 + this.offset.getY(), 4210752);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (!this.isVisible()) {
         return false;
      } else {
         IntListIterator slot = this.ourSlots.iterator();

         while (slot.hasNext()) {
            int id = (Integer)slot.next();
            AbstractWidget widget = this.gui.getButton(id);
            if (widget != null && widget.isHoveredOrFocused()) {
               return false;
            }
         }

         if (this.gui.isHoveringOpenerButton(mouseX, mouseY)) {
            return false;
         } else {
            Slot slotx = this.gui.getSlotUnderMouse();
            if (slotx != null && this.handler.containsSlot(slotx)) {
               this.loadData(slotx.getSlotIndex());
               this.realSlot = slotx.getSlotIndex();
               return true;
            } else if (slotx == null) {
               this.realSlot = -1;
               return true;
            } else {
               return !(slotx instanceof ContainerComponent.SettingsSlot)
                  && (!(slotx.container instanceof InventoryMenu) || !SpecialFilters.HANDLER_ITEM.matches(slotx.getItem()));
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected void loadData(int slot) {
      for (Direction dir : DirectionList.ALL) {
         ((ToolTipButton)this.gui.getCastedButton(110 + dir.get3DDataValue(), ToolTipButton.class))
            .setToolTip("gui.ic2.inventory.access.rule", DirectionList.getName(dir), this.handler.getSlotAccess(slot, dir).getName())
            .setMessage(RULE_BUTTONS.get(this.handler.getSlotAccess(slot, dir).getIndex()));
         ((IconCheckBox)this.gui.getCastedButton(100 + dir.get3DDataValue(), IconCheckBox.class))
            .setToolTip(
               this.handler.isBlockSideUnlocked(dir) ? "gui.ic2.inventory.access.enabled" : "gui.ic2.inventory.access.disabled", DirectionList.getName(dir)
            )
            .setChecked(this.handler.isSlotSideUnlocked(slot, dir));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class ActivateHandler implements OnPress {
      InventorySettingsComponent settings;
      IC2Screen screen;

      public ActivateHandler(InventorySettingsComponent settings, IC2Screen screen) {
         this.settings = settings;
         this.screen = screen;
      }

      @OnlyIn(Dist.CLIENT)
      public void onPress(Button p_onPress_1_) {
         boolean visible = !this.settings.isVisible();
         this.screen.closeAllComponents();
         this.settings.setVisible(visible);
         this.settings.realSlot = -1;
         this.settings.tick(this.screen);
      }
   }

   public static class FlagHandler implements OnPress {
      InventorySettingsComponent settings;
      int subFlag;

      public FlagHandler(InventorySettingsComponent settings, int subFlag) {
         this.settings = settings;
         this.subFlag = subFlag;
      }

      public void onPress(Button p_onPress_1_) {
         IHasInventory inv = this.settings.handler.getInventory();
         if (inv instanceof BlockEntity) {
            IC2.NETWORKING.get(false).sendClientTileEvent((BlockEntity)inv, 32767 + inv.getSlotCount() + this.subFlag, this.getInvertedValue());
         }
      }

      private int getInvertedValue() {
         if (this.subFlag == 1) {
            return !this.settings.handler.isSingleItem() ? 1 : 0;
         } else if (this.subFlag == 2) {
            return !this.settings.handler.isSingleStack() ? 1 : 0;
         } else {
            return 0;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class PressHandler implements OnPress {
      InventorySettingsComponent settings;
      int index;
      boolean side;

      public PressHandler(InventorySettingsComponent settings, int index, boolean side) {
         this.settings = settings;
         this.index = index;
         this.side = side;
      }

      @OnlyIn(Dist.CLIENT)
      public void onPress(Button p_onPress_1_) {
         IHasInventory inventory = this.settings.handler.getInventory();
         if (inventory instanceof BlockEntity) {
            IC2.NETWORKING.get(false).sendClientTileEvent((BlockEntity)inventory, 32767 + this.settings.realSlot + 1, this.index << 1 | (this.side ? 1 : 0));
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class ResetHandler implements OnPress {
      InventorySettingsComponent settings;

      public ResetHandler(InventorySettingsComponent settings) {
         this.settings = settings;
      }

      @OnlyIn(Dist.CLIENT)
      public void onPress(Button p_onPress_1_) {
         IHasInventory inv = this.settings.handler.getInventory();
         if (inv instanceof BlockEntity) {
            IC2.NETWORKING.get(false).sendClientTileEvent((BlockEntity)inv, 32767 + inv.getSlotCount() + 3, this.settings.realSlot);
         }
      }
   }
}
