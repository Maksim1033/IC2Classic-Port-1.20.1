package ic2.core.block.rendering.camouflage;

import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.util.DirectionList;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.networking.buffers.InputBuffer;
import ic2.core.networking.buffers.OutputBuffer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CamouflageStorage implements INetworkDataBuffer {
   ColorQuadCache[] colorCache = createColor();
   TextureQuadCache[] textureCache = createTexture();
   boolean[] useColor = new boolean[6];
   final CamouflageShape shape;

   public CamouflageStorage(CamouflageShape shape) {
      this.shape = shape;
      Arrays.fill(this.useColor, true);
   }

   public CamouflageShape getShape() {
      return this.shape;
   }

   public void setColor(DirectionList sides, DyeColor color) {
      for (Direction dir : sides) {
         this.useColor[dir.get3DDataValue()] = true;
         this.colorCache[dir.get3DDataValue()].setColor(color);
      }
   }

   public CamouflageStorage setTexture(DirectionList sides, Block block) {
      this.setTexture(
         sides,
         new RetextureEvent.TextureContainer(
            block.defaultBlockState(), Direction.NORTH, new RetextureEvent.Rotation[]{RetextureEvent.Rotation.ROTATION_0}, new int[]{-1}
         )
      );
      return this;
   }

   public void setTexture(DirectionList sides, RetextureEvent.TextureContainer container) {
      for (Direction dir : sides) {
         this.useColor[dir.get3DDataValue()] = false;
         this.textureCache[dir.get3DDataValue()].setData(container);
      }
   }

   public boolean canApply(DirectionList list, RetextureEvent.TextureContainer container) {
      for (Direction dir : list) {
         if (this.useColor[dir.get3DDataValue()] || this.textureCache[dir.get3DDataValue()].canSetData(container)) {
            return true;
         }
      }

      return false;
   }

   public boolean canApply(Direction dir, RetextureEvent.TextureContainer container) {
      return this.useColor[dir.get3DDataValue()] || this.textureCache[dir.get3DDataValue()].canSetData(container);
   }

   public boolean canApply(DirectionList dirs, DyeColor color) {
      if (color == null) {
         return false;
      } else {
         for (Direction dir : dirs) {
            if (!this.useColor[dir.get3DDataValue()] || this.colorCache[dir.get3DDataValue()].getColor() != color) {
               return true;
            }
         }

         return false;
      }
   }

   public DyeColor isSingleColor() {
      DyeColor color = null;

      for (Direction dir : DirectionList.ALL) {
         if (!this.useColor[dir.get3DDataValue()]) {
            return null;
         }

         if (color == null) {
            color = this.colorCache[dir.get3DDataValue()].getColor();
         } else if (this.colorCache[dir.get3DDataValue()].getColor() != color) {
            return null;
         }
      }

      return color;
   }

   public IQuadCache getCache(Direction side) {
      return (IQuadCache)(this.useColor[side.get3DDataValue()] ? this.colorCache[side.get3DDataValue()] : this.textureCache[side.get3DDataValue()]);
   }

   public boolean isColored(Direction dir) {
      return this.useColor[dir.get3DDataValue()];
   }

   @Override
   public void write(IOutputBuffer buffer) {
      Collection<ByteList> slots = this.map().values();
      buffer.writeByte((byte)slots.size());

      for (ByteList list : slots) {
         if (list.size() == 1) {
            byte index = list.getByte(0);
            buffer.writeBoolean(true);
            buffer.writeByte((byte)(index << 1 | (this.useColor[index] ? 1 : 0)));
            ((IQuadCache)(this.useColor[index] ? this.colorCache[index] : this.textureCache[index])).write(buffer);
         } else {
            buffer.writeBoolean(false);
            buffer.writeBytes(list.toByteArray());
            byte index = list.getByte(0);
            buffer.writeBoolean(this.useColor[index]);
            ByteBuf out = Unpooled.buffer();
            ((IQuadCache)(this.useColor[index] ? this.colorCache[index] : this.textureCache[index])).write(new OutputBuffer(out));
            byte[] data = new byte[out.writerIndex()];
            out.readBytes(data);
            buffer.writeBytes(data);
         }
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      int size = buffer.readByte();

      for (int i = 0; i < size; i++) {
         if (buffer.readBoolean()) {
            int data = buffer.readByte();
            int index = data >> 1;
            this.useColor[index] = (data & 1) != 0;
            ((IQuadCache)(this.useColor[index] ? this.colorCache[index] : this.textureCache[index])).read(buffer);
         } else {
            byte[] slots = buffer.readBytes();
            boolean color = buffer.readBoolean();
            IQuadCache[] cache = (IQuadCache[])(color ? this.colorCache : this.textureCache);
            byte[] data = buffer.readBytes();

            for (int index : slots) {
               this.useColor[index] = color;
               cache[index].read(new InputBuffer(data));
            }
         }
      }
   }

   private Object2ObjectMap<CompoundTag, ByteList> map() {
      Object2ObjectMap<CompoundTag, ByteList> mapped = CollectionUtils.<CompoundTag, ByteList>createLinkedMap();

      for (int i = 0; i < 6; i++) {
         CompoundTag data = new CompoundTag();
         ((IQuadCache)(this.useColor[i] ? this.colorCache[i] : this.textureCache[i])).write(data);
         NBTUtils.putBoolean(data, "type", this.useColor[i], false);
         ByteList list = (ByteList)mapped.get(data);
         if (list == null) {
            list = new ByteArrayList();
            mapped.put(data, list);
         }

         list.add((byte)i);
      }

      return mapped;
   }

   public CompoundTag save(CompoundTag nbt) {
      ListTag list = new ListTag();
      ObjectIterator var3 = Object2ObjectMaps.fastIterable(this.map()).iterator();

      while (var3.hasNext()) {
         Entry<CompoundTag, ByteList> entry = (Entry<CompoundTag, ByteList>)var3.next();
         CompoundTag data = (CompoundTag)entry.getKey();
         ByteList slots = (ByteList)entry.getValue();
         if (slots.size() > 1) {
            data.putByteArray("slots", slots.toByteArray());
         } else {
            if (slots.size() != 1) {
               continue;
            }

            data.putByte("slot", slots.getByte(0));
         }

         list.add(data);
      }

      NBTUtils.put(nbt, "cam", list);
      return nbt;
   }

   public void load(CompoundTag nbt) {
      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("cam", 10), CompoundTag.class)) {
         if (data.contains("slot")) {
            int index = data.getInt("slot");
            boolean color = data.getBoolean("type");
            this.useColor[index] = color;
            ((IQuadCache)(color ? this.colorCache[index] : this.textureCache[index])).read(data);
         } else {
            boolean color = data.getBoolean("type");
            byte[] slots = data.getByteArray("slots");
            IQuadCache[] cache = (IQuadCache[])(color ? this.colorCache : this.textureCache);

            for (int index : slots) {
               this.useColor[index] = color;
               cache[index].read(data);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public CamouflageShape.QuadResults getQuads(BlockState state, Level world, BlockPos pos, RenderType type) {
      return this.shape.generateQuads(this, state, world, pos, type);
   }

   static ColorQuadCache[] createColor() {
      ColorQuadCache[] array = new ColorQuadCache[6];

      for (Direction dir : DirectionList.ALL) {
         array[dir.get3DDataValue()] = new ColorQuadCache(dir);
      }

      return array;
   }

   static TextureQuadCache[] createTexture() {
      TextureQuadCache[] array = new TextureQuadCache[6];

      for (Direction dir : DirectionList.ALL) {
         array[dir.get3DDataValue()] = new TextureQuadCache(dir);
      }

      return array;
   }
}
