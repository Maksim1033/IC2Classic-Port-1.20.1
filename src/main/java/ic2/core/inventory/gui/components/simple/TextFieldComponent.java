package ic2.core.inventory.gui.components.simple;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager.LogicOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import org.joml.Matrix4f;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class TextFieldComponent extends GuiWidget {
   @OnlyIn(Dist.CLIENT)
   TextFieldHelper text;
   Supplier<String> textGetter;
   Consumer<String> textSetter;
   Predicate<String> textValidator;
   int visibleLanes = 0;
   long lastClickTime = 0L;
   int lastPos = -1;
   TextFieldComponent.Cache cache = new TextFieldComponent.Cache();
   boolean dirtyCache = false;
   int offset = 0;
   int tick = 0;
   boolean focused = false;
   boolean canLoseFocus = true;

   public TextFieldComponent(Box2i box, Supplier<String> textGetter, Consumer<String> textSetter, Predicate<String> textValidator) {
      super(box);
      this.textGetter = textGetter;
      this.textSetter = textSetter;
      this.textValidator = textValidator;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.GUI_CLOSE);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
   }

   @Override
   public boolean isMouseOver(int x, int y) {
      return super.isMouseOver(x, y) || this.focused && this.canLoseFocus;
   }

   public int getOffset() {
      return this.offset;
   }

   public int getMaxLanes() {
      TextFieldComponent.Cache cache = this.getCache();
      return cache.lines.length + (cache.cursorAtEnd && cache.cursor.getX() == 0 ? 1 : 0);
   }

   public int getVisibleLanes() {
      return this.visibleLanes;
   }

   public TextFieldComponent setOffset(int offset) {
      this.offset = Mth.clamp(offset, 0, Math.min(Math.max(0, this.getCache().lines.length - this.visibleLanes + 1), offset));
      return this;
   }

   public TextFieldComponent setFocused(boolean value) {
      this.focused = value;
      return this;
   }

   public TextFieldComponent setCanLoseFocus(boolean value) {
      this.canLoseFocus = true;
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      Minecraft mc = gui.getMinecraft();
      this.text = new TextFieldHelper(
         this::getText, this::setText, TextFieldHelper.createClipboardGetter(mc), TextFieldHelper.createClipboardSetter(mc), this.textValidator
      );
      this.dirtyCache = true;
      this.visibleLanes = Mth.floor((float)this.getBox().getHeight() / 9.0F);
      gui.setRepeatingKeys(true);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      this.tick++;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onClose() {
      this.gui.setRepeatingKeys(false);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      TextFieldComponent.Line[] lines = this.getCache().lines;
      matrix.pushPose();
      matrix.translate(0.0, (double)(-this.offset * 9), 0.0);

      for (int i = 0; i < this.visibleLanes && i + this.offset < lines.length; i++) {
         TextFieldComponent.Line line = lines[i + this.offset];
         this.gui.guiGraphics.drawString(this.gui.getFont(), line.asComponent, (int)line.x, (int)line.y, -16777216, false);
      }

      if (this.focused) {
         int length = this.cache.selection.length;
         if (length > 0) {
            int minLine = Math.abs(Math.min(Math.min(this.cache.cursorLine, this.cache.selectionLine) - this.offset, 0));
            int maxLine = Math.abs(Math.max(Math.max(this.cache.cursorLine, this.cache.selectionLine) - (this.offset + this.visibleLanes), 0));
            if (length - minLine - maxLine > 0) {
               this.renderHighlight(matrix, this.cache.selection, minLine, length - maxLine);
            }
         }

         if (this.cache.cursorLine >= this.offset && this.cache.cursorLine - this.visibleLanes <= this.offset) {
            this.renderCursor(matrix, this.cache.localCursor, this.cache.cursorAtEnd);
         }
      }

      matrix.popPose();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      if (!this.focused) {
         return false;
      } else if (keyCode == 69) {
         return true;
      } else if (this.onTextTyped(keyCode)) {
         this.dirtyCache = true;
         return true;
      } else {
         return super.onKeyTyped(keyCode);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onCharTyped(char letter, int letterCode) {
      if (SharedConstants.isAllowedChatCharacter(letter)) {
         this.text.insertText(Character.toString(letter));
         this.dirtyCache = true;
         return true;
      } else {
         return super.onCharTyped(letter, letterCode);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (!super.isMouseOver(mouseX, mouseY) && this.focused && this.canLoseFocus) {
         this.focused = false;
         return false;
      } else {
         this.focused = true;
         if (mouseButton == 0) {
            long clickTime = Util.getMillis();
            TextFieldComponent.Cache cache = this.getCache();
            int pos = cache.getIndexAtPosition(this.gui.getFont(), this.convertScreenToLocal(new Vec2i(mouseX, mouseY)));
            if (pos >= 0) {
               if (pos != this.lastPos || clickTime - this.lastClickTime >= 250L) {
                  this.text.setCursorPos(pos, Screen.hasShiftDown());
               } else if (!this.text.isSelecting()) {
                  this.text
                     .setSelectionRange(
                        StringSplitter.getWordPosition(this.getText(), -1, pos, false), StringSplitter.getWordPosition(this.getText(), 1, pos, false)
                     );
               } else {
                  this.text.selectAll();
               }

               this.dirtyCache = true;
            }

            this.lastPos = pos;
            this.lastClickTime = clickTime;
         }

         return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseDragged(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton == 0) {
         TextFieldComponent.Cache cache = this.getCache();
         int line = cache.getIndexAtPosition(this.gui.getFont(), this.convertScreenToLocal(new Vec2i(mouseX, mouseY)));
         this.text.setCursorPos(line, true);
         this.dirtyCache = true;
      }

      return true;
   }

   @OnlyIn(Dist.CLIENT)
   private boolean onTextTyped(int keyCode) {
      if (Screen.isSelectAll(keyCode)) {
         this.text.selectAll();
         return true;
      } else if (Screen.isCopy(keyCode)) {
         this.text.copy();
         return true;
      } else if (Screen.isPaste(keyCode)) {
         this.text.insertText(TextFieldHelper.getClipboardContents(Minecraft.getInstance()).replaceAll("\t", "   "));
         return true;
      } else if (Screen.isCut(keyCode)) {
         this.text.cut();
         return true;
      } else {
         switch (keyCode) {
            case 257:
            case 335:
               this.text.insertText("\n");
               return true;
            case 258:
               this.text.insertText("   ");
               return true;
            case 259:
               this.text.removeCharsFromCursor(-1);
               return true;
            case 261:
               this.text.removeCharsFromCursor(1);
               return true;
            case 262:
               this.text.moveByChars(1, Screen.hasShiftDown());
               return true;
            case 263:
               this.text.moveByChars(-1, Screen.hasShiftDown());
               return true;
            case 264:
               this.keyDown();
               return true;
            case 265:
               this.keyUp();
               return true;
            case 268:
               this.keyHome();
               return true;
            case 269:
               this.keyEnd();
               return true;
            default:
               return false;
         }
      }
   }

   private void keyUp() {
      this.changeLine(-1);
   }

   private void keyDown() {
      this.changeLine(1);
   }

   private void changeLine(int offset) {
      this.text.setCursorPos(this.getCache().changeLine(this.text.getCursorPos(), offset), Screen.hasShiftDown());
   }

   private void keyHome() {
      this.text.setCursorPos(this.getCache().findLineStart(this.text.getCursorPos()), Screen.hasShiftDown());
   }

   private void keyEnd() {
      this.text.setCursorPos(this.getCache().findLineEnd(this.text.getCursorPos()), Screen.hasShiftDown());
   }

   private String getText() {
      return this.textGetter.get();
   }

   private void setText(String s) {
      this.textSetter.accept(s);
   }

   private TextFieldComponent.Cache getCache() {
      if (this.dirtyCache) {
         this.dirtyCache = false;
         this.rebuildDisplayCache();
      }

      return this.cache;
   }

   private void rebuildDisplayCache() {
      String s = this.getText();
      if (s.isEmpty()) {
         this.cache.clear();
      } else {
         int cursorPos = this.text.getCursorPos();
         int selectedPos = this.text.getSelectionPos();
         IntList lineStarts = new IntArrayList();
         List<TextFieldComponent.Line> lines = CollectionUtils.<TextFieldComponent.Line>createList();
         MutableInt lineCount = new MutableInt();
         MutableBoolean isNewLine = new MutableBoolean();
         StringSplitter splitter = this.gui.getFont().getSplitter();
         splitter.splitLines(
            s,
            this.getBox().getWidth() - 5,
            Style.EMPTY,
            true,
            (style, from, to) -> {
               String cutText = s.substring(from, to);
               isNewLine.setValue(cutText.endsWith("\n"));
               lineStarts.add(from);
               lines.add(
                  new TextFieldComponent.Line(
                     style, StringUtils.stripEnd(cutText, " \n"), this.convertLocalToScreen(new Vec2i(0, lineCount.getAndIncrement() * 9))
                  )
               );
            }
         );
         int[] lineStartArray = lineStarts.toIntArray();
         boolean isEnd = cursorPos == s.length();
         Vec2i cursor;
         if (isEnd && isNewLine.isTrue()) {
            cursor = new Vec2i(0, lines.size() * 9);
         } else {
            int k = this.findLineFromPos(lineStartArray, cursorPos);
            cursor = new Vec2i(this.gui.getFont().width(s.substring(lineStartArray[k], cursorPos)), k * 9);
         }

         List<TextFieldComponent.Bounds> bounds = Lists.newArrayList();
         if (cursorPos != selectedPos) {
            int minPos = Math.min(cursorPos, selectedPos);
            int maxPos = Math.max(cursorPos, selectedPos);
            int minLine = this.findLineFromPos(lineStartArray, minPos);
            int maxLine = this.findLineFromPos(lineStartArray, maxPos);
            if (minLine == maxLine) {
               bounds.add(this.createPartialLineSelection(s, splitter, minPos, maxPos, minLine * 9, lineStartArray[minLine]));
            } else {
               bounds.add(
                  this.createPartialLineSelection(
                     s, splitter, minPos, minLine + 1 > lineStartArray.length ? s.length() : lineStartArray[minLine + 1], minLine * 9, lineStartArray[minLine]
                  )
               );

               for (int line = minLine + 1; line < maxLine; line++) {
                  bounds.add(
                     new TextFieldComponent.Bounds(
                        this.convertLocalToScreen(new Vec2i(0, line * 9)),
                        this.convertLocalToScreen(
                           new Vec2i((int)splitter.stringWidth(s.substring(lineStartArray[line], lineStartArray[line + 1])), line * 9 + 9)
                        )
                     )
                  );
               }

               bounds.add(this.createPartialLineSelection(s, splitter, lineStartArray[maxLine], maxPos, maxLine * 9, lineStartArray[maxLine]));
            }
         }

         this.cache
            .setCache(
               s,
               cursor,
               this.findLineFromPos(lineStartArray, cursorPos),
               this.findLineFromPos(lineStartArray, selectedPos),
               isEnd,
               lineStartArray,
               lines.toArray(TextFieldComponent.Line[]::new),
               bounds.toArray(TextFieldComponent.Bounds[]::new)
            );
         int cursorY = this.findLineFromPos(lineStartArray, cursorPos);
         if (cursorY == lines.size() - 1 && isEnd && isNewLine.isTrue()) {
            cursorY++;
         }

         if (cursorY < this.offset) {
            this.offset = Math.max(0, cursorY);
         } else if (cursorY > 6 && cursorY - 6 > this.offset || isEnd) {
            this.offset = Math.max(0, cursorY - 6);
         }
      }
   }

   private void renderCursor(PoseStack matrix, Vec2i pos, boolean end) {
      if (this.tick / 6 % 2 == 0) {
         if (!end) {
            this.gui.drawColoredRegion(matrix, (float)pos.getX(), (float)(pos.getY() - 1), 1.0F, 10.0F, -16777216);
         } else {
            this.gui.drawString(matrix, Component.literal("_"), pos.getX(), pos.getY(), 0);
         }
      }
   }

   private void renderHighlight(PoseStack matrix, TextFieldComponent.Bounds[] bounds, int start, int end) {
      Tesselator tes = Tesselator.getInstance();
      BufferBuilder buffer = tes.getBuilder();
      RenderSystem.enableDepthTest();
      RenderSystem.enableColorLogicOp();
      RenderSystem.logicOp(LogicOp.OR_REVERSE);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      Matrix4f pos = matrix.last().pose();

      for (int i = start; i < end && i < bounds.length; i++) {
         TextFieldComponent.Bounds bound = bounds[i];
         buffer.vertex(pos, (float)bound.xPos, (float)(bound.yPos + bound.height), 0.0F).color(-16776961).endVertex();
         buffer.vertex(pos, (float)(bound.xPos + bound.width), (float)(bound.yPos + bound.height), 0.0F).color(-16776961).endVertex();
         buffer.vertex(pos, (float)(bound.xPos + bound.width), (float)bound.yPos, 0.0F).color(-16776961).endVertex();
         buffer.vertex(pos, (float)bound.xPos, (float)bound.yPos, 0.0F).color(-16776961).endVertex();
      }

      tes.end();
      RenderSystem.disableColorLogicOp();
      RenderSystem.disableDepthTest();
   }

   @OnlyIn(Dist.CLIENT)
   private Vec2i convertScreenToLocal(Vec2i pos) {
      pos.set(pos.getX() - (this.getBox().getX() + 1), pos.getY() - (this.getBox().getY() + 1));
      return pos;
   }

   @OnlyIn(Dist.CLIENT)
   private Vec2i convertLocalToScreen(Vec2i pos) {
      pos.set(pos.getX() + this.getBox().getX() + 1, pos.getY() + this.getBox().getY() + 1);
      return pos;
   }

   @OnlyIn(Dist.CLIENT)
   private TextFieldComponent.Bounds createPartialLineSelection(String s, StringSplitter characters, int minPos, int maxPos, int minLine, int maxLine) {
      return new TextFieldComponent.Bounds(
         this.convertLocalToScreen(new Vec2i((int)characters.stringWidth(s.substring(maxLine, minPos)), minLine)),
         this.convertLocalToScreen(new Vec2i((int)characters.stringWidth(s.substring(maxLine, maxPos)), minLine + 9))
      );
   }

   private int findLineFromPos(int[] lineStarts, int pos) {
      int i = Arrays.binarySearch(lineStarts, pos);
      return i < 0 ? -(i + 2) : i;
   }

   public static class Bounds {
      int xPos;
      int yPos;
      int width;
      int height;

      public Bounds(Vec2i from, Vec2i to) {
         this.xPos = from.getX();
         this.yPos = from.getY();
         this.width = to.getX() - this.xPos;
         this.height = to.getY() - this.yPos;
      }
   }

   class Cache {
      private String fullText = "";
      private Vec2i cursor = new Vec2i();
      private Vec2i localCursor = new Vec2i();
      private boolean cursorAtEnd = true;
      private int cursorLine = 0;
      private int selectionLine = 0;
      private int[] lineStarts = new int[1];
      private TextFieldComponent.Line[] lines = new TextFieldComponent.Line[]{TextFieldComponent.Line.EMPTY_LINE};
      private TextFieldComponent.Bounds[] selection = new TextFieldComponent.Bounds[0];

      public void setCache(
         String fullText,
         Vec2i cursor,
         int cursorLine,
         int selectionLine,
         boolean cursorAtEnd,
         int[] lineStarts,
         TextFieldComponent.Line[] lines,
         TextFieldComponent.Bounds[] selection
      ) {
         this.fullText = fullText;
         this.cursor.set(cursor);
         this.localCursor.set(cursor);
         TextFieldComponent.this.convertLocalToScreen(this.localCursor);
         this.cursorLine = cursorLine;
         this.selectionLine = selectionLine;
         this.cursorAtEnd = cursorAtEnd;
         this.lineStarts = lineStarts;
         this.lines = lines;
         this.selection = selection;
      }

      public void clear() {
         this.fullText = "";
         this.cursor.set(0, 0);
         this.localCursor.set(0, 0);
         TextFieldComponent.this.convertLocalToScreen(this.localCursor);
         this.cursorLine = 0;
         this.selectionLine = 0;
         this.cursorAtEnd = true;
         this.lineStarts = new int[1];
         this.lines = new TextFieldComponent.Line[]{TextFieldComponent.Line.EMPTY_LINE};
         this.selection = new TextFieldComponent.Bounds[0];
      }

      public int getIndexAtPosition(Font font, Vec2i pos) {
         int line = pos.getY() / 9 + TextFieldComponent.this.offset;
         if (line < 0) {
            return 0;
         } else {
            return line >= this.lines.length
               ? this.fullText.length()
               : this.lineStarts[line] + font.getSplitter().plainIndexAtWidth(this.lines[line].contents, pos.getX(), this.lines[line].style);
         }
      }

      public int changeLine(int x, int y) {
         int offset = TextFieldComponent.this.findLineFromPos(this.lineStarts, x);
         int line = offset + y;
         return 0 <= line && line < this.lineStarts.length
            ? this.lineStarts[line] + Math.min(x - this.lineStarts[offset], this.lines[line].contents.length())
            : x;
      }

      public int findLineStart(int pos) {
         return this.lineStarts[TextFieldComponent.this.findLineFromPos(this.lineStarts, pos)];
      }

      public int findLineEnd(int pos) {
         int line = TextFieldComponent.this.findLineFromPos(this.lineStarts, pos);
         return this.lineStarts[line] + this.lines[line].contents.length();
      }
   }

   public static class Line {
      private static final TextFieldComponent.Line EMPTY_LINE = new TextFieldComponent.Line(Style.EMPTY, "", new Vec2i(0, 0));
      private final Style style;
      private final String contents;
      private final Component asComponent;
      private final int x;
      private final int y;

      public Line(Style style, String contents, Vec2i pos) {
         this.style = style;
         this.contents = contents;
         this.x = pos.getX();
         this.y = pos.getY();
         this.asComponent = Component.literal(contents).setStyle(style);
      }
   }
}
