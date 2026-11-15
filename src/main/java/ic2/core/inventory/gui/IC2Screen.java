package ic2.core.inventory.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4f;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.components.base.OpenerButton;
import ic2.core.inventory.gui.feature.ITooltipProvider;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.platform.corehacks.mixins.client.ScreenMixin;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Vec2i;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent.Pre;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

@OnlyIn(Dist.CLIENT)
public class IC2Screen extends AbstractContainerScreen<IC2Container> {
   public static final int DEFAULT_TEXT_COLOR = 4210752;
   public static final int SHOW_PLAYER_INVENTORY_NAME = 1;
   public static final int SHOW_CONTAINER_NAME = 2;
   public static final int SHOW_NOT_DISPLAY_TOOLTIPS = 4;
   public static final int RELOAD_GUI = 8;
   public static final int RESIZE_GUI = 16;
   public static final int IGNORE_GUI_BOUNDS = 32;
   public static final int UPDATE_TEXTURE = 64;
   public static final int RENDER_TOOLTIP = 128;
   public static long MAX_TOOLTIP_DELAY = 200L;
   public GuiGraphics guiGraphics;
   int blitOffset = 0;

   public int getBlitOffset() {
      return this.blitOffset;
   }

   public void setBlitOffset(int blitOffset) {
      this.blitOffset = blitOffset;
   }

   private static final IC2Screen.ScissorsStack STACK = new IC2Screen.ScissorsStack();
   ResourceLocation texture;
   AbstractTexture textureInstance;
   List<ITooltipProvider> tooltips = CollectionUtils.<ITooltipProvider>createList();
   List<AbstractWidget> expansionButtons = CollectionUtils.<AbstractWidget>createList();
   Int2ObjectMap<AbstractWidget> widgets = new Int2ObjectOpenHashMap();
   Int2IntMap slotColors = new Int2IntOpenHashMap();
   int flags = 0;
   int clock = 0;
   int lastMouseX = 0;
   int lastMouseY = 0;
   long lastCheck = 0L;
   int lastDrawnToolTipAmount = 0;
   Component guiName;
   Vec2i nameOffset = new Vec2i();
   Vec2i mcNameOffset = new Vec2i();

   public IC2Screen(IC2Container screenContainer, Inventory inv, Component titleIn) {
      super(screenContainer, inv, titleIn);
      this.setFlag(3);
      this.guiName = titleIn;
      this.slotColors.defaultReturnValue(-2130706433);
   }

   public <U extends AbstractWidget> U addRenderableWidget(int index, U widget) {
      this.widgets.put(index, widget);
      return this.addRenderableWidget(widget);
   }

   public <U extends GuiEventListener & Renderable & NarratableEntry> U addRenderableWidget(U widget) {
      if (widget instanceof ITooltipProvider) {
         this.tooltips.add((ITooltipProvider)widget);
      }

      if (widget instanceof OpenerButton) {
         this.expansionButtons.add((AbstractWidget)widget);
      }

      return (U)super.addRenderableWidget(widget);
   }

   public void addOpenerWidget(int index) {
      AbstractWidget widget = (AbstractWidget)this.widgets.get(index);
      if (widget != null) {
         this.expansionButtons.add(widget);
      }
   }

   public void setTexture(ResourceLocation texture) {
      this.texture = texture;
      this.textureInstance = null;
   }

   public void setFlag(int flag) {
      this.flags |= flag;
   }

   public void clearFlag(int flag) {
      this.flags &= ~flag;
   }

   public boolean hasFlag(int flag) {
      return (this.flags & flag) == flag;
   }

   public void hideSlot(int... slotIndexes) {
      for (int slotIndex : slotIndexes) {
         this.slotColors.put(slotIndex, 0);
      }
   }

   public void setCustomHoverColor(int color, int... slotIndexes) {
      for (int slotIndex : slotIndexes) {
         this.slotColors.put(slotIndex, color);
      }
   }

   public void removeSlotColors(int... slotIndexes) {
      for (int slotIndex : slotIndexes) {
         this.slotColors.remove(slotIndex);
      }
   }

