package ic2.core.inventory.gui;

import java.util.List;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public class IC2Font extends Font {
   Font font;

   public IC2Font(Font font) {
      super(null, false);
      this.font = font;
   }

   @Override
   public String bidirectionalShaping(String text) {
      return this.font.bidirectionalShaping(text);
   }

   @Override
   public int drawInBatch(
      String text,
      float x,
      float y,
      int color,
      boolean dropShadow,
      Matrix4f matrix,
      MultiBufferSource buffer,
      Font.DisplayMode displayMode,
      int backgroundColor,
      int packedLight
   ) {
      return this.font.drawInBatch(text, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight);
   }

   @Override
   public int drawInBatch(
      String text,
      float x,
      float y,
      int color,
      boolean dropShadow,
      Matrix4f matrix,
      MultiBufferSource buffer,
      Font.DisplayMode displayMode,
      int backgroundColor,
      int packedLight,
      boolean transparent
   ) {
      return this.font.drawInBatch(text, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight, transparent);
   }

   @Override
   public int drawInBatch(
      Component text,
      float x,
      float y,
      int color,
      boolean dropShadow,
      Matrix4f matrix,
      MultiBufferSource buffer,
      Font.DisplayMode displayMode,
      int backgroundColor,
      int packedLight
   ) {
      return this.font.drawInBatch(text, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight);
   }

   @Override
   public int drawInBatch(
      FormattedCharSequence text,
      float x,
      float y,
      int color,
      boolean dropShadow,
      Matrix4f matrix,
      MultiBufferSource buffer,
      Font.DisplayMode displayMode,
      int backgroundColor,
      int packedLight
   ) {
      return this.font.drawInBatch(text, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight);
   }

   @Override
   public void drawInBatch8xOutline(
      FormattedCharSequence text,
      float x,
      float y,
      int color,
      int outlineColor,
      Matrix4f matrix,
      MultiBufferSource buffer,
      int packedLight
   ) {
      this.font.drawInBatch8xOutline(text, x, y, color, outlineColor, matrix, buffer, packedLight);
   }

   @Override
   public int width(String text) {
      return this.font.width(text);
   }

   @Override
   public int width(FormattedText text) {
      return this.font.width(text);
   }

   @Override
   public int width(FormattedCharSequence text) {
      return this.font.width(text);
   }

   @Override
   public String plainSubstrByWidth(String text, int maxWidth, boolean tail) {
      return this.font.plainSubstrByWidth(text, maxWidth, tail);
   }

   @Override
   public String plainSubstrByWidth(String text, int maxWidth) {
      return this.font.plainSubstrByWidth(text, maxWidth);
   }

   @Override
   public FormattedText substrByWidth(FormattedText text, int maxWidth) {
      return this.font.substrByWidth(text, maxWidth);
   }

   @Override
   public int wordWrapHeight(String text, int maxWidth) {
      return this.font.wordWrapHeight(text, maxWidth);
   }

   @Override
   public int wordWrapHeight(FormattedText text, int maxWidth) {
      return this.font.wordWrapHeight(text, maxWidth);
   }

   @Override
   public List<FormattedCharSequence> split(FormattedText text, int maxWidth) {
      return this.font.split(text, maxWidth);
   }

   @Override
   public boolean isBidirectional() {
      return this.font.isBidirectional();
   }

   @Override
   public StringSplitter getSplitter() {
      return this.font.getSplitter();
   }
}
