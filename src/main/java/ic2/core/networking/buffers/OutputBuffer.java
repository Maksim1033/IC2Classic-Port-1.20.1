package ic2.core.networking.buffers;

import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;

public class OutputBuffer implements IOutputBuffer {
   FriendlyByteBuf buffer;

   public OutputBuffer(ByteBuf buf) {
      this(new FriendlyByteBuf(buf));
   }

   public OutputBuffer(FriendlyByteBuf buffer) {
      this.buffer = buffer;
   }

   @Override
   public void writeBoolean(boolean value) {
      this.buffer.writeBoolean(value);
   }

   @Override
   public void writeByte(byte value) {
      this.buffer.writeByte(value);
   }

   @Override
   public void writeShort(short value) {
      this.buffer.writeShort(value);
   }

   @Override
   public void writeMedium(int value) {
      this.buffer.writeMedium(value);
   }

   @Override
   public void writeInt(int value) {
      this.buffer.writeInt(value);
   }

   @Override
   public void writeVarInt(int value) {
      this.buffer.writeVarInt(value);
   }

   @Override
   public void writeFloat(float value) {
      this.buffer.writeFloat(value);
   }

   @Override
   public void writeDouble(double value) {
      this.buffer.writeDouble(value);
   }

   @Override
   public void writeLong(long value) {
      this.buffer.writeLong(value);
   }

   @Override
   public void writeData(long value, NetworkInfo.BitLevel type) {
      if (!type.isSkip()) {
         switch (type) {
            case BIT_0:
            default:
               break;
            case BIT_8:
               this.buffer.writeByte((int)value);
               break;
            case BIT_16:
               this.buffer.writeShort((int)value);
               break;
            case BIT_24:
               this.buffer.writeMedium((int)value);
               break;
            case BIT_32:
               this.buffer.writeInt((int)value);
               break;
            case BIT_40:
               this.buffer.writeByte((byte)((int)(value >>> 32)));
               this.buffer.writeByte((byte)((int)(value >>> 24)));
               this.buffer.writeByte((byte)((int)(value >>> 16)));
               this.buffer.writeByte((byte)((int)(value >>> 8)));
               this.buffer.writeByte((byte)((int)value));
               break;
            case BIT_48:
               this.buffer.writeByte((byte)((int)(value >>> 40)));
               this.buffer.writeByte((byte)((int)(value >>> 32)));
               this.buffer.writeByte((byte)((int)(value >>> 24)));
               this.buffer.writeByte((byte)((int)(value >>> 16)));
               this.buffer.writeByte((byte)((int)(value >>> 8)));
               this.buffer.writeByte((byte)((int)value));
               break;
            case BIT_56:
               this.buffer.writeByte((byte)((int)(value >>> 48)));
               this.buffer.writeByte((byte)((int)(value >>> 40)));
               this.buffer.writeByte((byte)((int)(value >>> 32)));
               this.buffer.writeByte((byte)((int)(value >>> 24)));
               this.buffer.writeByte((byte)((int)(value >>> 16)));
               this.buffer.writeByte((byte)((int)(value >>> 8)));
               this.buffer.writeByte((byte)((int)value));
               break;
            case BIT_64:
               this.buffer.writeLong(value);
         }
      }
   }

   @Override
   public void writeChar(char value) {
      this.buffer.writeChar(value);
   }

   @Override
   public void writeEnum(Enum<?> value) {
      this.buffer.writeByte(value == null ? 0 : value.ordinal() + 1);
   }

   @Override
   public void writeString(String value) {
      this.buffer.writeUtf(value, 262144);
   }

   @Override
   public void writeBytes(byte[] value) {
      this.buffer.writeByteArray(value);
   }

   @Override
   public void writeNBTData(CompoundTag value) {
      this.buffer.writeNbt(value);
   }

   @Override
   public <T> void writeForgeEntry(T value, IForgeRegistry<T> registry) {
      this.buffer.writeResourceLocation(registry.getKey(value));
   }

   @Override
   public void writeItemStack(ItemStack stack) {
      this.buffer.writeItem(stack);
   }

   @Override
   public void writeFluidStack(FluidStack stack) {
      if (stack.isEmpty()) {
         this.buffer.writeBoolean(false);
      } else {
         this.buffer.writeBoolean(true);
         this.buffer.writeResourceLocation(ForgeRegistryWrapper.getFluidKey(stack.getFluid()));
         this.buffer.writeVarInt(stack.getAmount());
         this.buffer.writeNbt(stack.getTag());
      }
   }

   @Override
   public void writeUUID(UUID value) {
      this.buffer.writeUUID(value);
   }

   @Override
   public void writeRegistryKey(ResourceKey<?> key) {
      this.buffer.writeResourceLocation(key.location());
   }
}