   protected void init() {
      super.init();
      this.clearWidgets();
      this.widgets.clear();
      this.tooltips.clear();

      for (Slot slot : ((IC2Container)this.menu).slots) {
         if (slot instanceof ITooltipProvider) {
            this.tooltips.add((ITooltipProvider)slot);
         }
      }

      MinecraftForge.EVENT_BUS.register(this);
   }

   public void removed() {
      MinecraftForge.EVENT_BUS.unregister(this);
      super.removed();
   }

   @SubscribeEvent
   public void onTooltip(Pre event) {
      if (this.lastDrawnToolTipAmount > 0) {
         event.setY(event.getY() + this.lastDrawnToolTipAmount * 10 + 7 + (this.lastDrawnToolTipAmount == 1 ? -1 : 0));
      }
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.guiGraphics = guiGraphics;
      PoseStack matrixStack = guiGraphics.pose();
      if (this.hasFlag(16)) {
         this.clearFlag(16);
         this.minecraft.resizeDisplay();
      }

      if (this.hasFlag(8)) {
         this.clearFlag(8);
         this.init();
      }

      super.render(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
      this.onPostDraw(matrixStack, partialTicks, mouseX, mouseY);
      int x = mouseX - this.getGuiLeft();
      int y = mouseX - this.getGuiTop();
      List<FormattedCharSequence> results = CollectionUtils.<FormattedCharSequence>createList();
      if (mouseX != 2147483647 && mouseY != 2147483647) {
         Consumer<Component> listener = T -> results.addAll(this.font.split(T, Math.max(mouseX, this.width - mouseX)));

         for (ITooltipProvider provider : this.tooltips) {
            provider.addToolTip(this, x, y, listener);
         }

         this.onTooltipCollection(matrixStack, mouseX, mouseY, listener);
      }

      if ((this.hasFlag(128) || this.lastMouseX == mouseX && this.lastMouseY == mouseY) && !results.isEmpty()) {
         if (System.currentTimeMillis() - this.lastCheck >= MAX_TOOLTIP_DELAY) {
            this.setFlag(128);
            this.lastDrawnToolTipAmount = 0;
            guiGraphics.renderTooltip(this.font, results, mouseX, mouseY);
            this.lastDrawnToolTipAmount = results.size();
         } else {
            this.lastDrawnToolTipAmount = 0;
         }
      } else {
         this.lastCheck = System.currentTimeMillis();
         this.lastMouseX = mouseX;
         this.lastMouseY = mouseY;
         if (results.isEmpty()) {
            this.clearFlag(128);
         }

         this.lastDrawnToolTipAmount = 0;
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
      this.guiGraphics = guiGraphics;
      this.renderBackground(guiGraphics);
      this.bindDefaultTexture();
      this.drawTextureRegion(guiGraphics.pose(), (float)this.getGuiLeft(), (float)this.getGuiTop(), 0.0F, 0.0F, (float)this.getXSize(), (float)this.getYSize());
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.guiGraphics = guiGraphics;
      if (this.hasFlag(1)) {
         guiGraphics.drawString(
            this.font,
            this.playerInventoryTitle,
            (8 + this.mcNameOffset.getX()),
            (this.imageHeight - 92 + this.mcNameOffset.getY()),
            4210752,
            false
         );
      }

      if (this.hasFlag(2)) {
         guiGraphics.drawString(
            this.font,
            this.guiName,
            (this.imageWidth / 2 + this.nameOffset.getX() - this.font.width(this.guiName) / 2),
            (6 + this.nameOffset.getY()),
            4210752,
            false
         );
      }
   }

   public void onPostDraw(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
   }

   public void onTooltipCollection(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
   }

   public void containerTick() {
      this.clock++;
      super.containerTick();
   }

   public void closeAllComponents() {
   }

   public boolean isHoveringOpenerButton(int mouseX, int mouseY) {
      mouseX += this.leftPos;
      mouseY += this.topPos;
      int i = 0;

      for (int m = this.expansionButtons.size(); i < m; i++) {
         if (this.expansionButtons.get(i).isMouseOver((double)mouseX, (double)mouseY)) {
            return true;
         }
      }

      return false;
   }

   public boolean isOverArea(int mouseX, int mouseY, int minX, int minY, int width, int height) {
      return mouseX >= minX && mouseY >= minY && mouseX <= minX + width && mouseY <= minY + height;
   }

   protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
      return this.hasFlag(32) ? false : super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton);
   }

