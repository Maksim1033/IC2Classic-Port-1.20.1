package ic2.core.networking.buffers;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;

public class InputBuffer implements IInputBuffer {
   FriendlyByteBuf buffer;

   public InputBuffer(byte[] data) {
      this(new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
   }

   public InputBuffer(ByteBuf buf) {
      this(new FriendlyByteBuf(buf));
   }

   public InputBuffer(FriendlyByteBuf buffer) {
      this.buffer = buffer;
   }

   @Override
   public boolean readBoolean() {
      return this.buffer.readBoolean();
   }

   @Override
   public byte readByte() {
      return this.buffer.readByte();
   }

   @Override
   public short readShort() {
      return this.buffer.readShort();
   }

   @Override
   public int readMedium() {
      return this.buffer.readMedium();
   }

   @Override
   public int readInt() {
      return this.buffer.readInt();
   }

   @Override
   public int readVarInt() {
      return this.buffer.readVarInt();
   }

   @Override
   public float readFloat() {
      return this.buffer.readFloat();
   }

   @Override
   public double readDouble() {
      return this.buffer.readDouble();
   }

   @Override
   public long readLong() {
      return this.buffer.readLong();
   }

   @Override
   public long readData(NetworkInfo.BitLevel length) {
      if (length.isSkip()) {
         return 0L;
      } else {
         switch (length) {
            case BIT_0:
               return 0L;
            case BIT_8:
               return (long)this.buffer.readByte();
            case BIT_16:
               return (long)this.buffer.readShort();
            case BIT_24:
               return (long)this.buffer.readMedium();
            case BIT_32:
               return (long)this.buffer.readInt();
            case BIT_40:
               return ((long)this.buffer.readByte() & 255L) << 32
                  | ((long)this.buffer.readByte() & 255L) << 24
                  | ((long)this.buffer.readByte() & 255L) << 16
                  | ((long)this.buffer.readByte() & 255L) << 8
                  | (long)this.buffer.readByte() & 255L;
            case BIT_48:
               return ((long)this.buffer.readByte() & 255L) << 40
                  | ((long)this.buffer.readByte() & 255L) << 32
                  | ((long)this.buffer.readByte() & 255L) << 24
                  | ((long)this.buffer.readByte() & 255L) << 16
                  | ((long)this.buffer.readByte() & 255L) << 8
                  | (long)this.buffer.readByte() & 255L;
            case BIT_56:
               return ((long)this.buffer.readByte() & 255L) << 48
                  | ((long)this.buffer.readByte() & 255L) << 40
                  | ((long)this.buffer.readByte() & 255L) << 32
                  | ((long)this.buffer.readByte() & 255L) << 24
                  | ((long)this.buffer.readByte() & 255L) << 16
                  | ((long)this.buffer.readByte() & 255L) << 8
                  | (long)this.buffer.readByte() & 255L;
            case BIT_64:
               return this.buffer.readLong();
            default:
               return 0L;
         }
      }
   }

   @Override
   public char readChar() {
      return this.buffer.readChar();
   }

   @Override
   public <T extends Enum<T>> T readEnum(Class<T> clz) {
      int value = this.readByte();
      return value == 0 ? null : clz.getEnumConstants()[value - 1];
   }

   @Override
   public byte[] readBytes() {
      return this.buffer.readByteArray();
   }

   @Override
   public String readString() {
      return this.buffer.readUtf(262144);
   }

   @Override
   public CompoundTag readNBTData() {
      return this.buffer.readNbt();
   }

   @Override
   public <T> T readForgeRegistryEntry(IForgeRegistry<T> registry) {
      return (T)registry.getValue(this.buffer.readResourceLocation());
   }

   @Override
   public ItemStack readItemStack() {
      return this.buffer.readItem();
   }

   @Override
   public FluidStack readFluidStack() {
      if (!this.buffer.readBoolean()) {
         return FluidStack.EMPTY;
      } else {
         Fluid fluid = ForgeRegistryWrapper.getFluidValue(this.buffer.readResourceLocation());
         FluidStack stack = new FluidStack(fluid, this.buffer.readVarInt());
         stack.setTag(this.buffer.readNbt());
         return stack;
      }
   }

   @Override
   public UUID readUUID() {
      return this.buffer.readUUID();
   }

   @Override
   public <T> ResourceKey<T> readRegistryKey(ResourceKey<Registry<T>> key) {
      return ResourceKey.create(key, this.buffer.readResourceLocation());
   }
}
