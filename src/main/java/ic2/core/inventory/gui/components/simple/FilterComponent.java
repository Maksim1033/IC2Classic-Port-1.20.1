package ic2.core.inventory.gui.components.simple;

import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.items.ITagItem;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.ComponentContainerScreen;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.OpenerButton;
import ic2.core.inventory.gui.feature.ICloseableComponent;
import ic2.core.inventory.slot.IGhostSlot;
import ic2.core.item.misc.FluidDisplay;
import ic2.core.item.misc.TagBlock;
import ic2.core.item.misc.TagItem;
import ic2.core.platform.recipes.helpers.ItemStackCache;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class FilterComponent extends GuiWidget implements ICloseableComponent {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/misc/filter_helper.png");
   private static final Component SELECTED = Component.translatable("gui.ic2.inventory.selected").withStyle(ChatFormatting.YELLOW);
   private static final Component CONFIGURE = Component.translatable("gui.ic2.inventory.configure").withStyle(ChatFormatting.YELLOW);
   final Component selecting_text;
   final boolean helper;
   IFilterTarget ghost;
   List<ItemStack> allValid = CollectionUtils.<ItemStack>createList();
   List<String> allValidText = CollectionUtils.<String>createList();
   List<ItemStack> displayed = CollectionUtils.<ItemStack>createList();
   SliderComponent slider;
   String lastText = "";
   final Vec2i offsets;
   final Vec2i buttonOffset;
   boolean blockTags;
   boolean fluids;
   boolean blockClosing;

   public FilterComponent(Vec2i offsets) {
      super(new Box2i(-118 + offsets.getX(), offsets.getY(), 118, 132));
      this.offsets = offsets;
      this.helper = false;
      this.buttonOffset = null;
      this.selecting_text = this.translate("gui.ic2.filter.ghost");
      this.slider = this.addChild(new SliderComponent(new Box2i(-19 + offsets.getX(), 37 + offsets.getY(), 12, 88), new Box2i(0, 132, 12, 15), 5))
         .setNonEmptyRows(4)
         .setCustomTexture(TEXTURE);
      this.setVisible(false);
      this.slider.setVisible(false);
   }

   public FilterComponent(Vec2i offsets, Vec2i buttonOffset) {
      super(new Box2i(-118 + offsets.getX(), offsets.getY(), 118, 132));
      this.offsets = offsets;
      this.buttonOffset = buttonOffset;
      this.helper = true;
      this.selecting_text = this.translate("gui.ic2.filter.normal");
      this.slider = this.addChild(new SliderComponent(new Box2i(-19 + offsets.getX(), 37 + offsets.getY(), 12, 88), new Box2i(0, 132, 12, 15), 5))
         .setNonEmptyRows(4)
         .setCustomTexture(TEXTURE);
      this.setVisible(false);
      this.slider.setVisible(false);
   }

   public FilterComponent setBlockTags() {
      this.blockTags = true;
      return this;
   }

   public FilterComponent blockClosing() {
      this.blockClosing = true;
      return this;
   }

   public FilterComponent setFluid() {
      this.fluids = true;
      return this;
   }

   @Override
   public boolean isMouseOver(int x, int y) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public void setGhost(IFilterTarget ghost) {
      if (this.ghost != ghost) {
         this.displayed.clear();
         this.allValid.clear();
         this.slider.setMax(0);
         if (ghost == null) {
            this.ghost = null;
         } else {
            if (!this.helper) {
               this.toggleHelper(true);
            }

            this.ghost = ghost;
            if (ghost.allowEmptyStack()) {
               this.allValid.add(ItemStack.EMPTY);
               this.allValidText.add("empty");
            }

            if (this.fluids && ghost.isFluidSelection()) {
               for (Fluid fluid : ForgeRegistries.FLUIDS) {
                  if (fluid.isSource(fluid.defaultFluidState())) {
                     ItemStack stack = FluidDisplay.createDisplay(fluid);
                     this.allValid.add(stack);
                     this.allValidText.add(this.getText(stack));
                  }
               }
            } else {
               List<ItemStack> tag = CollectionUtils.<ItemStack>createList();
               ItemStack[] cache = ItemStackCache.INSTANCE.getRawCache();
               int i = 0;

               for (int m = ItemStackCache.INSTANCE.size(); i < m; i++) {
                  ItemStack stack = cache[i];
                  if (ghost.isValid(stack)) {
                     if (stack.getItem() instanceof ITagItem) {
                        tag.add(stack);
                     } else if (!(stack.getItem() instanceof TagItem)) {
                        this.allValid.add(stack.copy());
                        this.allValidText.add(this.getText(stack));
                     }
                  }
               }

               BuiltInRegistries.ITEM.getTagNames().forEach(T -> {
                  ItemStack stackx = new ItemStack(IC2Items.TAG_ITEM);
                  TagItem.setTag(stackx, T.location());
                  if (ghost.isValid(stackx)) {
                     this.allValid.add(stackx);
                     this.allValidText.add(this.getText(stackx));
                  }
               });
               if (this.blockTags) {
                  BuiltInRegistries.ITEM.getTagNames().forEach(T -> {
                     ItemStack stackx = new ItemStack(IC2Items.TAG_BLOCK);
                     TagBlock.setTag(stackx, T.location());
                     if (ghost.isValid(stackx)) {
                        this.allValid.add(stackx);
                        this.allValidText.add(this.getText(stackx));
                     }
                  });
               }

               this.allValid.addAll(tag);
               i = 0;

               for (int mx = tag.size(); i < mx; i++) {
                  this.allValidText.add(this.getText(tag.get(i)));
               }
            }

            String s = this.lastText;
            this.lastText = "THIS WILL NEVER MATCH";
            this.updateValues(s);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private String getText(ItemStack stack) {
      List<Component> components = stack.getTooltipLines(
         this.gui.getPlayer(), this.gui.getMinecraft().options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL
      );
      StringBuilder builder = new StringBuilder();
      int i = 0;

      for (int m = components.size(); i < m; i++) {
         builder.append(ChatFormatting.stripFormatting(components.get(i).getString()).toLowerCase(Locale.ROOT));
      }

      return builder.toString();
   }

   public boolean isHelper() {
      return this.helper;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      if (gui instanceof ComponentContainerScreen) {
         ((ComponentContainerScreen)gui).registerComponent(new FilterComponent.ActivatorComponent(this));
      }

      EditBox text = gui.addRenderableWidget(
         this.helper ? 1001 : 1000,
         new EditBox(gui.getFont(), gui.getGuiLeft() - 110 + this.offsets.getX(), gui.getGuiTop() + 22 + this.offsets.getY(), 99, 9, this.string())
      );
      text.setMaxLength(50);
      text.setBordered(false);
      text.setCanLoseFocus(true);
      text.setVisible(false);
      text.setResponder(this::updateValues);
      if (this.helper) {
         gui.addRenderableWidget(
            1002,
            new OpenerButton(
                  gui.getGuiLeft() + 5 + this.buttonOffset.getX(),
                  gui.getGuiTop() + 16 + this.buttonOffset.getY(),
                  10,
                  10,
                  this.string("S"),
                  T -> this.toggleHelper(!this.isVisible())
               )
               .setToolTip("gui.ic2.filter.button")
         );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      this.gui.bindTexture(TEXTURE);
      int x = this.gui.getGuiLeft() - 118 + this.offsets.getX();
      int y = this.gui.getGuiTop() + this.offsets.getY();
      this.gui.drawTextureRegion(matrix, (float)x, (float)y, 0.0F, 0.0F, 118.0F, 132.0F);
      this.gui.drawCenterString(matrix, this.selecting_text, x + 57, y + 8, 4210752);
      Lighting.setupFor3DItems();
      int listOffset = this.slider.getCurrent();

      for (int i = 0; i < 25 && this.displayed.size() > i + listOffset; i++) {
         int xPos = x + 6 + 18 * (i % 5);
         int yPos = y + 37 + 18 * (i / 5);
         RenderUtils.renderGuiItem(this.gui.getRenderItem(), matrix, this.displayed.get(i + listOffset), (float)xPos, (float)yPos);
         RenderUtils.renderGuiItemDecorations(matrix, this.gui.getFont(), this.displayed.get(i + listOffset), (float)xPos, (float)yPos, "");
         if (mouseX + this.gui.getGuiLeft() >= xPos
            && mouseX + this.gui.getGuiLeft() <= xPos + 16
            && mouseY + this.gui.getGuiTop() >= yPos
            && mouseY + this.gui.getGuiTop() <= yPos + 16) {
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, false);
            int slotColor = this.gui.getSlotColor(0);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.gui.drawColoredRegion(matrix, (float)xPos, (float)yPos, 16.0F, 16.0F, slotColor);
            RenderSystem.defaultBlendFunc();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(false);
         }
      }

      Lighting.setupFor3DItems();
   }

   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      matrix.translate(0.0, 0.0, 100.0);
      if (this.ghost != null) {
         this.gui
            .drawColoredRegion(
               matrix, (float)this.ghost.getX(), (float)this.ghost.getY(), (float)this.ghost.getWidth(), (float)this.ghost.getHeight(), -2130720768
            );
      }

      for (Slot slot : ((IC2Container)this.gui.getMenu()).slots) {
         if (slot.isActive() && slot instanceof IGhostSlot) {
            IGhostSlot extra = (IGhostSlot)slot;
            if (extra.getType() == (this.helper ? IGhostSlot.GhostType.NORMAL : IGhostSlot.GhostType.FILTER)
               && (this.ghost == null || slot.x - 2 != this.ghost.getX() || slot.y - 2 != this.ghost.getY())) {
               this.gui.drawColoredRegion(matrix, (float)(slot.x - 2), (float)(slot.y - 2), 20.0F, 20.0F, -2145473563);
            }
         }
      }

      matrix.translate(0.0, 0.0, -100.0);
      RenderSystem.defaultBlendFunc();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      int listOffset = this.slider.getCurrent();

      for (int i = 0; i < 25 && this.displayed.size() > i + listOffset; i++) {
         int xPos = -113 + 18 * (i % 5) + this.offsets.getX();
         int yPos = 37 + 18 * (i / 5) + this.offsets.getY();
         if (mouseX >= xPos && mouseX <= xPos + 16 && mouseY >= yPos && mouseY <= yPos + 16) {
            for (Component comp : this.displayed.get(i + listOffset).getTooltipLines(this.gui.getPlayer(), Default.NORMAL)) {
               tooltips.accept(comp);
            }
         }
      }

      for (Slot slot : ((IC2Container)this.gui.getMenu()).slots) {
         if (slot.isActive() && slot instanceof IGhostSlot) {
            IGhostSlot extra = (IGhostSlot)slot;
            if (extra.getType() == (this.helper ? IGhostSlot.GhostType.NORMAL : IGhostSlot.GhostType.FILTER)
               && slot.x <= mouseX
               && slot.x + 20 >= mouseX
               && slot.y <= mouseY
               && slot.y + 20 >= mouseY) {
               if (this.ghost != null && slot.x - 2 == this.ghost.getX() && slot.y - 2 == this.ghost.getY()) {
                  tooltips.accept(SELECTED);
               } else {
                  tooltips.accept(CONFIGURE);
               }
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (this.box.isInBox(mouseX, mouseY)) {
         if (this.ghost != null && !this.helper) {
            int listOffset = this.slider.getCurrent();

            for (int i = 0; i < 25 && this.displayed.size() > i + listOffset; i++) {
               int xPos = this.box.getX() + 6 + 18 * (i % 5);
               int yPos = this.box.getY() + 37 + 18 * (i / 5);
               if (mouseX >= xPos && mouseX <= xPos + 18 && mouseY >= yPos && mouseY <= yPos + 18) {
                  this.ghost.accept(this.displayed.get(i + listOffset));
                  this.toggleHelper(false);
                  return true;
               }
            }
         }

         return false;
      } else if (this.gui.isHoveringOpenerButton(mouseX, mouseY)) {
         return false;
      } else {
         Slot slot = this.gui.getSlotUnderMouse();
         if (slot instanceof IGhostSlot type
            && (
               type.getType() == IGhostSlot.GhostType.FILTER && !slot.hasItem() && ((IC2Container)this.gui.getMenu()).getCarried().isEmpty()
                  || this.helper && type.getType() == IGhostSlot.GhostType.NORMAL
            )) {
            return false;
         }

         if (this.isVisible() && !this.helper) {
            this.toggleHelper(false);
            return true;
         } else {
            return this.isVisible();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      if (this.slider.isMouseOver(mouseX, mouseY)) {
         return false;
      } else {
         if (this.box.isInBox(mouseX, mouseY)) {
            this.slider.onMouseScroll(mouseX, mouseY, scroll);
         }

         return false;
      }
   }

   public void updateValues(String text) {
      text = text.toLowerCase(Locale.ROOT);
      if (!text.equalsIgnoreCase(this.lastText)) {
         this.lastText = text;
         this.displayed.clear();
         int i = 0;

         for (int m = this.allValid.size(); i < m; i++) {
            if (this.allValidText.get(i).contains(text)) {
               this.displayed.add(this.allValid.get(i));
            }
         }

         this.slider.setMax(this.displayed.size());
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      if (this.isVisible()) {
         if (keyCode == 69 && this.gui.getButton(this.helper ? 1001 : 1000).isFocused()) {
            return true;
         }

         if (keyCode == 256) {
            this.setVisible(false);
            this.slider.setVisible(false);
            this.gui.getButton(this.helper ? 1001 : 1000).visible = false;
            return true;
         }
      }

      return super.onKeyTyped(keyCode);
   }

   @OnlyIn(Dist.CLIENT)
   public void toggleHelper(boolean value) {
      if (!this.blockClosing) {
         this.gui.closeAllComponents();
      } else if (!value) {
         this.setGhost(null);
      }

      this.setVisible(value);
      this.slider.setVisible(value);
      this.gui.getButton(this.helper ? 1001 : 1000).visible = value;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void closeComponent(IC2Screen screen) {
      this.setVisible(false);
      this.slider.setVisible(false);
      this.gui.getButton(this.helper ? 1001 : 1000).visible = false;
      this.setGhost(null);
   }

   public static class ActivatorComponent extends GuiWidget {
      FilterComponent comp;

      public ActivatorComponent(FilterComponent comp) {
         super(Box2i.EMPTY_BOX);
         this.comp = comp;
      }

      @Override
      public boolean isMouseOver(int x, int y) {
         return !this.comp.helper || this.comp.isVisible();
      }

      @Override
      protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
         requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
         Slot slot = this.gui.getSlotUnderMouse();
         if (slot instanceof IGhostSlot type
            && (
               type.getType() == IGhostSlot.GhostType.FILTER && !slot.hasItem() && ((IC2Container)this.gui.getMenu()).getCarried().isEmpty()
                  || this.comp.helper && type.getType() == IGhostSlot.GhostType.NORMAL
            )) {
            this.comp.setGhost(IFilterTarget.ghost((IGhostSlot)slot));
            return true;
         }

         return super.onMouseClick(mouseX, mouseY, mouseButton);
      }
   }
}