   public static void pushScissors(int x, int y, int width, int height) {
      pushScissors(new IC2Screen.Rect(x, y, width, height));
   }

   public static void pushScissors(IC2Screen.Rect rect) {
      STACK.push(rect);
      applyScissors(rect);
   }

   public static void popScissors() {
      applyScissors(STACK.pop());
   }

   private static void applyScissors(IC2Screen.Rect rect) {
      if (rect == null) {
         RenderSystem.disableScissor();
      } else {
         Window window = Minecraft.getInstance().getWindow();
         int bottom = rect.maxY;
         double scaledHeight = (double)window.getHeight() / (double)window.getGuiScaledHeight();
         double scaledWidth = (double)window.getWidth() / (double)window.getGuiScaledWidth();
         RenderSystem.enableScissor(
            (int)((double)rect.getX() * scaledWidth),
            (int)((double)window.getHeight() - (double)bottom * scaledHeight),
            (int)((double)rect.getWidth() * scaledWidth),
            (int)((double)rect.getHeigth() * scaledHeight)
         );
      }
   }

   public void drawString(PoseStack matrixStackIn, Component text, int x, int y, int color) {
      GuiGraphics gg = this.guiGraphics != null ? this.guiGraphics : new GuiGraphics(this.minecraft, this.minecraft.renderBuffers().bufferSource());
      gg.drawString(this.font, text, x, y, color, false);
   }

   public void drawCenterString(PoseStack matrixStackIn, Component text, int x, int y, int color) {
      GuiGraphics gg = this.guiGraphics != null ? this.guiGraphics : new GuiGraphics(this.minecraft, this.minecraft.renderBuffers().bufferSource());
      gg.drawString(this.font, text, x - this.font.width(text) / 2, y, color, false);
   }

   public void drawRightString(PoseStack matrixStackIn, Component text, int x, int y, int color) {
      GuiGraphics gg = this.guiGraphics != null ? this.guiGraphics : new GuiGraphics(this.minecraft, this.minecraft.renderBuffers().bufferSource());
      gg.drawString(this.font, text, x - this.font.width(text), y, color, false);
   }

   public void drawString(PoseStack matrixStackIn, FormattedCharSequence text, int x, int y, int color) {
      GuiGraphics gg = this.guiGraphics != null ? this.guiGraphics : new GuiGraphics(this.minecraft, this.minecraft.renderBuffers().bufferSource());
      gg.drawString(this.font, text, x, y, color, false);
   }

   public void drawCenterString(PoseStack matrixStackIn, FormattedCharSequence text, int x, int y, int color) {
      GuiGraphics gg = this.guiGraphics != null ? this.guiGraphics : new GuiGraphics(this.minecraft, this.minecraft.renderBuffers().bufferSource());
      gg.drawString(this.font, text, x - this.font.width(text) / 2, y, color, false);
   }

   public void drawRightString(PoseStack matrixStackIn, FormattedCharSequence text, int x, int y, int color) {
      GuiGraphics gg = this.guiGraphics != null ? this.guiGraphics : new GuiGraphics(this.minecraft, this.minecraft.renderBuffers().bufferSource());
      gg.drawString(this.font, text, x - this.font.width(text), y, color, false);
   }

   public void drawItemStack(PoseStack matrix, float x, float y, ItemStack stack) {
      this.drawItemStack(matrix, x, y, stack, 20.0F, 20.0F);
   }

   public void drawItemStack(PoseStack matrix, float x, float y, ItemStack stack, float width, float height) {
      this.drawItemStack(matrix, x, y, stack, width, height, false, null);
   }

