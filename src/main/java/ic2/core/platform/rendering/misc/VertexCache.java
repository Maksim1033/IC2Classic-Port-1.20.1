package ic2.core.platform.rendering.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VertexCache implements BufferVertexConsumer {
   VertexFormat format;
   VertexFormatElement element;
   ByteBuffer data;
   int writtenBytes;
   int index;
   int vertecies;
   Mode mode;

   public VertexCache(int minVertecies, Mode mode, VertexFormat format) {
      this.format = format;
      this.element = (VertexFormatElement)format.getElements().get(0);
      this.mode = mode;
      this.data = ByteBuffer.allocate(minVertecies * format.getVertexSize()).order(ByteOrder.nativeOrder());
   }

   public int writtenBytes() {
      return this.writtenBytes;
   }

   public int vertexCount() {
      return this.vertecies;
   }

   public boolean hasVertices() {
      return this.vertecies > 0;
   }

   public VertexFormat format() {
      return this.format;
   }

   public void endVertex() {
      this.vertecies++;
      this.ensureCacpacity(this.format.getVertexSize());
      if (this.mode == Mode.LINES || this.mode == Mode.LINE_STRIP) {
         int i = this.format.getVertexSize();
         this.data.put(this.writtenBytes, this.data, this.writtenBytes - i, i);
         this.writtenBytes += i;
         this.vertecies++;
         this.ensureCacpacity(this.format.getVertexSize());
      }
   }

   private void ensureCacpacity(int toAdd) {
      while (this.writtenBytes + toAdd >= this.data.capacity()) {
         this.data = ByteBuffer.allocate(this.data.capacity() * 2).order(ByteOrder.nativeOrder()).put(this.data);
      }
   }

   public void defaultColor(int r, int g, int b, int a) {
   }

   public void unsetDefaultColor() {
   }

   public VertexFormatElement currentElement() {
      return this.element;
   }

   public void nextElement() {
      this.writtenBytes = this.writtenBytes + this.element.getByteSize();
      this.index = ++this.index % this.format.getElements().size();
      this.element = (VertexFormatElement)this.format.getElements().get(this.index);
      if (this.element.getUsage() == Usage.PADDING) {
         this.nextElement();
      }
   }

   public void putByte(int index, byte value) {
      this.data.put(this.writtenBytes + index, value);
   }

   public void putShort(int index, short value) {
      this.data.putShort(this.writtenBytes + index, value);
   }

   public void putFloat(int index, float value) {
      this.data.putFloat(this.writtenBytes + index, value);
   }

   private ByteBuffer prepData() {
      return this.data.position(this.writtenBytes).flip();
   }

   private void cleanupData() {
      this.data.limit(this.data.capacity()).position(0);
   }

   public void copyFrom(VertexCache cache) {
      if (cache.format() == this.format() && cache.writtenBytes >= 0) {
         this.ensureCacpacity(cache.writtenBytes());
         this.data.position(this.writtenBytes).put(cache.prepData());
         this.vertecies = this.vertecies + cache.vertecies;
         this.writtenBytes = this.writtenBytes + cache.writtenBytes;
         cache.cleanupData();
      }
   }

   public void copyFrom(VertexCache cache, int start, int width) {
      if (cache.format() == this.format() && cache.writtenBytes >= 0 && width >= 0) {
         int size = this.format.getVertexSize();
         this.ensureCacpacity((start + width) * size);
         this.data.position(this.writtenBytes).put(cache.prepData().position(start * size).limit((start + width) * size));
         this.vertecies += width;
         this.writtenBytes += width * size;
         cache.cleanupData();
      }
   }

   public void putIntoBuffer(BufferBuilder builder) {
      if (this.writtenBytes != 0) {
         builder.putBulkData(this.prepData());
         this.cleanupData();
      }
   }

   public void putIntoBuffer(BufferBuilder builder, int offset) {
      if (this.writtenBytes != 0) {
         builder.putBulkData(this.prepData().position(offset * this.format.getVertexSize()));
         this.cleanupData();
      }
   }

   public void clear() {
      this.writtenBytes = 0;
      this.vertecies = 0;
   }

   public boolean isEmpty() {
      return this.vertecies <= 0;
   }

   public static class BufferedVertexCache extends VertexCache {
      VertexBuffer buffer;

      public BufferedVertexCache(int minVertecies, Mode mode, VertexFormat format) {
         super(minVertecies, mode, format);
      }

      public void finish() {
         if (this.buffer == null) {
            this.buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
         }

         BufferBuilder builder = Tesselator.getInstance().getBuilder();
         builder.begin(this.mode, this.format);
         this.putIntoBuffer(builder);
         this.buffer.bind();
         this.buffer.upload(builder.end());
      }

      public void draw() {
         if (this.buffer != null) {
            this.buffer.bind();
            this.buffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
            VertexBuffer.unbind();
         }
      }

      public void delete() {
         if (this.buffer != null) {
            this.buffer.close();
            this.buffer = null;
         }
      }
   }
}
