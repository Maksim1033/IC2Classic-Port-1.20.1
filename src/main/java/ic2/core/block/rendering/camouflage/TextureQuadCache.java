package ic2.core.block.rendering.camouflage;

import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import java.util.Arrays;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

public class TextureQuadCache implements IQuadCache {
   BlockState state;
   Direction side;
   RetextureEvent.Rotation[] rotations;
   int[] colors;

   public TextureQuadCache(Direction dir) {
   }

   public void setData(RetextureEvent.TextureContainer container) {
      this.state = container.getState();
      this.side = container.getSide();
      this.rotations = container.getRotations();
      this.colors = container.getColors();
   }

   public boolean canSetData(RetextureEvent.TextureContainer container) {
      return this.state != container.getState()
         || this.side != container.getSide()
         || !Arrays.equals(this.colors, container.getColors())
         || this.matchesNot(container.getRotations());
   }

   private boolean matchesNot(RetextureEvent.Rotation[] array) {
      if (this.rotations == null) {
         return true;
      } else {
         for (int i = 0; i < array.length; i++) {
            if (array[i] != this.rotations[i]) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeNBTData(NbtUtils.writeBlockState(this.state));
      buffer.writeByte((byte)this.side.get3DDataValue());
      buffer.writeByte((byte)this.colors.length);

      for (int i = 0; i < this.colors.length; i++) {
         buffer.writeByte((byte)this.rotations[i].getIndex());
         buffer.writeInt(this.colors[i]);
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), buffer.readNBTData());
      this.side = Direction.from3DDataValue(buffer.readByte());
      int size = buffer.readByte();
      this.rotations = new RetextureEvent.Rotation[size];
      this.colors = new int[size];

      for (int i = 0; i < size; i++) {
         this.rotations[i] = RetextureEvent.Rotation.byIndex(buffer.readByte());
         this.colors[i] = buffer.readInt();
      }
   }

   @Override
   public CompoundTag write(CompoundTag nbt) {
      nbt.put("state", NbtUtils.writeBlockState(this.state));
      nbt.putByte("side", (byte)this.side.get3DDataValue());
      int[] data = new int[this.colors.length * 2 + 1];
      data[data.length - 1] = this.colors.length;

      for (int i = 0; i < this.colors.length; i++) {
         data[i * 2] = this.rotations[i].getIndex();
         data[i * 2 + 1] = this.colors[i];
      }

      nbt.putIntArray("data", data);
      return nbt;
   }

   @Override
   public void read(CompoundTag nbt) {
      this.state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("state"));
      this.side = Direction.from3DDataValue(nbt.getInt("side"));
      int[] data = nbt.getIntArray("data");
      int size = data[data.length - 1];
      this.rotations = new RetextureEvent.Rotation[size];
      this.colors = new int[size];

      for (int i = 0; i < size; i++) {
         this.rotations[i] = RetextureEvent.Rotation.byIndex(data[i * 2]);
         this.colors[i] = data[i * 2 + 1];
      }
   }

   @Override
   public BlockState getDisplayBlock() {
      return this.state;
   }

   @Override
   public int[] getColors() {
      return this.colors;
   }

   @Override
   public RetextureEvent.Rotation[] getRotations() {
      return this.rotations;
   }

   @Override
   public Direction getSide() {
      return this.side;
   }
}