   public void drawItemStackWithDeco(PoseStack matrix, float x, float y, ItemStack stack) {
      this.drawItemStack(matrix, x, y, stack, 20.0F, 20.0F, true, null);
   }

   public void drawItemStack(PoseStack matrix, float x, float y, ItemStack stack, float width, float height, boolean durability, String text) {
      GuiGraphics gg = this.guiGraphics != null ? this.guiGraphics : new GuiGraphics(this.minecraft, this.minecraft.renderBuffers().bufferSource());
      PoseStack pose = gg.pose();
      pose.pushPose();
      pose.translate((double)x, (double)y, 0.0);
      float xScale = 20.0F / width;
      float yScale = 20.0F / height;
      pose.scale(1.0F / xScale, 1.0F / yScale, 1.0F);
      gg.renderItem(stack, 2, 2);
      if (durability) {
         gg.renderItemDecorations(this.font, stack, 2, 2, text);
      }
      pose.popPose();
   }

   public void drawTextureRegion(PoseStack stack, float x, float y, float texX, float texY, float width, float height) {
      this.drawTextureRegion(stack, x, y, texX, texY, width, height, width, height);
   }

   public void drawTextureRegion(PoseStack stack, float x, float y, float texX, float texY, float width, float height, int color) {
      Matrix4f matrix = stack.last().pose();
      float maxX = x + width;
      float maxY = y + height;
      float t_minX = texX / 256.0F;
      float t_minY = texY / 256.0F;
      float t_maxX = (texX + width) / 256.0F;
      float t_maxY = (texY + height) / 256.0F;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
      bufferbuilder.vertex(matrix, x, maxY, (float)this.getBlitOffset()).color(color).uv(t_minX, t_maxY).endVertex();
      bufferbuilder.vertex(matrix, maxX, maxY, (float)this.getBlitOffset()).color(color).uv(t_maxX, t_maxY).endVertex();
      bufferbuilder.vertex(matrix, maxX, y, (float)this.getBlitOffset()).color(color).uv(t_maxX, t_minY).endVertex();
      bufferbuilder.vertex(matrix, x, y, (float)this.getBlitOffset()).color(color).uv(t_minX, t_minY).endVertex();
      tesselator.end();
   }

   public void drawTextureRegion(PoseStack stack, float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight) {
      Matrix4f matrix = stack.last().pose();
      float maxX = x + width;
      float maxY = y + height;
      float t_minX = texX / 256.0F;
      float t_minY = texY / 256.0F;
      float t_maxX = (texX + texWidth) / 256.0F;
      float t_maxY = (texY + texHeight) / 256.0F;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bufferbuilder.vertex(matrix, x, maxY, (float)this.getBlitOffset()).uv(t_minX, t_maxY).endVertex();
      bufferbuilder.vertex(matrix, maxX, maxY, (float)this.getBlitOffset()).uv(t_maxX, t_maxY).endVertex();
      bufferbuilder.vertex(matrix, maxX, y, (float)this.getBlitOffset()).uv(t_maxX, t_minY).endVertex();
      bufferbuilder.vertex(matrix, x, y, (float)this.getBlitOffset()).uv(t_minX, t_minY).endVertex();
      tesselator.end();
   }

   public void drawTextureRegion(
      PoseStack stack, float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight, int color
   ) {
      Matrix4f matrix = stack.last().pose();
      float maxX = x + width;
      float maxY = y + height;
      float t_minX = texX / 256.0F;
      float t_minY = texY / 256.0F;
      float t_maxX = (texX + texWidth) / 256.0F;
      float t_maxY = (texY + texHeight) / 256.0F;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
      bufferbuilder.vertex(matrix, x, maxY, (float)this.getBlitOffset()).color(color).uv(t_minX, t_maxY).endVertex();
      bufferbuilder.vertex(matrix, maxX, maxY, (float)this.getBlitOffset()).color(color).uv(t_maxX, t_maxY).endVertex();
      bufferbuilder.vertex(matrix, maxX, y, (float)this.getBlitOffset()).color(color).uv(t_maxX, t_minY).endVertex();
      bufferbuilder.vertex(matrix, x, y, (float)this.getBlitOffset()).color(color).uv(t_minX, t_minY).endVertex();
      tesselator.end();
   }

   public static void drawTexture(PoseStack stack, float x, float y, float z, TextureAtlasSprite sprite, float width, float height) {
      Matrix4f matrix = stack.last().pose();
      float maxX = x + width;
      float maxY = y + height;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bufferbuilder.vertex(matrix, x, maxY, z).uv(sprite.getU0(), sprite.getV1()).endVertex();
      bufferbuilder.vertex(matrix, maxX, maxY, z).uv(sprite.getU1(), sprite.getV1()).endVertex();
      bufferbuilder.vertex(matrix, maxX, y, z).uv(sprite.getU1(), sprite.getV0()).endVertex();
      bufferbuilder.vertex(matrix, x, y, z).uv(sprite.getU0(), sprite.getV0()).endVertex();
      tesselator.end();
   }

   public static void drawTexture(PoseStack stack, float x, float y, float z, TextureAtlasSprite sprite, int color, float width, float height) {
      Matrix4f matrix = stack.last().pose();
      float maxX = x + width;
      float maxY = y + height;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
      bufferbuilder.vertex(matrix, x, maxY, z).color(color).uv(sprite.getU0(), sprite.getV1()).endVertex();
      bufferbuilder.vertex(matrix, maxX, maxY, z).color(color).uv(sprite.getU1(), sprite.getV1()).endVertex();
      bufferbuilder.vertex(matrix, maxX, y, z).color(color).uv(sprite.getU1(), sprite.getV0()).endVertex();
      bufferbuilder.vertex(matrix, x, y, z).color(color).uv(sprite.getU0(), sprite.getV0()).endVertex();
      tesselator.end();
   }

   public static void blit(PoseStack stack, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int texWidth, int texHeight) {
      Matrix4f matrix = stack.last().pose();
      float maxX = (float)(x + width);
      float maxY = (float)(y + height);
      float t_minX = u / (float)texWidth;
      float t_minY = v / (float)texHeight;
      float t_maxX = (u + (float)uWidth) / (float)texWidth;
      float t_maxY = (v + (float)vHeight) / (float)texHeight;
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bufferbuilder.vertex(matrix, (float)x, maxY, 0.0F).uv(t_minX, t_maxY).endVertex();
      bufferbuilder.vertex(matrix, maxX, maxY, 0.0F).uv(t_maxX, t_maxY).endVertex();
      bufferbuilder.vertex(matrix, maxX, (float)y, 0.0F).uv(t_maxX, t_minY).endVertex();
      bufferbuilder.vertex(matrix, (float)x, (float)y, 0.0F).uv(t_minX, t_minY).endVertex();
      tesselator.end();
   }

   public void drawColoredRegion(PoseStack stack, float x, float y, float width, float height, int color) {
      Matrix4f matrix = stack.last().pose();
      RenderSystem.defaultBlendFunc();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      bufferbuilder.vertex(matrix, x, y + height, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y + height, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x, y, (float)this.getBlitOffset()).color(color).endVertex();
      tesselator.end();
      RenderSystem.disableBlend();
   }

   public void drawColorFrame(PoseStack stack, float x, float y, float width, float height, int color) {
      Matrix4f matrix = stack.last().pose();
      RenderSystem.defaultBlendFunc();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      bufferbuilder.begin(Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
      bufferbuilder.vertex(matrix, x, y, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y + height, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y + height, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x, y + height, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x, y + height, (float)this.getBlitOffset()).color(color).endVertex();
      bufferbuilder.vertex(matrix, x, y, (float)this.getBlitOffset()).color(color).endVertex();
      tesselator.end();
      RenderSystem.disableBlend();
   }

   public static void drawFrame(PoseStack stack, float x, float y, float width, float height, int color) {
      Matrix4f matrix = stack.last().pose();
      RenderSystem.defaultBlendFunc();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      bufferbuilder.begin(Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
      bufferbuilder.vertex(matrix, x, y, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y + height, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix, x + width, y + height, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix, x, y + height, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix, x, y + height, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix, x, y, 0.0F).color(color).endVertex();
      tesselator.end();
      RenderSystem.disableBlend();
   }

   public void drawGradientRegionHorizontal(PoseStack stack, float x, float y, float width, float height, int leftColor, int rightColor) {
      Matrix4f matrix = stack.last().pose();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      bufferbuilder.vertex(matrix, x, y + height, (float)this.getBlitOffset()).color(leftColor | 0xFF000000).endVertex();
      bufferbuilder.vertex(matrix, x + width, y + height, (float)this.getBlitOffset()).color(rightColor | 0xFF000000).endVertex();
      bufferbuilder.vertex(matrix, x + width, y, (float)this.getBlitOffset()).color(rightColor | 0xFF000000).endVertex();
      bufferbuilder.vertex(matrix, x, y, (float)this.getBlitOffset()).color(leftColor | 0xFF000000).endVertex();
      tesselator.end();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   public void drawGradientRegionVertical(PoseStack stack, float x, float y, float width, float height, int topColor, int bottomColor) {
      Matrix4f matrix = stack.last().pose();
      RenderSystem.defaultBlendFunc();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      bufferbuilder.vertex(matrix, x, y + height, (float)this.getBlitOffset()).color(bottomColor | 0xFF000000).endVertex();
      bufferbuilder.vertex(matrix, x + width, y + height, (float)this.getBlitOffset()).color(bottomColor | 0xFF000000).endVertex();
      bufferbuilder.vertex(matrix, x + width, y, (float)this.getBlitOffset()).color(topColor | 0xFF000000).endVertex();
      bufferbuilder.vertex(matrix, x, y, (float)this.getBlitOffset()).color(topColor | 0xFF000000).endVertex();
      tesselator.end();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   public void makeSlotFunction(PoseStack matrix, Slot slot) {
      int i = slot.x + this.leftPos;
      int j = slot.y + this.topPos;
      this.setBlitOffset(100);
      GuiGraphics gg = this.guiGraphics != null ? this.guiGraphics : new GuiGraphics(this.minecraft, this.minecraft.renderBuffers().bufferSource());
      ItemStack stack = slot.getItem();
      if (stack.isEmpty() && slot.isActive()) {
         Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
         if (pair != null) {
            TextureAtlasSprite sprite = (TextureAtlasSprite)this.minecraft.getTextureAtlas((ResourceLocation)pair.getFirst()).apply((ResourceLocation)pair.getSecond());
            gg.blit(i, j, this.getBlitOffset(), 16, 16, sprite);
         }
      } else {
         gg.renderItem(stack, i, j);
         gg.renderItemDecorations(this.font, stack, i, j, null);
      }
      this.setBlitOffset(0);
   }

   @Override
   protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (!this.hasFlag(4) || !(this.getSlotUnderMouse() instanceof LockedSlot)) {
         super.renderTooltip(guiGraphics, mouseX, mouseY);
      }
   }

   public void renderComponentHoverEffect(GuiGraphics guiGraphics, @Nullable Style style, int mouseX, int mouseY) {
      guiGraphics.renderComponentHoverEffect(this.font, style, mouseX, mouseY);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int p_231044_5_) {
      this.lastCheck = System.currentTimeMillis();
      return super.mouseClicked(mouseX, mouseY, p_231044_5_);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.lastCheck = System.currentTimeMillis();
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public void bindDefaultTexture() {
      RenderSystem.setShaderTexture(0, this.getActiveTexture().getId());
   }

   public AbstractTexture getActiveTexture() {
      if (this.textureInstance == null) {
         this.textureInstance = this.getTexture(this.texture);
      }

      return this.textureInstance;
   }

   public AbstractTexture getTexture(ResourceLocation location) {
      return this.minecraft.getTextureManager().getTexture(location);
   }

   public void bindTexture(ResourceLocation location) {
      RenderSystem.setShaderTexture(0, this.getTexture(location).getId());
   }

   public void preventDoubleClick() {
      ((ScreenMixin)this).setLastClickButton(0);
   }

   public void setPlayerInventoryOffset(int x, int y) {
      this.mcNameOffset.setX(x);
      this.mcNameOffset.setY(y);
   }

   public void setContainerOffset(int x, int y) {
      this.nameOffset.setX(x);
      this.nameOffset.setY(y);
   }

   public void modifySize(int width, int height) {
      this.imageWidth += width;
      this.imageHeight += height;
   }

   public void setMaxSize(int width, int height) {
      this.imageWidth = width;
      this.imageHeight = height;
   }

   public void setXSize(int width) {
      this.imageWidth = width;
   }

   public void setYSize(int height) {
      this.imageHeight = height;
   }

   public void setGuiName(Component guiName) {
      this.guiName = guiName;
   }

   public Component getTitle() {
      return this.guiName;
   }

   public void setRepeatingKeys(boolean value) {
   }

   public int getZLevel() {
      return this.getBlitOffset();
   }

   public void setZLevel(int z) {
      this.setBlitOffset(z);
   }

   public LocalPlayer getPlayer() {
      return this.minecraft.player;
   }

   public UUID getPlayerID() {
      return this.getPlayer().getUUID();
   }

   public ResourceLocation getGuiTexture() {
      return this.texture;
   }

   public AbstractTexture getTextureObject() {
      return this.textureInstance;
   }

   public ItemRenderer getRenderItem() {
      return this.minecraft.getItemRenderer();
   }

   public Font getFont() {
      return this.font;
   }

   public int getClock() {
      return this.clock;
   }

   public boolean isSlotDragging() {
      return this.isQuickCrafting;
   }

   public AbstractWidget getButton(int id) {
      return (AbstractWidget)this.widgets.get(id);
   }

   public <K extends AbstractWidget> K getCastedButton(int id, Class<K> clz) {
      return (K)this.widgets.get(id);
   }

   public boolean hasButton(int id) {
      return this.widgets.containsKey(id);
   }

   public Int2ObjectMap<AbstractWidget> getWidgets() {
      return this.widgets;
   }

   public void removeButtons(Predicate<AbstractWidget> widgets) {
      this.children().removeIf(T -> T instanceof AbstractWidget && widgets.test((AbstractWidget)T));
      this.renderables.removeIf(T -> T instanceof AbstractWidget && widgets.test((AbstractWidget)T));
   }

   public int getSlotColor(int index) {
      return this.slotColors.get(index);
   }

   public boolean isSlotHovered(Slot slot, int mouseX, int mouseY) {
      return this.isHovering(slot.x, slot.y, 16, 16, (double)(mouseX + this.leftPos), (double)(mouseY + this.topPos));
   }

   public <K extends AbstractContainerMenu> K getCastedContainer(Class<K> clz) {
      return (K)(clz.isInstance(this.menu) ? this.menu : null);
   }

   public static Style getComponentNew(List<FormattedCharSequence> list, int mouseX, int mouseY) {
      int yLevel = 9;
      if (mouseX >= 0 && mouseY >= 0) {
         int index;
         for (index = 0; yLevel <= mouseY; index++) {
            yLevel += 9;
         }

         return index >= list.size() ? null : Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(list.get(index), mouseX);
      } else {
         return null;
      }
   }

   public <T extends IFluidHandler & IFluidTank> void displayMultiTank(PoseStack matrix, int x, int y, int xOver, int yOver, int ySize, T tank) {
      if (tank.getFluidAmount() > 0) {
         RenderSystem.disableCull();
         x += this.getGuiLeft();
         y += this.getGuiTop() + ySize;
         Function<ResourceLocation, TextureAtlasSprite> map = this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
         this.bindTexture(InventoryMenu.BLOCK_ATLAS);
         float yTotal = 0.0F;
         float yProgress = 0.0F;
         int max = tank.getCapacity();
         int i = 0;

         for (int m = tank.getTanks(); i < m; i++) {
            FluidStack stack = tank.getFluidInTank(i);
            float lvl = stack.isEmpty() ? 0.0F : (float)stack.getAmount() / (float)max * (float)ySize;
            if (!(lvl <= 0.0F)) {
               yTotal += lvl;
               IClientFluidTypeExtensions attr = IClientFluidTypeExtensions.of(stack.getFluid());
               TextureAtlasSprite liquidIcon = map.apply(attr.getStillTexture(stack));
               if (liquidIcon != map.apply(MissingTextureAtlasSprite.getLocation())) {
                  int color = attr.getTintColor(stack);

                  while (yProgress < yTotal) {
                     float bounds = Math.min(16.0F, yTotal - yProgress);
                     drawTexture(matrix, (float)x, (float)y - yProgress - bounds, (float)this.getBlitOffset(), liquidIcon, color, 16.0F, bounds);
                     if (bounds <= 0.0F || yProgress + 1.0F >= yTotal) {
                        break;
                     }

                     yProgress += bounds;
                  }
               }
            }
         }

         this.bindDefaultTexture();
         this.drawTextureRegion(matrix, (float)x, (float)(y - ySize), (float)xOver, (float)yOver, 16.0F, (float)ySize);
      }
   }

   public void displayTank(PoseStack stack, float x, float y, float xOver, float yOver, float ySize, float yMax, FluidStack fluid) {
      this.displayTank(stack, x, y, xOver, yOver, 16.0F, ySize, yMax, fluid);
   }

   public void displayTank(PoseStack stack, float x, float y, float xOver, float yOver, float xSize, float ySize, float yMax, FluidStack fluid) {
      if (!fluid.isEmpty()) {
         x += (float)this.getGuiLeft();
         y += (float)this.getGuiTop();
         Function<ResourceLocation, TextureAtlasSprite> map = this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
         IClientFluidTypeExtensions attr = IClientFluidTypeExtensions.of(fluid.getFluid());
         TextureAtlasSprite liquidIcon = map.apply(attr.getStillTexture(fluid));
         if (liquidIcon != map.apply(MissingTextureAtlasSprite.getLocation())) {
            this.bindTexture(InventoryMenu.BLOCK_ATLAS);
            RenderSystem.disableCull();
            int color = attr.getTintColor(fluid);
            int yTotal = 0;
            y += yMax;

            while ((float)yTotal < ySize) {
               float bounds = Math.min(16.0F, ySize - (float)yTotal);
               drawTexture(stack, x, y - (float)yTotal - bounds, (float)this.getBlitOffset(), liquidIcon, color, xSize, bounds);
               yTotal += Mth.ceil(bounds);
               if (bounds <= 0.0F || (float)yTotal >= ySize) {
                  break;
               }
            }

            this.bindDefaultTexture();
            this.drawTextureRegion(stack, x, y - yMax, xOver, yOver, xSize, yMax);
         }
      }
   }

   public static class Rect {
      int minX;
      int minY;
      int maxX;
      int maxY;

      public Rect(int x, int y, int width, int heigth) {
         this.minX = x;
         this.minY = y;
         this.maxX = x + width;
         this.maxY = y + heigth;
      }

      public void limit(IC2Screen.Rect rect) {
         this.minX = Math.max(rect.minX, this.minX);
         this.minY = Math.max(rect.minY, this.minY);
         this.maxX = Math.min(rect.maxX, this.maxX);
         this.maxY = Math.min(rect.maxY, this.maxY);
      }

      public int getX() {
         return this.minX;
      }

      public int getY() {
         return this.minY;
      }

      public int getWidth() {
         return this.maxX - this.minX;
      }

      public int getHeigth() {
         return this.maxY - this.minY;
      }
   }

   public static class ScissorsStack {
      Deque<IC2Screen.Rect> stack = new ArrayDeque<>();

      public void push(IC2Screen.Rect owner) {
         if (this.stack.isEmpty()) {
            this.stack.push(owner);
         } else {
            owner.limit(this.stack.peek());
         }
      }

      public IC2Screen.Rect pop() {
         this.stack.pop();
         return this.stack.peek();
      }
   }
}
